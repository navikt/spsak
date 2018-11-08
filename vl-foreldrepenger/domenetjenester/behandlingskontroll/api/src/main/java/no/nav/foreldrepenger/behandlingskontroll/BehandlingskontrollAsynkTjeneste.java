package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public interface BehandlingskontrollAsynkTjeneste {

    /**
     * Sjekker om prosess tasks pågår nå for angitt behandling. Returnerer neste utestående tasks per gruppe for status (KLAR, FEILET, VENTER),
     * men ikke FERDIG, SUSPENDERT (unntatt der matcher angitt gruppe)
     * 
     * Hvis gruppe angis sjekkes kun angitt gruppe. Dersom denne er null returneres status for alle åpne grupper (ikke-ferdig) for angitt
     * behandling. Tasks som er {@link ProsessTaskStatus#FERDIG} ignoreres i resultatet når gruppe ikke er angitt.
     */
    Map<String, ProsessTaskData> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe);

    Map<String, ProsessTaskData> sjekkProsessTaskPågår(Long fagsakId, Long behandlingId, String gruppe);

    /**
     * Merge ny gruppe med eksisterende, hvis tidligere gruppe er i gang ignoreres input gruppe her. Hvis tidligere gruppe har feil, overskrives
     * denne (ny skrives, gamle fjernes). For å merge sees det på individuelle tasks inne i gruppen (da gruppe id kan være forskjellig uansett).
     */
    String lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(Long fagsakId, Long behandlingId, ProsessTaskGruppe gruppe);

    /**
     * Kjør prosess asynkront (i egen prosess task) videre.
     * 
     * @return gruppe assignet til prosess task
     */
    String asynkProsesserBehandling(Behandling behandling);

}
