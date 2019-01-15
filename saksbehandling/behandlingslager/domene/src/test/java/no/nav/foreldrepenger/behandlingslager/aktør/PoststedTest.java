package no.nav.foreldrepenger.behandlingslager.aktør;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.geografisk.Poststed;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class PoststedTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_hente_poststed() {
        Poststed postSted = kodeverkRepository.finn(Poststed.class, "0103");

        Assert.assertEquals(postSted.getNavn(), "OSLO");
    }
}
