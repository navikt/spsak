package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;


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
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.TilstotendeYtelseOgEndretBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto;

@ApplicationScoped
public class VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer {


    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkRepository kodeverkRepository;
    private MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste;
    private FastsettBGTilstøtendeYtelseOppdaterer tyOppdaterer;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderTilstøtendeYtelseOgEndretBeregninsgrunnlagOppdaterer(GrunnlagRepositoryProvider repositoryProvider,
                                                                      ResultatRepositoryProvider resultatRepositoryProvider,
                                                                      HistorikkTjenesteAdapter historikkAdapter,
                                                                      ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.matchBeregningsgrunnlagTjeneste = new MatchBeregningsgrunnlagTjeneste(resultatRepositoryProvider);
        this.tyOppdaterer = new FastsettBGTilstøtendeYtelseOppdaterer(repositoryProvider, resultatRepositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(TilstotendeYtelseOgEndretBeregningsgrunnlagDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {

        if (dto.getPerioder().isEmpty()) {
            throw new IllegalArgumentException("Liste med perioder kan ikkje vere tom.");
        }
        List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriode bgPeriode : perioder) {
            Optional<TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto> periodeOptional = dto.getPerioder()
                .stream().filter(p -> p.getFom().isEqual(bgPeriode.getBeregningsgrunnlagPeriodeFom())).findFirst();
            if (periodeOptional.isPresent()) {
                settVerdierForAndelerIPeriode(behandling, bgPeriode, periodeOptional.get(), dto.erBesteberegning());
            } else {
                TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto førstePeriodeDto = dto.getPerioder().get(0);
                settVerdierForAndelerIPeriode(behandling, bgPeriode, førstePeriodeDto, dto.erBesteberegning());
            }
        }
    }

    private void settVerdierForAndelerIPeriode(Behandling behandling, BeregningsgrunnlagPeriode bgPeriode,
                                               TilstotendeYtelseOgEndretBeregningsgrunnlagPeriodeDto periode, boolean gjelderBesteberegning) {
        for (FastsattBeløpTilstøtendeYtelseAndelDto andelDto : periode.getAndeler()) {
            BeregningsgrunnlagPrStatusOgAndel korrektAndel;
            if (andelDto.getNyAndel() || andelDto.getLagtTilAvSaksbehandler()) {
                Optional<BeregningsgrunnlagAndeltype> andeltypeOpt = kodeverkRepository.finnOptional(BeregningsgrunnlagAndeltype.class, andelDto.getAndel());
                if (andeltypeOpt.isPresent() && andelDto.getNyAndel()) {
                    fastsettBeløpForNyeAndelerUtenArbeidsforhold(bgPeriode, andelDto);
                    leggTilHistorikkinnslagForAndelstype(andelDto, bgPeriode, behandling);
                    continue;
                } else {
                    korrektAndel = Kopimaskin.deepCopy(getKorrektAndel(behandling, bgPeriode, andelDto));
                }
            } else {
                korrektAndel = getKorrektAndel(behandling, bgPeriode, andelDto);
            }
            leggTilArbeidsforholdHistorikkinnslag(andelDto, korrektAndel, bgPeriode, behandling, gjelderBesteberegning);
            settInntektskategoriOgFastsattBeløp(andelDto, korrektAndel, bgPeriode, gjelderBesteberegning);
        }
    }

    private void settInntektskategoriOgFastsattBeløp(FastsattBeløpTilstøtendeYtelseAndelDto andelDto,
                                                     BeregningsgrunnlagPrStatusOgAndel korrektAndel, BeregningsgrunnlagPeriode korrektPeriode, boolean gjelderBesteberegning) {
            Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, andelDto.getInntektskategori().getKode());
            BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder(korrektAndel)
                .medBeregnetPrÅr(BigDecimal.valueOf(andelDto.getFastsattBeløp()))
                .medOverstyrtPrÅr(andelDto.finnRedusertBeløp().getVerdi())
                .medInntektskategori(inntektskategori)
                .medFastsattAvSaksbehandler(true);
            andelDto.getRefusjonskravPrAar().map(BigDecimal::valueOf).ifPresent(refusjonPrÅr ->
                leggTilRefusjonskravHvisAndelHarArbeidsforhold(korrektAndel, andelBuilder, refusjonPrÅr)
            );
            if (korrektAndel.getAktivitetStatus().equals(AktivitetStatus.DAGPENGER) && gjelderBesteberegning) {
                andelBuilder
                    .medBesteberegningPrÅr(BigDecimal.valueOf(andelDto.getFastsattBeløp()));
            }
            if (andelDto.getNyAndel() || andelDto.getLagtTilAvSaksbehandler()) {
                leggTilRefusjonskravHvisAndelHarArbeidsforhold(korrektAndel, andelBuilder, BigDecimal.ZERO);
                andelBuilder
                    .nyttAndelsnr(korrektPeriode)
                    .medLagtTilAvSaksbehandler(true).build(korrektPeriode);
            }
    }

    private void leggTilRefusjonskravHvisAndelHarArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel korrektAndel, BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder, BigDecimal refusjonPrÅr) {
        korrektAndel.getBgAndelArbeidsforhold().ifPresent(af -> andelBuilder
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(af)
            .medRefusjonskravPrÅr(refusjonPrÅr)));
    }


    private BeregningsgrunnlagPrStatusOgAndel getKorrektAndel(Behandling behandling, BeregningsgrunnlagPeriode periode, FastsattBeløpTilstøtendeYtelseAndelDto endretAndel) {
        if (endretAndel.getLagtTilAvSaksbehandler() && !endretAndel.getNyAndel()) {
            return tyOppdaterer.finnAndelFraForrigeGrunnlag(behandling, periode, endretAndel);
        }
        return matchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(behandling, periode, endretAndel.getAndelsnr(), endretAndel.getArbeidsforholdId());
    }

    private void fastsettBeløpForNyeAndelerUtenArbeidsforhold(BeregningsgrunnlagPeriode bgPeriode,
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

    private void leggTilHistorikkinnslagForAndelstype(FastsattBeløpTilstøtendeYtelseAndelDto andel, BeregningsgrunnlagPeriode korrektPeriode, Behandling behandling) {
        if (korrektPeriode.getBeregningsgrunnlagPeriodeFom().isEqual(korrektPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt())) {
            tyOppdaterer.leggTilHistorikkinnslagForNyAndelMedAndelstype(behandling, andel);
        } else {
            String andelsInfo = tyOppdaterer.getAndelsinfoForNyAndelMedAndelstype(andel);
            HistorikkEndretFeltType endretFeltType = settEndretFeltType(andel);
            HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = historikkAdapter.tekstBuilder();
            historikkInnslagTekstBuilder.medNavnOgGjeldendeFra(endretFeltType, andelsInfo, korrektPeriode.getBeregningsgrunnlagPeriodeFom());
            if (andel.getRefusjonskravPrAar().isPresent()) { // NOSONAR
                historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.NYTT_REFUSJONSKRAV,null, andel.getRefusjonskravPrAar().get()); // NOSONAR
            }
            historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT, null, andel.getFastsattBeløp());
            historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKTSKATEGORI, null, andel.getInntektskategori());
            settSkjermlenke(behandling, historikkInnslagTekstBuilder);
            historikkInnslagTekstBuilder.ferdigstillHistorikkinnslagDel();
        }
    }

    private void settSkjermlenke(Behandling behandling, HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder) {
        List<HistorikkinnslagDel> historikkDeler = historikkInnslagTekstBuilder.getHistorikkinnslagDeler();
        boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkInnslagTekstBuilder.medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
    }

    private HistorikkEndretFeltType settEndretFeltType(FastsattBeløpTilstøtendeYtelseAndelDto andel) {
        return andel.getNyAndel() ? HistorikkEndretFeltType.NY_AKTIVITET : HistorikkEndretFeltType.NY_FORDELING;
    }

    private void leggTilArbeidsforholdHistorikkinnslag(FastsattBeløpTilstøtendeYtelseAndelDto andel, BeregningsgrunnlagPrStatusOgAndel korrektAndel,
                                                       BeregningsgrunnlagPeriode korrektPeriode, Behandling behandling, boolean gjelderBesteberegning) {
        if (korrektPeriode.getBeregningsgrunnlagPeriodeFom().isEqual(korrektPeriode.getBeregningsgrunnlag().getSkjæringstidspunkt())) {
            tyOppdaterer.leggTilHistorikkinnslag(behandling, korrektPeriode, andel, korrektAndel, gjelderBesteberegning);
        } else {
            String andelsInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(korrektAndel);
            HistorikkEndretFeltType endretFeltType = settEndretFeltType(andel);
            HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = historikkAdapter.tekstBuilder();
            historikkInnslagTekstBuilder.medNavnOgGjeldendeFra(endretFeltType, andelsInfo, korrektPeriode.getBeregningsgrunnlagPeriodeFom());
            if (andel.getRefusjonskravPrAar().isPresent() && harIkkjeRefusjonskravFråFørEllerRefusjonskravErUlike(andel, korrektAndel)) { // NOSONAR
                historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.NYTT_REFUSJONSKRAV,
                    getRefusjonskravEllerNull(korrektAndel), andel.getRefusjonskravPrAar().get()); // NOSONAR
            }
            historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT, null, andel.getFastsattBeløp());
            historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKTSKATEGORI, null, andel.getInntektskategori());
            settSkjermlenke(behandling, historikkInnslagTekstBuilder);
            historikkInnslagTekstBuilder.ferdigstillHistorikkinnslagDel();
        }
    }

    private boolean harIkkjeRefusjonskravFråFørEllerRefusjonskravErUlike(FastsattBeløpTilstøtendeYtelseAndelDto andel, BeregningsgrunnlagPrStatusOgAndel korrektAndel) {
        return getRefusjonskravEllerNull(korrektAndel) == null
            || BigDecimal.valueOf(andel.getRefusjonskravPrAar().get()).compareTo(korrektAndel.getBgAndelArbeidsforhold() // NOSONAR
            .map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).get()) != 0; // NOSONAR
    }

    private BigDecimal getRefusjonskravEllerNull(BeregningsgrunnlagPrStatusOgAndel korrektAndel) {
        return korrektAndel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null);
    }

}
