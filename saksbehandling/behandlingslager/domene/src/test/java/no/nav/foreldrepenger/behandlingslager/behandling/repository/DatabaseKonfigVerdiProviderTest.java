package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class DatabaseKonfigVerdiProviderTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    @KonfigVerdi(value="behandling.venter.frist.lengde")
    private Period behandlingFrist;

    @Inject
    @KonfigVerdi(value="relaterte.ytelser.periode.start")
    private Period relaterteYtelserPeriodeStart;

    @Inject
    @KonfigVerdi(value="virtuell.saksbehandler.navn")
    private String virtuellSaksbehandlerNavn;

    @Inject
    @KonfigVerdi(value="vedtak.klagefrist.uker")
    private Integer klageFristUker;

    @Inject
    @KonfigVerdi(value="relaterte.ytelser.vl.periode.start")
    private Period relaterteYtelserVLPeriodeStart;

    @Test
    public void skal_ha_injisert_kjent_konfig_verdi_fra_databasen() throws Exception{
        assertThat(behandlingFrist).isNotNull();
        assertThat(relaterteYtelserPeriodeStart).isNotNull();
        assertThat(virtuellSaksbehandlerNavn).isNotNull();
        assertThat(klageFristUker).isNotNull();
        assertThat(relaterteYtelserVLPeriodeStart).isNotNull();
    }
}
