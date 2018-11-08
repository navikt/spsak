package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;

/**
 * Signaliserer status på resultat av å kjøre et {@link BehandlingSteg}.
 */
public enum BehandlingStegResultat {

    /**
     * Signaliser at steget er startet, men ikke utført (pågår)
     */
    STARTET,

    /**
     * Signaliser at steget settes på vent. Ingenting pågår mens det står på vent, og det må 'vekkes' opp igjen ved en
     * handling (Saksbehandler), en melding mottas, elleren prosesstask.
     */
    SETT_PÅ_VENT,

    /**
     * Signaliser at steget er ferdig kjørt og del-resultat generert foreligger.
     */
    UTFØRT,

    /**
     * Signaliser at steget er avbrutt og tidligere behandlingssteg skal kjøres på nytt
     */
    TILBAKEFØRT,

    /**
     * Signaliser at steget er ført fremover gjennom overhopp
     */
    FREMOVERFØRT;

    private static final Map<BehandlingStegStatus, BehandlingStegResultat> MAP_STATUS_HANDLING;

    static {
        Map<BehandlingStegStatus, BehandlingStegResultat> map = new LinkedHashMap<>();
        // map.put(BehandlingStegStatus.INNGANG, IKKE DEFINERT); // NOSONAR
        map.put(BehandlingStegStatus.STARTET, BehandlingStegResultat.STARTET);
        map.put(BehandlingStegStatus.VENTER, BehandlingStegResultat.SETT_PÅ_VENT);
        // map.put(BehandlingStegStatus.UTGANG, IKKE DEFINERT); // NOSONAR
        map.put(BehandlingStegStatus.UTFØRT, BehandlingStegResultat.UTFØRT);
        map.put(BehandlingStegStatus.FREMOVERFØRT, BehandlingStegResultat.FREMOVERFØRT);
        map.put(BehandlingStegStatus.TILBAKEFØRT, BehandlingStegResultat.TILBAKEFØRT);

        MAP_STATUS_HANDLING = Collections.unmodifiableMap(map);
    }

    static BehandlingStegStatus mapTilStatus(BehandlingStegResultat behandleStegHandling) {
        Optional<BehandlingStegStatus> findFirst = MAP_STATUS_HANDLING
            .entrySet()
            .stream()
            .filter((entry) -> Objects.equals(entry.getValue(), behandleStegHandling))
            .map(e -> e.getKey())
            .findFirst();

        return findFirst
            .orElseThrow(() -> {
                return new IllegalArgumentException(
                    "Utvikler-feil: ukjent mapping fra " + //$NON-NLS-1$
                        BehandlingStegResultat.class.getSimpleName() + "." //$NON-NLS-1$
                        + behandleStegHandling);
            });

    }
}
