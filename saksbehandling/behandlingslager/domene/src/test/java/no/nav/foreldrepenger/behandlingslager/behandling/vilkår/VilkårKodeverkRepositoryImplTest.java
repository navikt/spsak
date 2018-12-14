package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class VilkårKodeverkRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private VilkårKodeverkRepository repo = new VilkårKodeverkRepositoryImpl(repoRule.getEntityManager(), kodeverkRepository);


    @Test
    public void skal_verifisere_kodeverk_som_mottas_fra_regelmotor() {
        assertThat(repo.finnVilkårResultatType("INNVILGET")).isEqualTo(VilkårResultatType.INNVILGET);
        assertThat(repo.finnVilkårUtfallType("OPPFYLT")).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(repo.finnVilkårUtfallMerknad("1001")).isEqualTo(VilkårUtfallMerknad.VM_1001);
        assertThat(repo.finnAvslagÅrsakListe(VilkårType.FP_VK_23)).contains(Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING);
    }

    @Test
    public void test_finn_vilkårtype_fra_avslagårsak() {
        assertThat(repo.finnVilkårTypeListe(Avslagsårsak.SØKER_ER_UTVANDRET.getKode())).contains(VilkårType.MEDLEMSKAPSVILKÅRET);
    }

    @Test
    public void skal_hente_alle_avslagsårsaker_gruppert_på_vilkårstype() {
        Map<VilkårType, List<Avslagsårsak>> map = repo.finnAvslagårsakerGruppertPåVilkårType();
        assertThat(map.get(VilkårType.SØKERSOPPLYSNINGSPLIKT)).containsOnly(Avslagsårsak.MANGLENDE_DOKUMENTASJON);
        assertThat(map.get(VilkårType.OPPTJENINGSPERIODEVILKÅR))
            .containsOnly(Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING);
    }
}
