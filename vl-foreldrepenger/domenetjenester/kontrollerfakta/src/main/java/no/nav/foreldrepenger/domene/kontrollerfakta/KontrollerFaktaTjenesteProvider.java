package no.nav.foreldrepenger.domene.kontrollerfakta;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@ApplicationScoped
public class KontrollerFaktaTjenesteProvider {

    public KontrollerFaktaTjeneste finnKontrollerFaktaTjenesteFor(Behandling behandling) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();
        String startpunktType = null; // NOSONAR - brukes for å tydeliggjøre uangitt startpunkttype

        Instance<KontrollerFaktaTjeneste> selectedTjeneste = hentKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType, startpunktType);

        if (selectedTjeneste.isAmbiguous()) {
            throw KontrollerFaktaTjenesteFeil.FACTORY.flereImplementasjonerAvKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType).toException();
        } else if (selectedTjeneste.isUnsatisfied()) {
            throw KontrollerFaktaTjenesteFeil.FACTORY.ingenImplementasjonAvKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType).toException();
        }
        return selectedTjeneste.get();
    }

    public Optional<KontrollerFaktaTjeneste> finnKontrollerFaktaTjenesteFor(Behandling behandling, StartpunktType startpunkt) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();
        String startpunktType = startpunkt.getKode();

        Instance<KontrollerFaktaTjeneste> selectedTjeneste = hentKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType, startpunktType);

        if (selectedTjeneste.isAmbiguous()) {
            throw KontrollerFaktaTjenesteFeil.FACTORY.flereImplementasjonerAvKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType).toException();
        } else if (selectedTjeneste.isUnsatisfied()) {
            // Aksepterer ingen match mot startpunkt-annotert kontrolltjenste
            return Optional.empty();
        }
        return Optional.of(selectedTjeneste.get());
    }

    private Instance<KontrollerFaktaTjeneste> hentKontrollerFaktaTjeneste(String fagsakYtelseType, String behandlingType, String startpunkt) {
        // Finn alle som matcher FagsakYtelseType
        Instance<KontrollerFaktaTjeneste> selectedFagsakYtelsesType = CDI.current()
            .select(KontrollerFaktaTjeneste.class, new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakYtelseType));

        //Av de som matcher fagsakytelsestype, finn bean som matcher behandlingstype.
        //Finner vi ikke annotasjon med samme behandlingstype som behandlingen, bruker vi fallback til default annotasjon
        Instance<KontrollerFaktaTjeneste> selectedBehandlingsType = selectedFagsakYtelsesType.select(new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingType));
        if (selectedBehandlingsType.isUnsatisfied()) {
            // Default
            selectedBehandlingsType = selectedFagsakYtelsesType.select(new BehandlingTypeRef.BehandlingTypeRefLiteral());
        }

        //Av de som matcher behandlingstype, finn bean som matcher startpunkttype.
        //Finner vi ikke annotasjon med samme type, bruker vi fallback til default annotasjon
        Instance<KontrollerFaktaTjeneste> selectedStartpunktType;
        if (startpunkt != null) {
            selectedStartpunktType = selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral(startpunkt));
            if (selectedStartpunktType.isAmbiguous()) {
                selectedStartpunktType = selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral());
            }
        } else {
            // Default
            selectedStartpunktType = selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral());
        }

        return selectedStartpunktType;
    }
}
