package no.nav.foreldrepenger.behandlingslager.behandling.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BasicBehandlingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class OpptjeningRepositoryTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private final EntityManager em = repoRule.getEntityManager();
    private final BasicBehandlingBuilder basicBehandlingBuilder = new BasicBehandlingBuilder(repoRule.getEntityManager());
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(em);
    private final OpptjeningRepository opptjeningRepository = new OpptjeningRepositoryImpl(em, repositoryProvider.getBehandlingRepository(), repositoryProvider.getKodeverkRepository());

    @Test
    public void skal_lagre_opptjeningsperiode() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Behandling behandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(FagsakYtelseType.ENGANGSTØNAD);

        @SuppressWarnings("unused")
        VilkårResultat vilkårResultat = basicBehandlingBuilder.leggTilTomtVilkårResultat(behandling);

        Opptjening opptjeningsperiode = opptjeningRepository.lagreOpptjeningsperiode(behandling, today, tomorrow);

        assertThat(opptjeningsperiode.getFom()).isEqualTo(today);
        assertThat(opptjeningsperiode.getTom()).isEqualTo(tomorrow);

        assertThat(opptjeningsperiode.getOpptjeningAktivitet()).isEmpty();
        assertThat(opptjeningsperiode.getOpptjentPeriode()).isNull();

        Opptjening funnet = opptjeningRepository.finnOpptjening(behandling).orElseThrow(IllegalArgumentException::new);

        assertThat(funnet).isEqualTo(opptjeningsperiode);
    }


}
