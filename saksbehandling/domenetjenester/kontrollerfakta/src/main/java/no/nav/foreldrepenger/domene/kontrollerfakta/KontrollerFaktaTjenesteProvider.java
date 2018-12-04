package no.nav.foreldrepenger.domene.kontrollerfakta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

/** Provider som slår opp relevante KontrollerFaktaTjeneste basert på en behandling. */
@ApplicationScoped
public class KontrollerFaktaTjenesteProvider {

    private Instance<KontrollerFaktaTjeneste> instans;

    private Map<Set<?>, KontrollerFaktaTjeneste> cachedInstans = new HashMap<>();

    KontrollerFaktaTjenesteProvider() {
        // for CDI proxy
    }

    @Inject
    KontrollerFaktaTjenesteProvider(Instance<KontrollerFaktaTjeneste> instans) {
        this.instans = instans;
    }

    KontrollerFaktaTjeneste finnKontrollerFaktaTjenesteFor(Behandling behandling) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();
        StartpunktType startpunktType = null; // NOSONAR - brukes for å tydeliggjøre uangitt startpunkttype

        Optional<KontrollerFaktaTjeneste> kontrollerFaktaTjeneste = finnKontrollerFaktaTjenesteFor(behandling, startpunktType);

        if (!kontrollerFaktaTjeneste.isPresent()) {
            throw KontrollerFaktaTjenesteFeil.FACTORY.ingenImplementasjonAvKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType).toException();
        }
        return kontrollerFaktaTjeneste.get();
    }

    Optional<KontrollerFaktaTjeneste> finnKontrollerFaktaTjenesteFor(Behandling behandling, StartpunktType startpunkt) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();
        String startpunktType = startpunkt == null ? null : startpunkt.getKode();

        Optional<KontrollerFaktaTjeneste> selectedTjeneste = hentKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType, startpunktType);

        return selectedTjeneste;
    }

    private Optional<KontrollerFaktaTjeneste> hentKontrollerFaktaTjeneste(String fagsakYtelseType, String behandlingType, String startpunkt) {

        var key = Set.of(fagsakYtelseType, behandlingType, startpunkt);

        if (!cachedInstans.containsKey(key)) {
            Instance<KontrollerFaktaTjeneste> nyInstans = nyInstans(fagsakYtelseType, behandlingType, startpunkt);

            if (nyInstans.isAmbiguous()) {
                throw KontrollerFaktaTjenesteFeil.FACTORY.flereImplementasjonerAvKontrollerFaktaTjeneste(fagsakYtelseType, behandlingType).toException();
            } else if (nyInstans.isUnsatisfied()) {
                // Aksepterer ingen match mot startpunkt-annotert kontrolltjenste
                cachedInstans.putIfAbsent(key, null);
            } else {
                cachedInstans.putIfAbsent(key, nyInstans.get());
            }

        }

        return Optional.ofNullable(cachedInstans.get(key));
    }

    private synchronized Instance<KontrollerFaktaTjeneste> nyInstans(String fagsakYtelseType, String behandlingType, String startpunkt) {
        // Finn alle som matcher FagsakYtelseType
        Instance<KontrollerFaktaTjeneste> selectedFagsakYtelsesType = instans.select(new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakYtelseType));

        // Av de som matcher fagsakytelsestype, finn bean som matcher behandlingstype.
        // Finner vi ikke annotasjon med samme behandlingstype som behandlingen, bruker vi fallback til default annotasjon
        Instance<KontrollerFaktaTjeneste> selectedBehandlingsType = selectedFagsakYtelsesType
            .select(new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingType));
        if (selectedBehandlingsType.isUnsatisfied()) {
            // Default
            selectedBehandlingsType = selectedFagsakYtelsesType.select(new BehandlingTypeRef.BehandlingTypeRefLiteral());
        }

        // Av de som matcher behandlingstype, finn bean som matcher startpunkttype.
        // Finner vi ikke annotasjon med samme type, bruker vi fallback til default annotasjon
        Instance<KontrollerFaktaTjeneste> selectedStartpunktType;
        if (startpunkt != null) {
            selectedStartpunktType = selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral(startpunkt));
            if (selectedStartpunktType.isAmbiguous()) {
                selectedStartpunktType = selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral());
            }
        } else {
            // Default
            return selectedBehandlingsType.select(new StartpunktRef.StartpunktRefLiteral());
        }
        return selectedStartpunktType;
    }
}
