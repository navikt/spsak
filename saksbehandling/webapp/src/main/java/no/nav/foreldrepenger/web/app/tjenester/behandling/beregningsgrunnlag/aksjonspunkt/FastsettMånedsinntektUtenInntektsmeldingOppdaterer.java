package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsettMånedsinntektUtenInntektsmeldingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.VurderLønnsendringAndelDto;

@ApplicationScoped
public class FastsettMånedsinntektUtenInntektsmeldingOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;
    FastsettMånedsinntektUtenInntektsmeldingOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettMånedsinntektUtenInntektsmeldingOppdaterer(HistorikkTjenesteAdapter historikkAdapter,
                                                              ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.arbeidsgiverHistorikkinnslagTjeneste = arbeidsgiverHistorikkinnslagTjeneste;
    }

    public void oppdater(FastsettMånedsinntektUtenInntektsmeldingDto dto, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .map(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPrStatusOgAndelList).flatMap(Collection::stream)
            .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
            .collect(Collectors.toList());

        List<Lønnsendring> lønnsendringer = new ArrayList<>();
        dto.getVurderLønnsendringAndelListe().forEach(dtoAndel ->
        {
            BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel = finnKorrektAndel(arbeidstakerAndeler, dtoAndel);
            Integer gammelArbeidsinntekt = finnGammelMånedsinntekt(beregningsgrunnlagAndel);
            Lønnsendring lønnsendring = new Lønnsendring(beregningsgrunnlagAndel, gammelArbeidsinntekt, dtoAndel.getArbeidsinntekt());
            lønnsendringer.add(lønnsendring);
            int årsinntekt = dtoAndel.getArbeidsinntekt() * 12;
            BeregningsgrunnlagPrStatusOgAndel.builder(beregningsgrunnlagAndel)
                .medBeregnetPrÅr(BigDecimal.valueOf(årsinntekt));
        });

        lagHistorikkinnslag(lønnsendringer);
    }

    private Integer finnGammelMånedsinntekt(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagAndel) {
        if (beregningsgrunnlagAndel.getBeregnetPrÅr() == null) {
            return null;
        }
        return beregningsgrunnlagAndel.getBeregnetPrÅr().intValue() / 12;
    }

    private BeregningsgrunnlagPrStatusOgAndel finnKorrektAndel(List<BeregningsgrunnlagPrStatusOgAndel> arbeidstakerAndeler, VurderLønnsendringAndelDto dtoAndel) {
        return arbeidstakerAndeler.stream()
            .filter(bgAndel -> dtoAndel.getAndelsnr().equals(bgAndel.getAndelsnr()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Fant ikke andel for andelsnr " + dtoAndel.getAndelsnr()));
    }

    private void lagHistorikkinnslag(List<Lønnsendring> lønnsendringer) {
        HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = historikkAdapter.tekstBuilder();
        lønnsendringer.forEach(lønnsendring -> {
            if (lønnsendring.getNyArbeidsinntekt() != null && !lønnsendring.getNyArbeidsinntekt().equals(lønnsendring.getGammelArbeidsinntekt())) {
                BeregningsgrunnlagPrStatusOgAndel andel = lønnsendring.getAndel();
                String arbeidsforholdInfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(andel);
                historikkInnslagTekstBuilder.medEndretFelt(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD, arbeidsforholdInfo, lønnsendring.getGammelArbeidsinntekt(), lønnsendring.getNyArbeidsinntekt());
            }
        });
    }
}
