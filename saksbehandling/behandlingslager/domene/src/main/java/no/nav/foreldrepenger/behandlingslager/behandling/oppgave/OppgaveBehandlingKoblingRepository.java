package no.nav.foreldrepenger.behandlingslager.behandling.oppgave;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;

public interface OppgaveBehandlingKoblingRepository extends BehandlingslagerRepository {

    /**
     * Lagrer kobling til GSAK oppgave for behandling. Sørger for at samtidige oppdateringer på samme Behandling, ikke kan gjøres samtidig.
     *
     * @see BehandlingLås
     */
    Long lagre(OppgaveBehandlingKobling oppgaveBehandlingKobling);

    Optional<OppgaveBehandlingKobling> hentOppgaveBehandlingKobling(String oppgaveId);

    List<OppgaveBehandlingKobling> hentOppgaverRelatertTilBehandling(Long behandlingId);

}
