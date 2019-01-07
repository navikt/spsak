package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class BehandlingLåsTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();
    private final GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(em);
    private final Saksnummer saksnummer  = new Saksnummer("2");

    private Fagsak fagsak;

    private Behandling behandling;

    @Before
    public void setup() {
        fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).medSaksnummer(saksnummer).build();
        em.persist(fagsak.getNavBruker());
        em.persist(fagsak);
        em.flush();

        behandling = Behandling.forFørstegangssøknad(fagsak).build();
        em.persist(behandling);
        em.flush();
    }

    @Test
    public void skal_finne_behandling_gitt_id() {

        // Act
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        assertThat(lås).isNotNull();

        Behandling resultat = repositoryProvider.getBehandlingRepository().hentBehandling(behandling.getId());
        assertThat(resultat).isNotNull();

        // Assert

        repositoryProvider.getBehandlingRepository().lagre(resultat, lås);
    }

}
