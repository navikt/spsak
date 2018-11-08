package no.nav.foreldrepenger.økonomistøtte.api;

import java.util.List;

public interface SimulerOppdragApplikasjonTjeneste {
    /**
     * Generer XMLene som skal sendes over til oppdrag for simulering. Det lages en XML per Oppdrag110.
     * Vi har en Oppdrag110-linje per oppdragsmottaker.
     *
     * @param behandlingId behandling.id
     * @param ventendeTaskId TaskId til ventende prosessTask
     * @return En liste med XMLer som kan sendes over til oppdrag
     */
    List<String> simulerOppdrag(Long behandlingId, Long ventendeTaskId);
}
