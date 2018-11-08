package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public interface BehandlingskontrollTaskTjeneste {

    /**
     * Opprett og lagre en StartBehandlingTask (kaller prosesserBehandling) i ny ProsessTaskGruppe.
     * Forutsetter initialtilstand. Returnerer gruppe.
     */
    String opprettStartBehandlingTask(Long fagsakId, Long behandlingId, AktørId aktør);

    /**
     * Kjør prosess asynkront (i egen prosess task) videre.
     */
    String opprettFortsettBehandlingTaskNesteSekvens(ProsessTaskData prosessTaskData);

    String opprettFortsettBehandlingTask(Long fagsakId, Long behandlingId, AktørId aktør, Optional<AksjonspunktDefinisjon> autopunktUtført);

    // Vurder behov: String opprettFortsettBehandlingTaskAutopunkterUtført(Long fagsakId, Long behandlingId, AktørId aktør);

    /**
     * Kjør prosess asynkront (i egen prosess task) videre.
     */
    String opprettBehandlingskontrollTask(String taskType, Long fagsakId, Long behandlingId, AktørId aktør);

}
