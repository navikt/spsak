package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.MatchBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.MatchBeregningsgrunnlagTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@ApplicationScoped
public class FastsettEndretBeregningsgrunnlagOppdaterer {

    private static final int MÅNEDER_I_1_ÅR = 12;
    private HistorikkTjenesteAdapter historikkAdapter;
    private KodeverkRepository kodeverkRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    FastsettEndretBeregningsgrunnlagOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettEndretBeregningsgrunnlagOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                      HistorikkTjenesteAdapter historikkAdapter,
                                                      ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.matchBeregningsgrunnlagTjeneste = new MatchBeregningsgrunnlagTjenesteImpl(repositoryProvider);
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(FastsettEndretBeregningsgrunnlagDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        HistorikkInnslagTekstBuilder historikkBuilder = historikkAdapter.tekstBuilder();
        List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
        for (FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode : dto.getEndretBeregningsgrunnlagPerioder()) {
            BeregningsgrunnlagPeriode korrektPeriode = getKorrektPeriode(behandling, perioder, endretPeriode);
            slettAndelerSomBleLagtTilTidligereOgSåFjernet(endretPeriode, korrektPeriode);
            for (FastsettEndretBeregningsgrunnlagAndelDto endretAndel : endretPeriode.getAndeler()) {
                BeregningsgrunnlagPrStatusOgAndel korrektAndel;
                if (endretAndel.getNyAndel() || endretAndel.getLagtTilAvSaksbehandler()) {
                    korrektAndel = Kopimaskin.deepCopy(getKorrektAndel(behandling, korrektPeriode, endretAndel));
                } else {
                    korrektAndel = getKorrektAndel(behandling, korrektPeriode, endretAndel);
                }
                leggTilArbeidsforholdHistorikkinnslag(endretAndel, historikkBuilder, korrektAndel, korrektPeriode, behandling);
                settInntektskategoriOgFastsattBeløp(endretAndel, korrektAndel, korrektPeriode);
            }
        }
    }

    private void slettAndelerSomBleLagtTilTidligereOgSåFjernet(FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode, BeregningsgrunnlagPeriode korrektPeriode) {
        List<Long> andelsnummerListe = endretPeriode.getAndeler().stream().filter(endretAndel -> !endretAndel.getNyAndel())
            .map(FastsettEndretBeregningsgrunnlagAndelDto::getAndelsnr).collect(Collectors.toList());
        BeregningsgrunnlagPeriode.builder(korrektPeriode).fjernBeregningsgrunnlagPrStatusOgAndelerSomIkkeLiggerIListeAvAndelsnr(andelsnummerListe);
    }


