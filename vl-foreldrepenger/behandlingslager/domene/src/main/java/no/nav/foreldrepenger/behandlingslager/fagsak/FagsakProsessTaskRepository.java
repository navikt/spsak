package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.List;
import java.util.Optional;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;

/** Repository for å håndtere kobling mellom Fagsak (og Behandling) mot Prosess Tasks. */
public interface FagsakProsessTaskRepository {

    String lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(Long fagsakId, Long behandlingId, ProsessTaskGruppe gruppe);

    List<ProsessTaskData> sjekkStatusProsessTasks(Long fagsakId, Long behandlingId, String gruppe);

    Optional<FagsakProsessTask> sjekkTillattKjøreFagsakProsessTask(ProsessTaskData prosessTaskData);

    void lagre(FagsakProsessTask fagsakProsessTask);

}
