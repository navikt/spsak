package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
public class VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer {

    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;

    VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderSelvstendigNæringsdrivendeNyIArbeidslivetOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                                                     HistorikkTjenesteAdapter historikkAdapter) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
    }

    public void oppdater(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto dto, Behandling behandling, Beregningsgrunnlag nyttBeregningsgrunnlag) {
        BeregningsgrunnlagPeriode periode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel bgAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(bpsa -> bpsa.getAktivitetStatus().erSelvstendigNæringsdrivende())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Kunne ikke finne BeregningsgrunnlagPrStatusOgAndel for SELVSTENDIG_NÆRINGSDRIVENDE (eller SN i kombinasjon)"));

        Boolean opprinneligNyIArbeidslivetVerdi = bgAndel.getNyIArbeidslivet();
        BeregningsgrunnlagPrStatusOgAndel.builder(bgAndel).medNyIArbeidslivet(dto.erNyIArbeidslivet()).build(periode);
        lagHistorikkInnslag(dto, opprinneligNyIArbeidslivetVerdi, behandling);
    }

    private void lagHistorikkInnslag(VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto dto, Boolean opprinneligNyIArbeidslivetVerdi, Behandling behandling) {
        oppdaterVedEndretVerdi(HistorikkEndretFeltType.SELVSTENDIG_NÆRINGSDRIVENDE, dto, opprinneligNyIArbeidslivetVerdi);
        List<HistorikkinnslagDel> historikkDeler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        Boolean erSkjermlenkeSatt = historikkDeler.stream().anyMatch(historikkDel -> historikkDel.getSkjermlenke().isPresent());
        if (!erSkjermlenkeSatt) {
            historikkAdapter.tekstBuilder().medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN.getKode()), behandling);
        }
        historikkAdapter.tekstBuilder().ferdigstillHistorikkinnslagDel();
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilFaktaEndretVerdiType(Boolean erNyIArbeidslivet) {
        if (erNyIArbeidslivet == null) {
            return null;
        }
        return erNyIArbeidslivet ? HistorikkEndretFeltVerdiType.NY_I_ARBEIDSLIVET : HistorikkEndretFeltVerdiType.IKKE_NY_I_ARBEIDSLIVET;
    }

    private void oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, VurderSelvstendigNæringsdrivendeNyIArbeidslivetDto dto,
                                           Boolean opprinneligNyIArbeidslivetVerdi) {
        HistorikkEndretFeltVerdiType opprinneligVerdi = konvertBooleanTilFaktaEndretVerdiType(opprinneligNyIArbeidslivetVerdi);
        HistorikkEndretFeltVerdiType nyVerdi = konvertBooleanTilFaktaEndretVerdiType(dto.erNyIArbeidslivet());
        if(opprinneligVerdi != nyVerdi) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, opprinneligVerdi, nyVerdi);
        }
    }


}
