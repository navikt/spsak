package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class RevurderingVarslingÅrsakTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Test
    public void skal_hente_alle_årsaker_til_revurderingstyper() {
        List<RevurderingVarslingÅrsak> alleAarsaker = repository.hentAlle(RevurderingVarslingÅrsak.class);

        assertThat(alleAarsaker).isNotEmpty();
        assertThat(alleAarsaker).contains(RevurderingVarslingÅrsak.ANNET);
    }
}
