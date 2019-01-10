package no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.BasicBehandlingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class OpptjeningRepositoryTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    private final EntityManager em = repoRule.getEntityManager();
    private final BasicBehandlingBuilder basicBehandlingBuilder = new BasicBehandlingBuilder(repoRule.getEntityManager());
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(em);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final OpptjeningRepository opptjeningRepository = new OpptjeningRepositoryImpl(em, behandlingRepository, repositoryProvider.getKodeverkRepository());

    @Test
    public void skal_lagre_opptjeningsperiode() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Behandling behandling = basicBehandlingBuilder.opprettOgLagreFørstegangssøknad(FagsakYtelseType.FORELDREPENGER);

        @SuppressWarnings("unused")
        VilkårResultat vilkårResultat = basicBehandlingBuilder.leggTilTomtVilkårResultat(behandling);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());

        Opptjening opptjeningsperiode = opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, today, tomorrow);

        assertThat(opptjeningsperiode.getFom()).isEqualTo(today);
        assertThat(opptjeningsperiode.getTom()).isEqualTo(tomorrow);

        assertThat(opptjeningsperiode.getOpptjeningAktivitet()).isEmpty();
        assertThat(opptjeningsperiode.getOpptjentPeriode()).isNull();

        Opptjening funnet = opptjeningRepository.finnOpptjening(behandlingsresultat).orElseThrow(IllegalArgumentException::new);

        assertThat(funnet).isEqualTo(opptjeningsperiode);
    }


}
