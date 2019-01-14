package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

/**
 * Lager historikk innslag for endringer på felt, og setter Aksjonspunkt til totrinnskontroll hvis endret.
 */
class HistorikkAksjonspunktAdapter {

    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private Behandling behandling;

    HistorikkAksjonspunktAdapter(Behandling behandling, HistorikkTjenesteAdapter historikkTjenesteAdapter, AksjonspunktRepository aksjonspunktRepository) {
        this.behandling = behandling;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    void håndterAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon, Vilkår vilkår, Boolean erVilkarOk, String begrunnelse, HistorikkEndretFeltType historikkEndretFeltType) {
        boolean erEndret = oppdaterVedEndretVerdi(historikkEndretFeltType, vilkår.getGjeldendeVilkårUtfall(), erVilkarOk
            ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT);

        if (!erEndret) {
            historikkTjenesteAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, null,
                erVilkarOk ? HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT : HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT);
        }

        boolean erBegrunnelseForAksjonspunktEndret = aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling,
            aksjonspunktDefinisjon, begrunnelse);
        historikkTjenesteAdapter.tekstBuilder()
            .medBegrunnelse(begrunnelse, erBegrunnelseForAksjonspunktEndret)
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        if (erEndret) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }
    }

    private boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType feltkode, VilkårUtfallType original, VilkårUtfallType bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkTjenesteAdapter.tekstBuilder().medEndretFelt(feltkode, original, bekreftet);
            return true;
        }
        return false;
    }


}
