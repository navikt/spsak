package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

abstract class AbstractOverstyringshåndterer<T extends OverstyringAksjonspunktDto> implements Overstyringshåndterer<T> {

    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    protected AbstractOverstyringshåndterer() {
        // for CDI proxy
    }

    protected AbstractOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
                                            HistorikkTjenesteAdapter historikkAdapter,
                                            AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
    }

    @Override
    public void håndterAksjonspunktForOverstyring(T dto, Behandling behandling) {
        precondition(behandling, dto);
        opprettAksjonspunktForOverstyring(behandling, dto, aksjonspunktDefinisjon);
        lagHistorikkInnslag(behandling, dto);
    }

    /**
     * Valider om precondition for overstyring er møtt. Kaster exception hvis ikke.
     *
     * @param behandling behandling
     * @param dto
     */
    protected void precondition(Behandling behandling, T dto) {
        // all good, do NOTHING.
    }

    protected abstract void lagHistorikkInnslag(Behandling behandling, T dto);

    protected void opprettAksjonspunktForOverstyring(Behandling behandling, OverstyringAksjonspunktDto dto, AksjonspunktDefinisjon apDef) {
        Optional<Aksjonspunkt> eksisterendeAksjonspunkt = behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().equals(apDef))
            .findFirst();
        Aksjonspunkt aksjonspunkt = eksisterendeAksjonspunkt.orElseGet(() -> aksjonspunktRepository.leggTilAksjonspunkt(behandling, apDef));

        if (!aksjonspunkt.erAktivt()) {
            aksjonspunktRepository.reaktiver(aksjonspunkt);
        }
        if (aksjonspunkt.erOpprettet()) {
            aksjonspunktRepository.setTilUtført(aksjonspunkt, dto.getBegrunnelse());
            return;
        }
        if (aksjonspunkt.erAvbrutt()) {
            // Må reåpne avbrutte før de kan settes til utført (kunne ha vært én operasjon i aksjonspunktRepository)
            aksjonspunktRepository.setReåpnet(aksjonspunkt);
            aksjonspunktRepository.setTilUtført(aksjonspunkt, dto.getBegrunnelse());
            return;
        }
    }

    protected void lagHistorikkInnslagForOverstyrtVilkår(String begrunnelse, boolean vilkårOppfylt, String aksjonspunktkode, Behandling behandling) {
        HistorikkEndretFeltVerdiType tilVerdi = vilkårOppfylt ? HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT : HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT;
        HistorikkEndretFeltVerdiType fraVerdi = vilkårOppfylt ? HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT : HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT;

        getHistorikkAdapter().tekstBuilder()
            .medHendelse(HistorikkinnslagType.OVERSTYRT)
            .medBegrunnelse(begrunnelse)
            .medSkjermlenke(aksjonspunktRepository.finnAksjonspunktDefinisjon(aksjonspunktkode), behandling)
            .medEndretFelt(HistorikkEndretFeltType.OVERSTYRT_VURDERING, fraVerdi, tilVerdi);
    }

    protected HistorikkTjenesteAdapter getHistorikkAdapter() {
        return historikkAdapter;
    }

}
