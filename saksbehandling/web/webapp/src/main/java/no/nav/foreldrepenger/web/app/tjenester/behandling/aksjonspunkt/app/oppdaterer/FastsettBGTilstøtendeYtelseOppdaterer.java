package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAndeltype;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.MatchBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.MatchBeregningsgrunnlagTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBGTilstøtendeYtelseDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class FastsettBGTilstøtendeYtelseOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkRepository kodeverkRepository;
    private MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;
    FastsettBGTilstøtendeYtelseOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBGTilstøtendeYtelseOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                 ResultatRepositoryProvider resultatRepositoryProvider,
                                                 HistorikkTjenesteAdapter historikkAdapter, ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.matchBeregningsgrunnlagTjeneste = new MatchBeregningsgrunnlagTjenesteImpl(resultatRepositoryProvider);
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(FastsettBGTilstøtendeYtelseDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (FastsattBeløpTilstøtendeYtelseAndelDto andel : dto.getTilstøtendeYtelseAndeler()) {
            Optional<BeregningsgrunnlagAndeltype> andeltypeOpt = kodeverkRepository.finnOptional(BeregningsgrunnlagAndeltype.class, andel.getAndel());
            if (andeltypeOpt.isPresent() && andel.getNyAndel()) {
                fastsettBeløpForNyeAndelerUtenArbeidsforhold(perioder, andel);
                leggTilHistorikkinnslagForNyAndelMedAndelstype(behandling, andel);
            } else {
                BeregningsgrunnlagPrStatusOgAndel korrektAndel = Kopimaskin.deepCopy(getKorrektAndel(behandling, perioder, andel));
                leggTilHistorikkinnslag(behandling, perioder.get(0), andel, korrektAndel, dto.erBesteberegning());
                settInntektskategoriOgFastsattBeløp(andel, korrektAndel, perioder, dto.erBesteberegning());
            }
        }
    }

    void leggTilHistorikkinnslag(Behandling behandling, BeregningsgrunnlagPeriode periode, FastsattBeløpTilstøtendeYtelseAndelDto andel,
                                 BeregningsgrunnlagPrStatusOgAndel korrektAndel, boolean gjelderBesteberegning) {
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndelIForrigeGrunnlag = matchBeregningsgrunnlagTjeneste
            .matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, andel.getAndelsnr(), andel.getArbeidsforholdId());
        if (andel.getNyAndel() || !korrektAndelIForrigeGrunnlag.isPresent()) {
            leggTilHistorikkinnslagForFørsteGang(behandling, andel, korrektAndel);
        } else {
            sammenlignVerdierOgLeggTilHistorikkinnslagForEksisterendeAndelEtterFørsteGang(behandling, andel, korrektAndelIForrigeGrunnlag.get(), gjelderBesteberegning, periode);
        }
    }

    private void leggTilHistorikkinnslagForFørsteGang(Behandling behandling, FastsattBeløpTilstøtendeYtelseAndelDto andel, BeregningsgrunnlagPrStatusOgAndel korrektAndel) {
        Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, andel.getInntektskategori().getKode());
        String andelsInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(korrektAndel);
        if (andel.getNyAndel()) {
            lagHistorikkinnslagdelForNyAndel(andelsInfo, inntektskategori.getNavn(), andel.getFastsattBeløp());
            settSkjemlenkeOmIkkeSatt(behandling);
        } else {
            lagHistorikkinnslagdelForFordeling(behandling, andelsInfo, inntektskategori, andel.getFastsattBeløp(), null);
            lagHistorikkinnslagdelForInntektskategori(behandling, andelsInfo, inntektskategori, null);
        }
    }

    void leggTilHistorikkinnslagForNyAndelMedAndelstype(Behandling behandling, FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, andel.getInntektskategori().getKode());
        lagHistorikkinnslagdelForNyAndel(getAndelsinfoForNyAndelMedAndelstype(andel), inntektskategori.getNavn(), andel.getFastsattBeløp());
        settSkjemlenkeOmIkkeSatt(behandling);
    }



    private void sammenlignVerdierOgLeggTilHistorikkinnslagForEksisterendeAndelEtterFørsteGang(Behandling behandling, FastsattBeløpTilstøtendeYtelseAndelDto andel,
                                                                                               BeregningsgrunnlagPrStatusOgAndel korrektAndelIForrigeGrunnlag, boolean gjelderBesteberegning, BeregningsgrunnlagPeriode periode) {
        Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, andel.getInntektskategori().getKode());
        Inntektskategori forrigeInntektskategori = korrektAndelIForrigeGrunnlag.getInntektskategori();
        Integer forrigeBeløp = settForrigeBeløpEllerNull(korrektAndelIForrigeGrunnlag, gjelderBesteberegning);
        String andelsInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(korrektAndelIForrigeGrunnlag);
        lagHistorikkinnslagdelForFordeling(behandling, andelsInfo, forrigeInntektskategori, andel.getFastsattBeløp(), forrigeBeløp);
        lagHistorikkinnslagdelForInntektskategori(behandling, andelsInfo, inntektskategori, forrigeInntektskategori);
    }

    private Integer settForrigeBeløpEllerNull(BeregningsgrunnlagPrStatusOgAndel korrektAndel, boolean gjelderBesteberegning) {
        if (gjelderBesteberegning && AktivitetStatus.DAGPENGER.equals(korrektAndel.getAktivitetStatus())) {
            return korrektAndel.getBesteberegningPrÅr() == null ? null : korrektAndel.getBesteberegningPrÅr().intValue();
        } else {
            return korrektAndel.getOverstyrtPrÅr() == null ? null : korrektAndel.getOverstyrtPrÅr().intValue();
        }
    }

    String getAndelsinfoForNyAndelMedAndelstype(FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        Optional<BeregningsgrunnlagAndeltype> andeltypeOpt = kodeverkRepository.finnOptional(BeregningsgrunnlagAndeltype.class, andel.getAndel());
        if (andeltypeOpt.isPresent()) {
            return andeltypeOpt.get().getNavn();
        } else {
            return andel.getAndel();
        }
    }


    private void lagHistorikkinnslagdelForNyAndel(String andel, String inntektskategori, Integer fastsattBeløp) {
        historikkAdapter.tekstBuilder()
            .medTema(HistorikkEndretFeltType.FORDELING_FOR_NY_ANDEL, andel)
            .medEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_NY_ANDEL, inntektskategori, null, fastsattBeløp);
    }

    private void lagHistorikkinnslagdelForInntektskategori(Behandling behandling, String andelsInfo, Inntektskategori inntektskategori, Inntektskategori forrigeInntektskategori) {
        if (inntektskategori != null && !inntektskategori.equals(forrigeInntektskategori)) {
            historikkAdapter.tekstBuilder()
                .medEndretFelt(HistorikkEndretFeltType.INNTEKTSKATEGORI_FOR_ANDEL, andelsInfo, forrigeInntektskategori, inntektskategori);
            settSkjemlenkeOmIkkeSatt(behandling);
        }
    }

    private void lagHistorikkinnslagdelForFordeling(Behandling behandling, String andel, Inntektskategori inntektskategori, Integer fastsattBeløp, Integer forrigeBeløp) {
        if (fastsattBeløp != null && !fastsattBeløp.equals(forrigeBeløp)){
            historikkAdapter.tekstBuilder()
                .medTema(HistorikkEndretFeltType.FORDELING_FOR_ANDEL, andel)
                .medEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL, inntektskategori.getNavn(), forrigeBeløp, fastsattBeløp);
            settSkjemlenkeOmIkkeSatt(behandling);
        }
    }

    void settInntektskategoriOgFastsattBeløp(FastsattBeløpTilstøtendeYtelseAndelDto andel, BeregningsgrunnlagPrStatusOgAndel korrektAndel,
                                                     List<BeregningsgrunnlagPeriode> perioder, boolean gjelderBesteberegning) {
        Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, andel.getInntektskategori().getKode());
        for(BeregningsgrunnlagPeriode bgPeriode : perioder) {
            if (andel.getNyAndel() || andel.getLagtTilAvSaksbehandler()) {
                BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder(Kopimaskin.deepCopy(korrektAndel))
                    .medBeregnetPrÅr(BigDecimal.valueOf(andel.getFastsattBeløp()))
                    .medOverstyrtPrÅr(andel.finnRedusertBeløp().getVerdi())
                    .medInntektskategori(inntektskategori)
                    .medFastsattAvSaksbehandler(true)
                    .nyttAndelsnr(bgPeriode)
                    .medLagtTilAvSaksbehandler(true);
                korrektAndel.getBgAndelArbeidsforhold().ifPresent(bga ->
                    andelBuilder.medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(Kopimaskin.deepCopy(bga)).medRefusjonskravPrÅr(null)));
                andelBuilder.build(bgPeriode);
            } else {
                Optional<BeregningsgrunnlagPrStatusOgAndel> matchetAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()
                    .stream().filter(bgAndel -> bgAndel.equals(korrektAndel)).findFirst();
                matchetAndel.ifPresent(match -> {
                        if (gjelderBesteberegning && match.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER)) {
                        BeregningsgrunnlagPrStatusOgAndel.builder(match)
                            .medBeregnetPrÅr(BigDecimal.valueOf(andel.getFastsattBeløp()))
                            .medBesteberegningPrÅr(BigDecimal.valueOf(andel.getFastsattBeløp()))
                            .medOverstyrtPrÅr(andel.finnRedusertBeløp().getVerdi())
                            .medInntektskategori(inntektskategori)
                            .medFastsattAvSaksbehandler(true);
                        } else {
                            BeregningsgrunnlagPrStatusOgAndel.builder(match)
                                .medBeregnetPrÅr(BigDecimal.valueOf(andel.getFastsattBeløp()))
                                .medOverstyrtPrÅr(andel.finnRedusertBeløp().getVerdi())
                                .medInntektskategori(inntektskategori)
                                .medFastsattAvSaksbehandler(true);
                        }
                    }

                );
            }
        }
    }

    BeregningsgrunnlagPrStatusOgAndel getKorrektAndel(Behandling behandling, List<BeregningsgrunnlagPeriode> perioder, FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        if (andel.getLagtTilAvSaksbehandler() && !andel.getNyAndel()) {
            return finnAndelFraForrigeGrunnlag(behandling, perioder.get(0), andel);
        }
        return perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(a -> a.getAndelsnr().equals(andel.getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> FastsettBGTilstøtendeYtelseOppdatererFeil.FACTORY.finnerIkkeAndelFeil(behandling.getId()).toException());
    }

    BeregningsgrunnlagPrStatusOgAndel finnAndelFraForrigeGrunnlag(Behandling behandling, BeregningsgrunnlagPeriode periode, FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        return matchBeregningsgrunnlagTjeneste.matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, andel.getAndelsnr(), andel.getArbeidsforholdId())
            .orElseThrow(() -> FastsettBGTilstøtendeYtelseOppdatererFeil.FACTORY.fantIkkeForrigeGrunnlag(andel.getAndelsnr() == null ? null : andel.getAndelsnr().toString(),
                andel.getArbeidsforholdId(), behandling.getId()).toException());
    }

    private void fastsettBeløpForNyeAndelerUtenArbeidsforhold(List<BeregningsgrunnlagPeriode> perioder,
                                                              FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        AktivitetStatus status = null;
        if (andel.getAndel().equals(BeregningsgrunnlagAndeltype.BRUKERS_ANDEL.getKode())) {
            status = AktivitetStatus.BRUKERS_ANDEL;
        }
        if (andel.getAndel().equals(BeregningsgrunnlagAndeltype.EGEN_NÆRING.getKode())) {
            status = AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;
        }
        if (andel.getAndel().equals(BeregningsgrunnlagAndeltype.FRILANS.getKode())) {
            status = AktivitetStatus.FRILANSER;
        }
        if (status != null) {
            for (BeregningsgrunnlagPeriode bgPeriode : perioder) {
                BeregningsgrunnlagPrStatusOgAndel.builder()
                    .medAktivitetStatus(status)
                    .medInntektskategori(andel.getInntektskategori())
                    .medBeregnetPrÅr(BigDecimal.valueOf(andel.getFastsattBeløp()))
                    .medOverstyrtPrÅr(andel.finnRedusertBeløp().getVerdi())
                    .medFastsattAvSaksbehandler(true)
                    .medLagtTilAvSaksbehandler(true)
                    .build(bgPeriode);
            }
        }
    }

    private void settSkjemlenkeOmIkkeSatt(Behandling behandling) {
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
    }


    private interface FastsettBGTilstøtendeYtelseOppdatererFeil extends DeklarerteFeil {

        FastsettBGTilstøtendeYtelseOppdatererFeil FACTORY = FeilFactory.create(FastsettBGTilstøtendeYtelseOppdatererFeil.class);

        @TekniskFeil(feilkode = "FP-401643", feilmelding = "Finner ikke andelen for eksisterende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil finnerIkkeAndelFeil(long behandlingId);


        @TekniskFeil(feilkode = "FP-401698", feilmelding = "Fant ikke grunnlag fra tidligere faktaavklaring for andel med andelsnr %s og arbeidsforholdId %s. Behandling %s", logLevel = LogLevel.WARN)
        Feil fantIkkeForrigeGrunnlag(String andelsnr,String arbeidsforholdId, long behandlingId);
    }


}
