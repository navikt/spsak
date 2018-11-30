package no.nav.foreldrepenger.behandlingslager.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;


public class KodeverkTabellRepositoryImplTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private KodeverkTabellRepository repository = new KodeverkTabellRepositoryImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_hente_2_vurder_årsaker() throws Exception {
        Set<VurderÅrsak> resultat = repository.finnVurderÅrsaker(Arrays.asList(VurderÅrsak.ANNET.getKode(), VurderÅrsak.FEIL_FAKTA.getKode()));
        assertThat(resultat).hasSize(2);
    }

    @Test
    public void skal_hente_1_vurder_årsaker() throws Exception {
        Set<VurderÅrsak> resultat = repository.finnVurderÅrsaker(Collections.singleton(VurderÅrsak.ANNET.getKode()));
        assertThat(resultat).hasSize(1);
        assertThat(resultat.iterator().next().getKode()).isEqualTo(VurderÅrsak.ANNET.getKode());
    }

    @Test
    public void skal_hente_0_vurder_årsaker_ved_tom_input() throws Exception {
        Set<VurderÅrsak> resultat = repository.finnVurderÅrsaker(Collections.emptySet());
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_hente_0_vurder_årsaker_ved_ukjent_kode() throws Exception {
        Set<VurderÅrsak> resultat = repository.finnVurderÅrsaker(Collections.singleton("foobar-asfaf"));
        assertThat(resultat).isEmpty();
    }
}