    private void leggTilArbeidsforholdHistorikkinnslag(FastsettEndretBeregningsgrunnlagAndelDto andel,
                                                       HistorikkInnslagTekstBuilder historikkBuilder, BeregningsgrunnlagPrStatusOgAndel korrektAndel,
                                                       BeregningsgrunnlagPeriode korrektPeriode, Behandling behandling) {
        String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(korrektAndel);
        HistorikkEndretFeltType endretFeltType = andel.getNyAndel() ? HistorikkEndretFeltType.NY_AKTIVITET : HistorikkEndretFeltType.NY_FORDELING;
        historikkBuilder.medNavnOgGjeldendeFra(endretFeltType, arbeidsforholdInfo, korrektPeriode.getBeregningsgrunnlagPeriodeFom());
        if (andel.getFastsatteVerdier().getRefusjon() != null
            && korrektAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null) != null
            && BigDecimal.valueOf(andel.getFastsatteVerdier().getRefusjon()).compareTo(
            Objects.requireNonNull(korrektAndel.getBgAndelArbeidsforhold().get().getRefusjonskravPrÅr())
                .divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP)) != 0) {
            historikkBuilder.medEndretFelt(HistorikkEndretFeltType.NYTT_REFUSJONSKRAV,
                korrektAndel.getBgAndelArbeidsforhold().get().getRefusjonskravPrÅr().divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP), andel.getFastsatteVerdier().getRefusjon());
        }
        historikkBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT, null, andel.getFastsatteVerdier().getFastsattBeløp());
        historikkBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKTSKATEGORI, null, andel.getFastsatteVerdier().getInntektskategori());

        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkBuilder.medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkBuilder.ferdigstillHistorikkinnslagDel();
    }


    private void settInntektskategoriOgFastsattBeløp(FastsettEndretBeregningsgrunnlagAndelDto endretAndel,
                                                     BeregningsgrunnlagPrStatusOgAndel korrektAndel, BeregningsgrunnlagPeriode korrektPeriode) {
        Inntektskategori inntektskategori = kodeverkRepository.finn(Inntektskategori.class, endretAndel.getFastsatteVerdier().getInntektskategori().getKode());

        BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder(korrektAndel)
            .medBeregnetPrÅr(BigDecimal.valueOf(endretAndel.getFastsatteVerdier().getFastsattBeløp()).multiply(BigDecimal.valueOf(MÅNEDER_I_1_ÅR)))
            .medInntektskategori(inntektskategori)
            .medFastsattAvSaksbehandler(true);

        BigDecimal refusjonPrMnd = endretAndel.getFastsatteVerdier().getRefusjon() != null
            ? BigDecimal.valueOf(endretAndel.getFastsatteVerdier().getRefusjon())
            : korrektAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null);

        if (refusjonPrMnd != null) {
            BigDecimal refusjonPrÅr = refusjonPrMnd.multiply(BigDecimal.valueOf(MÅNEDER_I_1_ÅR));
            BGAndelArbeidsforhold.Builder builder = BGAndelArbeidsforhold
                .builder(korrektAndel.getBgAndelArbeidsforhold().orElse(null))
                .medRefusjonskravPrÅr(refusjonPrÅr);
            andelBuilder.medBGAndelArbeidsforhold(builder);
        }

        if (endretAndel.getNyAndel() || endretAndel.getLagtTilAvSaksbehandler()) {
            andelBuilder.nyttAndelsnr(korrektPeriode).medLagtTilAvSaksbehandler(true).build(korrektPeriode);
        }
    }


    private BeregningsgrunnlagPeriode getKorrektPeriode(Behandling behandling, List<BeregningsgrunnlagPeriode> perioder, FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode) {
        return perioder.stream()
            .filter(periode -> periode.getBeregningsgrunnlagPeriodeFom().equals(endretPeriode.getFom()))
            .findFirst()
            .orElseThrow(() -> FastsettEndretBeregningsgrunnlagOppdaterer.FastsettEndretBeregningsgrunnlagOppdatererFeil.FACTORY.finnerIkkePeriodeFeil(behandling.getId()).toException());
    }


    BeregningsgrunnlagPrStatusOgAndel getKorrektAndel(Behandling behandling, BeregningsgrunnlagPeriode periode, FastsettEndretBeregningsgrunnlagAndelDto endretAndel) {
        if (endretAndel.getLagtTilAvSaksbehandler() && !endretAndel.getNyAndel()) {
            return matchBeregningsgrunnlagTjeneste.matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, endretAndel.getAndelsnr(), endretAndel.getArbeidsforholdId())
                .orElseThrow(() -> FastsettEndretBeregningsgrunnlagOppdatererFeil.FACTORY.fantIkkeForrigeGrunnlag(endretAndel.getAndelsnr() == null ? null :
                        endretAndel.getAndelsnr().toString(),
                    endretAndel.getArbeidsforholdId(), behandling.getId()).toException());
        }
        return matchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(behandling, periode, endretAndel.getAndelsnr(), endretAndel.getArbeidsforholdId());
    }


    interface FastsettEndretBeregningsgrunnlagOppdatererFeil extends DeklarerteFeil {

        FastsettEndretBeregningsgrunnlagOppdaterer.FastsettEndretBeregningsgrunnlagOppdatererFeil FACTORY = FeilFactory.create(FastsettEndretBeregningsgrunnlagOppdaterer.FastsettEndretBeregningsgrunnlagOppdatererFeil.class);

        @TekniskFeil(feilkode = "FP-401645", feilmelding = "Finner ikke periode for eksisterende grunnlag. Behandling %s", logLevel = LogLevel.WARN)
        Feil finnerIkkePeriodeFeil(long behandlingId);


        @TekniskFeil(feilkode = "FP-401699", feilmelding = "Fant ikke grunnlag fra tidligere faktaavklaring for andel med andelsnr %s og arbeidsforholdId %s. Behandling %s", logLevel = LogLevel.WARN)
        Feil fantIkkeForrigeGrunnlag(String andelsnr,String arbeidsforholdId, long behandlingId);
    }


}
