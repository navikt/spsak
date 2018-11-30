package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderATogFLiSammeOrganisasjonDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
public class FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettMånedsinntektATogFLiSammeOrganisasjonOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                                   HistorikkTjenesteAdapter historikkAdapter,
                                                                   ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(VurderATogFLiSammeOrganisasjonDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
            .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker() || bpsa.getAktivitetStatus().erFrilanser())
            .collect(Collectors.toList());

        List<Lønnsendring> fastsatteInntekter = new ArrayList<>();
        dto.getVurderATogFLiSammeOrganisasjonAndelListe().forEach(dtoAndel ->
        {
            BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel = finnKorrektAndel(arbeidstakerAndeler, dtoAndel);
            Integer gammelArbeidsinntekt = finnGammelMånedsinntekt(beregningsgrunnlagAndel);
            int årsinntekt = dtoAndel.getArbeidsinntekt() * 12;
            BeregningsgrunnlagPrStatusOgAndel.builder(beregningsgrunnlagAndel)
                .medBeregnetPrÅr(BigDecimal.valueOf(årsinntekt))
                .medFastsattAvSaksbehandler(true);
            fastsatteInntekter.add(new Lønnsendring(beregningsgrunnlagAndel, gammelArbeidsinntekt, dtoAndel.getArbeidsinntekt()));
        });

        lagHistorikkinnslag(fastsatteInntekter, behandling);
    }

    private Integer finnGammelMånedsinntekt(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel) {
        if (beregningsgrunnlagAndel.getBeregnetPrÅr() == null) {
            return null;
        }
        return beregningsgrunnlagAndel.getBeregnetPrÅr().intValue() / 12;
    }

    private BeregningsgrunnlagPrStatusOgAndel finnKorrektAndel(List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler, VurderATogFLiSammeOrganisasjonAndelDto dtoAndel) {
        return arbeidstakerAndeler.stream()
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
                } else {
                    String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(andel);
                    tekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, arbeidsforholdInfo, fastsattInntekt.getGammelArbeidsinntekt(), fastsattInntekt.getNyArbeidsinntekt());
                }
            }
        });
    }
}
