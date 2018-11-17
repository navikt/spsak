package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class VilkårKodeverkRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private VilkårKodeverkRepository repo = new VilkårKodeverkRepositoryImpl(repoRule.getEntityManager());


    @Test
    public void skal_verifisere_kodeverk_som_mottas_fra_regelmotor() {
        assertThat(repo.finnVilkårType(VilkårType.FP_VK_1)).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(repo.finnVilkårResultatType("INNVILGET")).isEqualTo(VilkårResultatType.INNVILGET);
        assertThat(repo.finnVilkårUtfallType("OPPFYLT")).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(repo.finnAksjonspunktDefinisjon("5001")).isEqualTo(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        assertThat(repo.finnVilkårUtfallMerknad("1001")).isEqualTo(VilkårUtfallMerknad.VM_1001);
        assertThat(repo.finnAvslagÅrsakListe(VilkårType.FP_VK_5)).contains(Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O);
    }

    @Test
    public void test_finn_vilkårtype_fra_avslagårsak() {
        assertThat(repo.finnVilkårTypeListe(Avslagsårsak.SØKER_ER_UTVANDRET.getKode())).contains(VilkårType.MEDLEMSKAPSVILKÅRET);
    }

    @Test
    public void skal_hente_alle_avslagsårsaker_gruppert_på_vilkårstype() {
        Map<VilkårType, List<Avslagsårsak>> map = repo.finnAvslagårsakerGruppertPåVilkårType();
        assertThat(map.get(VilkårType.SØKERSOPPLYSNINGSPLIKT)).containsOnly(Avslagsårsak.MANGLENDE_DOKUMENTASJON);
        assertThat(map.get(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD))
                .containsOnly(Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_F, Avslagsårsak.OMSORGSOVERTAKELSE_ETTER_56_UKER,
                        Avslagsårsak.IKKE_FORELDREANSVAR_ALENE_ETTER_BARNELOVA,
                        Avslagsårsak.ENGANGSSTØNAD_ER_ALLEREDE_UTBETALT_TIL_FAR_MEDMOR,
                        Avslagsårsak.FORELDREPENGER_ER_ALLEREDE_UTBETALT_TIL_FAR_MEDMOR);
    }
}
