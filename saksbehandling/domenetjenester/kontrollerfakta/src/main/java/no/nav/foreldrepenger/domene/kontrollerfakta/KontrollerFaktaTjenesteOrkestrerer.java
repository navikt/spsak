package no.nav.foreldrepenger.domene.kontrollerfakta;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@Dependent
public class KontrollerFaktaTjenesteOrkestrerer {

    private KontrollerFaktaTjenesteProvider kontrollerFaktaTjenesteProvider;

    KontrollerFaktaTjenesteOrkestrerer() {
        // For CDI proxy
    }

    @Inject
    public KontrollerFaktaTjenesteOrkestrerer(KontrollerFaktaTjenesteProvider kontrollerFaktaTjenesteProvider) {
        this.kontrollerFaktaTjenesteProvider = kontrollerFaktaTjenesteProvider;
    }

    // Orkestrerer aksjonspunktene for kontroll av fakta som utføres etter et startpunkt
    // Dersom ingen spesifikk KontrollerFaktaTjeneste er angitt for startpunktet, så utføres generell kontroll av fakta
    // (Det er forventet at protokoll for KontrollerFaktaTjeneste vil evolvere i senere leveranser)
    public List<AksjonspunktResultat> utledAksjonspunkterTilHøyreForStartpunkt(Behandling behandling, StartpunktType startpunkt) {
        List<AksjonspunktResultat> startpunktSpesfikkeApForKontrollAvFakta = kontrollerFaktaTjenesteProvider.finnKontrollerFaktaTjenesteFor(behandling, startpunkt)
            .map(tjeneste -> tjeneste.utledAksjonspunkterTilHøyreForStartpunkt(behandling.getId(), startpunkt))
            .orElse(Collections.emptyList());
        if (!startpunktSpesfikkeApForKontrollAvFakta.isEmpty()) {
            // Disse må utføres før de generelle kontrollene
            return startpunktSpesfikkeApForKontrollAvFakta;
        }

        List<AksjonspunktResultat> generelleApForKontrollAvFakta = kontrollerFaktaTjenesteProvider.finnKontrollerFaktaTjenesteFor(behandling)
            .utledAksjonspunkterTilHøyreForStartpunkt(behandling.getId(), startpunkt);
        return generelleApForKontrollAvFakta;
    }
}
