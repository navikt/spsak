package no.nav.foreldrepenger.fordel.web.app.metrics;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.vedtak.felles.jpa.OracleVersionChecker;

public class MetricRepositoryTest {

    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private OracleVersionChecker versionChecker = new OracleVersionChecker(repositoryRule.getEntityManager());
    private MetricRepository repository = new MetricRepository(repositoryRule.getEntityManager(), versionChecker);

    @Test
    public void skal_hente_antall_prosess_tasks() throws Exception {
        final List<Object[]> list = repository.tellAntallProsessTaskerPerStatus();
        assertThat(list).isNotNull();
    }

    @Test
    public void skal_hente_antall_prosess_tasks_per_type() throws Exception {
        final List<Object[]> list = repository.tellAntallProsessTaskerPerTypeOgStatus();
        assertThat(list).isNotNull();
    }

    @Test
    public void skal_hente_prosesstasks_med_prefix() throws Exception {
        final List<String> list = repository.hentProsessTaskTyperMedPrefixer(Collections.singletonList(""));
        assertThat(list).isNotNull();
    }

    @Test
    public void skal_telle_im() throws Exception {
        final Long res = repository.tellAntallDokumentTypeIdForTaskType(OpprettGSakOppgaveTask.TASKNAME, DokumentTypeId.INNTEKTSMELDING.getKode(), null);
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(0L);
        final Long res2 = repository.tellAntallDokumentTypeIdForTaskType(OpprettGSakOppgaveTask.TASKNAME, DokumentTypeId.INNTEKTSMELDING.getKode(), LocalDate.now().minusDays(5));
        assertThat(res2).isNotNull();
        assertThat(res2).isEqualTo(0L);
    }
}
