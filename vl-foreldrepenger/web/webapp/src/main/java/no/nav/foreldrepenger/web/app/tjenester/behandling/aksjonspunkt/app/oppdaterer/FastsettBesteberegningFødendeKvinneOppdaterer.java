package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BesteberegningFødendeKvinneAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BesteberegningFødendeKvinneDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
public class FastsettBesteberegningFødendeKvinneOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    FastsettBesteberegningFødendeKvinneOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBesteberegningFødendeKvinneOppdaterer(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(BesteberegningFødendeKvinneDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {

        List<Lønnsendring> fastsatteInntekter = new ArrayList<>();
        dto.getBesteberegningAndelListe().forEach(dtoAndel ->
        {
            for (BeregningsgrunnlagPeriode periode : nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
                List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagAndeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
                BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel = finnKorrektAndel(beregningsgrunnlagAndeler, dtoAndel);
                Integer gammelArbeidsinntekt = finnGammelMånedsinntekt(beregningsgrunnlagAndel);
                int årsinntekt = dtoAndel.getInntektPrMnd() * 12;
                if (AktivitetStatus.DAGPENGER.equals(beregningsgrunnlagAndel.getAktivitetStatus())) {
                    BeregningsgrunnlagPrStatusOgAndel.builder(beregningsgrunnlagAndel)
                        .medBesteberegningPrÅr(BigDecimal.valueOf(årsinntekt))
                        .medInntektskategori(dtoAndel.getInntektskategori())
                        .medFastsattAvSaksbehandler(true);
                } else {
                    BeregningsgrunnlagPrStatusOgAndel.builder(beregningsgrunnlagAndel)
                        .medBeregnetPrÅr(BigDecimal.valueOf(årsinntekt))
                        .medInntektskategori(dtoAndel.getInntektskategori())
                        .medFastsattAvSaksbehandler(true);
                }
                if (!fastsatteInntekterInneholderAndel(fastsatteInntekter, beregningsgrunnlagAndel)) {
                    fastsatteInntekter.add(new Lønnsendring(beregningsgrunnlagAndel, gammelArbeidsinntekt, dtoAndel.getInntektPrMnd()));
                }
            }
        });
        lagHistorikkinnslag(fastsatteInntekter, behandling);
    }

    private boolean fastsatteInntekterInneholderAndel(List<Lønnsendring> fastsatteInntekter, BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel) {
        return fastsatteInntekter
            .stream()
            .anyMatch(fastsattInntekt -> fastsattInntekt.getAndel().getAndelsnr().equals(beregningsgrunnlagAndel.getAndelsnr()));
    }

    private Integer finnGammelMånedsinntekt(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel) {
        if (beregningsgrunnlagAndel.getBesteberegningPrÅr() == null) {
            return null;
        }
        return beregningsgrunnlagAndel.getBesteberegningPrÅr().intValue() / 12;
    }

    private BeregningsgrunnlagPrStatusOgAndel finnKorrektAndel(List<BeregningsgrunnlagPrStatusOgAndel> bgAndeler, BesteberegningFødendeKvinneAndelDto dtoAndel) {
        return bgAndeler.stream()
            .filter(bgAndel -> dtoAndel.getAndelsnr().equals(bgAndel.getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Fant ikke andel for andelsnr " + dtoAndel.getAndelsnr()));
    }

    private void lagHistorikkinnslag(List<Lønnsendring> fastsatteInntekter, Behandling behandling) {
        HistorikkInnslagTekstBuilder tekstBuilder = historikkAdapter.tekstBuilder();
        boolean erSkjermlenkeSatt = tekstBuilder.getHistorikkinnslagDeler().stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            tekstBuilder.medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        fastsatteInntekter.forEach(fastsattInntekt -> {
            if (fastsattInntekt.getNyArbeidsinntekt() != null && !fastsattInntekt.getNyArbeidsinntekt().equals(fastsattInntekt.getGammelArbeidsinntekt())) {
                BeregningsgrunnlagPrStatusOgAndel andel = fastsattInntekt.getAndel();
                if (AktivitetStatus.FRILANSER.equals(andel.getAktivitetStatus())) {
                    tekstBuilder.medEndretFelt(HistorikkEndretFeltType.FRILANS_INNTEKT, fastsattInntekt.getGammelArbeidsinntekt(), fastsattInntekt.getNyArbeidsinntekt());
                } else if (AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getAktivitetStatus())) {
                    tekstBuilder.medEndretFelt(HistorikkEndretFeltType.BRUTTO_NAERINGSINNTEKT, fastsattInntekt.getGammelArbeidsinntekt(), fastsattInntekt.getNyArbeidsinntekt());
                } else if (AktivitetStatus.DAGPENGER.equals(andel.getAktivitetStatus())) {
                    tekstBuilder.medEndretFelt(HistorikkEndretFeltType.DAGPENGER_INNTEKT, fastsattInntekt.getGammelArbeidsinntekt(), fastsattInntekt.getNyArbeidsinntekt());
                } else {
                    String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(andel);
                    tekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, arbeidsforholdInfo, fastsattInntekt.getGammelArbeidsinntekt(), fastsattInntekt.getNyArbeidsinntekt());
                }
            }
        });
    }
}
