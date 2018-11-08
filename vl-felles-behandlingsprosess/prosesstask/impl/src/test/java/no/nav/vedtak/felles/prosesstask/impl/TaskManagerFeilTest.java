package no.nav.vedtak.felles.prosesstask.impl;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.exception.JDBCConnectionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.felles.prosesstask.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TaskManagerFeilTest {

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private TaskManagerRepositoryImpl taskManagerRepo;

    @Test
    public void skal_logge_transient_feil_under_polling() throws Exception {
        TaskManager taskManager = new TaskManager(taskManagerRepo) {
            @Override
            protected List<Runnable> pollForAvailableTasks() {
                throw new JDBCConnectionException("NOT AVAILABLE!", null);
            }
        };

        taskManager.new PollAvailableTasks().run();

        logSniffer.assertHasWarnMessage("FP-739415");

    }

    @Test
    public void skal_logge_annen_feil_under_polling() throws Exception {
        TaskManager taskManager = new TaskManager(taskManagerRepo) {
            @Override
            protected List<Runnable> pollForAvailableTasks() {
                throw new RuntimeException("HERE BE DRAGONS!");
            }
        };

        taskManager.new PollAvailableTasks().run();

        logSniffer.assertHasWarnMessage("FP-996896");

    }
}
