package no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import javax.persistence.PersistenceException;

import org.assertj.core.api.Assertions;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.modig.core.test.LogSniffer;

public class HendelsemottakRepositoryImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    private HendelsemottakRepositoryImpl repo = new HendelsemottakRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_si_at_hendeles_er_ny_når_den_ikke_er_registret() throws Exception {
        assertThat(repo.hendelseErNy("erstatter")).isTrue();
    }

    @Test
    public void skal_lagre_hendelse_og_sjekke_om_finnes() throws Exception {
        repo.registrerMottattHendelse("erstatter");
        assertThat(repo.hendelseErNy("erstatter")).isFalse();
    }

    @Test
    public void skal_ikke_kunne_lagre_to_like_hendelser() throws Exception {
        repo.registrerMottattHendelse("erstatter");

        try {
            repo.registrerMottattHendelse("erstatter");
            fail("Skal få exception");
        } catch (PersistenceException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(ConstraintViolationException.class);
            logSniffer.assertHasWarnMessage("SQL Error: 1, SQLState: 23000");
            logSniffer.assertHasErrorMessage("ORA-00001: unique constraint ");
        }

    }


}
