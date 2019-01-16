package no.nav.foreldrepenger.behandling.revurdering;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;

/**
 * Sjekk om revurdering endrer utfall.
 */
@ApplicationScoped
public class RevurderingEndring { // NO_UCD (test only)

    static final String UTVIKLERFEIL_INGEN_ENDRING_SAMMEN = "Utviklerfeil: Det skal ikke være mulig å ha INGEN_ENDRING sammen med andre konsekvenser. BehandlingId: ";

    RevurderingEndring() {
        // for CDI proxy
    }

    /**
         * Tjeneste som vurderer om revurderingen har endret utrfall i forhold til original behandling
         * @param behandling
         * @return
         */
    public boolean erRevurderingMedUendretUtfall(Behandling behandling, Behandlingsresultat behandlingsresultat) {
        if (!BehandlingType.REVURDERING.equals(behandling.getType())) {
            return false;
        }
        List<KonsekvensForYtelsen> konsekvenserForYtelsen = behandlingsresultat.getKonsekvenserForYtelsen();
        boolean ingenKonsekvensForYtelsen = konsekvenserForYtelsen.contains(KonsekvensForYtelsen.INGEN_ENDRING);
        if (ingenKonsekvensForYtelsen && konsekvenserForYtelsen.size() > 1) {
            throw new IllegalStateException(UTVIKLERFEIL_INGEN_ENDRING_SAMMEN + behandling.getId());
        }
        return ingenKonsekvensForYtelsen;
    }
}
