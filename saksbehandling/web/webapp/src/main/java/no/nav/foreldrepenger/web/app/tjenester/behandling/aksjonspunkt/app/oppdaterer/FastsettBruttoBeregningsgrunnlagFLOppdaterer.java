package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettMånedsinntektFLDto;

@ApplicationScoped
public class FastsettBruttoBeregningsgrunnlagFLOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    FastsettBruttoBeregningsgrunnlagFLOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FastsettBruttoBeregningsgrunnlagFLOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
    }

    public void oppdater(FastsettMånedsinntektFLDto dto, Behandling behandling, Beregningsgrunnlag nyttGrunnlag) {
        Integer frilansinntekt = dto.getMaanedsinntekt();
        BigDecimal årsinntektFL = BigDecimal.valueOf(frilansinntekt).multiply(BigDecimal.valueOf(12));
        BigDecimal opprinneligFrilansinntekt = null;
        List<BeregningsgrunnlagPeriode> bgPerioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        for (BeregningsgrunnlagPeriode bgPeriode : bgPerioder) {
            BeregningsgrunnlagPrStatusOgAndel bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Mangler BeregningsgrunnlagPrStatusOgAndel[FRILANS] for behandling " + behandling.getId()));
            opprinneligFrilansinntekt = bgAndel.getBeregnetPrÅr();
            BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                .medBeregnetPrÅr(årsinntektFL)
                .medFastsattAvSaksbehandler(true)
                .build(bgPeriode);
        }
        lagHistorikkInnslag(dto, opprinneligFrilansinntekt, behandling);
    }

    private void lagHistorikkInnslag(FastsettMånedsinntektFLDto dto, BigDecimal opprinneligFrilansinntekt, Behandling behandling) {
        Integer opprinneligInntektInt = opprinneligFrilansinntekt == null ? null : opprinneligFrilansinntekt.intValue();
        oppdaterVedEndretVerdi(HistorikkEndretFeltType.FRILANS_INNTEKT, dto, opprinneligInntektInt);
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
    }

    private void oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, FastsettMånedsinntektFLDto dto, Integer opprinneligFrilansinntekt) {
        if (opprinneligFrilansinntekt != null && !opprinneligFrilansinntekt.equals(dto.getMaanedsinntekt())) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, opprinneligFrilansinntekt, dto.getMaanedsinntekt());
        } else {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, null, dto.getMaanedsinntekt());
        }
    }
}
