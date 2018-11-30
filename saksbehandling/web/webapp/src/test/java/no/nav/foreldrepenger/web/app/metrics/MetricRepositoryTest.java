package no.nav.foreldrepenger.web.app.metrics;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.OracleVersionChecker;

public class MetricRepositoryTest {

    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private OracleVersionChecker versionChecker = new OracleVersionChecker(repositoryRule.getEntityManager());
    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(repositoryRule.getEntityManager());
    private MetricRepository repository = new MetricRepository(repositoryRule.getEntityManager(), versionChecker, aksjonspunktRepository);

    @Test
    public void skal_hente_ut_ventende_aksjonspunkt() throws Exception {
        final BigDecimal ventendeFoedsler = repository.tellLettereAntallVentendeBehandlinger();

        assertThat(ventendeFoedsler).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_hente_ut_ventende_oppgaver() throws Exception {
        final BigDecimal ventendeOppgaver = repository.tellLettereAntallVentendeOppgaver();

        assertThat(ventendeOppgaver).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_hente_ut_ikke_avsluttet_behandling() throws Exception {
        final BigDecimal ikkeavsluttet = repository.tellLettereAntallBehandlingerSomIkkeHarBlittAvsluttet();

        assertThat(ikkeavsluttet).isEqualTo(BigDecimal.ZERO);
    }

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
}
