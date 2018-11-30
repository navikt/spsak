package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.time.Instant;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe.Entry;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskLifecycleObserver;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskVeto;
import no.nav.vedtak.felles.prosesstask.impl.BasicCdiProsessTaskDispatcher;
import no.nav.vedtak.felles.prosesstask.impl.BasicCdiProsessTaskDispatcher.ProsessTaskHandlerRef;

/**
 * Vetoer kjøring av prosesstasks som tilhører grupper som er senere enn tidligste prosesstaskgruppe for en fagsak.
 *
 * Denne plugges automatisk inn i prosesstask rammeverket (vha. CDI og {@link ProsessTaskLifecycleObserver} interfacet) og kan veto en
 * kjøring av en ProsessTask (denne vil da forsøkes kjøres om igjen om ca. 30 sek default).
 */
@ApplicationScoped
public class HåndterRekkefølgeAvFagsakProsessTaskGrupper implements ProsessTaskLifecycleObserver {
    private static final Logger log = LoggerFactory.getLogger(HåndterRekkefølgeAvFagsakProsessTaskGrupper.class);
    private FagsakProsessTaskRepository repository;
    private ProsessTaskRepository prosessTaskRepository;

    public HåndterRekkefølgeAvFagsakProsessTaskGrupper() {
        // for CDI proxy
    }

    @Inject
    public HåndterRekkefølgeAvFagsakProsessTaskGrupper(FagsakProsessTaskRepository repository, ProsessTaskRepository prosessTaskRepository) {
        this.repository = repository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public ProsessTaskVeto vetoKjøring(ProsessTaskData ptData) {
        Long fagsakId = ptData.getFagsakId();
        if (fagsakId == null) {
            return new ProsessTaskVeto(false, ptData.getId()); // do nothing, er ikke relatert til fagsak/behandling
        }

        Optional<FagsakProsessTask> blokkerendeTask = repository.sjekkTillattKjøreFagsakProsessTask(ptData);
        // dersom blokkerende task er tom, vetoes ikke tasken
        boolean vetoed = blokkerendeTask.isPresent();
        if (vetoed) {
            ProsessTaskData blokker = prosessTaskRepository.finn(blokkerendeTask.get().getProsessTaskId());
            log.info("Vetoer kjøring av prosesstask[{}] av type[{}] for fagsak [{}] , er blokkert av prosesstask[{}] av type[{}] for samme fagsak.",
                ptData.getId(), ptData.getTaskType(), ptData.getFagsakId(), blokker.getId(), blokker.getTaskType());

            return new ProsessTaskVeto(false, ptData.getId(), blokker.getId(), getClass().getSimpleName()
                + " vetoer pga definert rekkefølge i FAGSAK_PROSESS_TASK.GRUPPE_SEKVENSNR. Blir pukket når blokkerende task kjøres FERDIG.");
        }

        return new ProsessTaskVeto(false, ptData.getId()); // do nothing, er ikke relatert til fagsak/behandling
    }

    /** Denne metoden kalles umiddelbart etter at prosesstasks er oppretttet. En gruppe kan også bestå av 1 enkel task. */
    @Override
    public void opprettetProsessTaskGruppe(ProsessTaskGruppe gruppe) {

        Long gruppeSekvensNr = getGruppeSekvensNr();

        BasicCdiProsessTaskDispatcher cdiDispatcher = new BasicCdiProsessTaskDispatcher();

        for (Entry entry : gruppe.getTasks()) {

            ProsessTaskData task = entry.getTask();
            if (task.getFagsakId() == null) {
                // ikke interessant her, move along
                continue;
            }

            try (ProsessTaskHandlerRef handler = cdiDispatcher.findHandler(task)) {
                if (!handler.getBean().getClass().isAnnotationPresent(FagsakProsesstaskRekkefølge.class)) {
                    // error handling
                    throw new UnsupportedOperationException(handler.getClass().getSimpleName() + " må være annotert med "
                        + FagsakProsesstaskRekkefølge.class.getSimpleName() + " for å kobles til en Fagsak");
                }
                FagsakProsesstaskRekkefølge rekkefølge = handler.getBean().getClass().getAnnotation(FagsakProsesstaskRekkefølge.class);
                Long sekvensNr = rekkefølge.gruppeSekvens() ? gruppeSekvensNr : null;
                repository.lagre(new FagsakProsessTask(task.getFagsakId(), task.getId(), task.getBehandlingId(), sekvensNr));
            }
        }
    }

    /**
     * Rekkefølge av grupper. Bruker tidsstempel for enkelt skyld inntil videre.
     * Ellers må vi ha bokholderi på sekvens for en gruppe på en gitt fagsak dersom det skal være absolutt mulig å opprette grupper i rekkefølge
     * på samme fagsak i samme millisek.
     */
    protected Long getGruppeSekvensNr() {
        Long gruppeSekvensNr = Instant.now().toEpochMilli();
        return gruppeSekvensNr;
    }

}
