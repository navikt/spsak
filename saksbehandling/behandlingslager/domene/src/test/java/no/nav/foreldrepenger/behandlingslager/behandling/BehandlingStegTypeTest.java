package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingStegTypeTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Test
    public void skal_hente_alle_behandling_stegtyper() throws Exception {
        List<BehandlingStegType> alle = repository.hentAlle(BehandlingStegType.class);

        assertThat(alle).isNotEmpty();

    }
}
