package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.math.BigDecimal;
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
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderNyoppstartetFLDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
public class VurderNyoppstartetFLOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;


    VurderNyoppstartetFLOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderNyoppstartetFLOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                          HistorikkTjenesteAdapter historikkAdapter) {
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    public void oppdater(VurderNyoppstartetFLDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        if (dto.erErNyoppstartetFL()) {
            BeregningsgrunnlagPeriode periode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
            BeregningsgrunnlagPrStatusOgAndel bgAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bpsa -> bpsa.getAktivitetStatus().erFrilanser())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kunne ikke finne BeregningsgrunnlagPrStatusOgAndel for FRILANSER (eller FL i kombinasjon)"));

            Boolean opprinneligErNyoppstartetFLVerdi = bgAndel.getFastsattAvSaksbehandler();
            BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel).medFastsattAvSaksbehandler(dto.erErNyoppstartetFL()).build(periode);
            lagHistorikkInnslag(dto, opprinneligErNyoppstartetFLVerdi, behandling);
        } else {
            List<BeregningsgrunnlagPeriode> perioder = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder();
            Boolean opprinneligErNyoppstartetFLVerdi = null;
            for (BeregningsgrunnlagPeriode bgPeriode : perioder) {
                BeregningsgrunnlagPrStatusOgAndel bgAndel = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(bpsa -> AktivitetStatus.FRILANSER.equals(bpsa.getAktivitetStatus()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Kunne ikke finne BeregningsgrunnlagPrStatusOgAndel for FRILANSER"));
                BigDecimal beregnetPrÅr = bgAndel.getBeregnetPrÅr();
                opprinneligErNyoppstartetFLVerdi = bgAndel.getFastsattAvSaksbehandler();
                if (beregnetPrÅr != null || !(dto.erErNyoppstartetFL().equals(opprinneligErNyoppstartetFLVerdi))) {
                    BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel)
                        .medBeregnetPrÅr(null)
                        .medFastsattAvSaksbehandler(dto.erErNyoppstartetFL())
                        .build(bgPeriode);
                }
            }
            lagHistorikkInnslag(dto, opprinneligErNyoppstartetFLVerdi, behandling);
        }
    }

    private void lagHistorikkInnslag(VurderNyoppstartetFLDto dto, Boolean opprinneligErNyoppstartetFLVerdi, Behandling behandling) {
        oppdaterVedEndretVerdi(HistorikkEndretFeltType.FRILANSVIRKSOMHET, dto, opprinneligErNyoppstartetFLVerdi);
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean erNyoppstartet) {
        if (erNyoppstartet == null) {
            return null;
        }
        return erNyoppstartet ? HistorikkEndretFeltVerdiType.NYOPPSTARTET : HistorikkEndretFeltVerdiType.IKKE_NYOPPSTARTET;
    }

    private void oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, VurderNyoppstartetFLDto dto,
                                        Boolean opprinneligNyoppstartetFLVerdi) {
        HistorikkEndretFeltVerdiType opprinneligVerdi = konvertBooleanTilFaktaEndretVerdiType(opprinneligNyoppstartetFLVerdi);
        HistorikkEndretFeltVerdiType nyVerdi = konvertBooleanTilFaktaEndretVerdiType(dto.erErNyoppstartetFL());
        if(opprinneligVerdi != nyVerdi) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, opprinneligVerdi, nyVerdi);
        }
    }
}
