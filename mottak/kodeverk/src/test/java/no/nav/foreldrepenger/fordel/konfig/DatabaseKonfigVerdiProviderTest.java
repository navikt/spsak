package no.nav.foreldrepenger.fordel.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class DatabaseKonfigVerdiProviderTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    @KonfigVerdi(value = "infotrygd.sak.gyldig.periode")
    private Period periode;


    @Inject
    @KonfigVerdi(value = "infotrygd.inntektsmelding.startdato.akseptert.diff")
    private Period infotrygdInntektsmeldingStartdatoAkseptertDiff;

    @Test
    public void skal_ha_injisert_kjent_konfig_verdi_fra_databasen() throws Exception {
        assertThat(periode).isNotNull();
        assertThat(infotrygdInntektsmeldingStartdatoAkseptertDiff).isNotNull();
    }
}
