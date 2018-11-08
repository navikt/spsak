package no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

@ApplicationScoped
public class AksjonspunktUtlederForEngangsstønadAdopsjon implements AksjonspunktUtleder {

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();
        List<AksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();

        // felles for MOR og FAR
        aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON));
        aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN));

        // gjelder bare FAR
        if (RelasjonsRolleType.erFar(fagsak.getRelasjonsRolleType())) {
            aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE));
        }
        return aksjonspunktResultater;
    }
}
