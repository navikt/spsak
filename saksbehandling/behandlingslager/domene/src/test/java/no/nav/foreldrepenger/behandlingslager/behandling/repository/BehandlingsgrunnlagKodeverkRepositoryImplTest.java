package no.nav.foreldrepenger.behandlingslager.behandling.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class BehandlingsgrunnlagKodeverkRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingsgrunnlagKodeverkRepository repo = new BehandlingsgrunnlagKodeverkRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_verifisere_kodeverk_som_mottas_fra_regelmotor() {
        assertThat(repo.personstatusTyperFortsattBehandling()).contains(PersonstatusType.DØD);
        assertThat(repo.finnHøyestRangertRegion(Collections.singletonList("SWE"))).isEqualTo(Region.NORDEN);
        assertThat(repo.finnRegioner("SWE")).contains(Region.NORDEN);
    }

    @Test
    public void skal_verifisere_region_fra_kodeverk_reølasjon(){
        assertThat(repo.finnRegioner("NOR")).contains(Region.NORDEN);
        assertThat(repo.finnRegioner("DNK")).contains(Region.NORDEN);
        assertThat(repo.finnRegioner("FRA")).contains(Region.EOS);
        assertThat(repo.finnRegioner("HUN")).contains(Region.EOS);
        assertThat(repo.finnRegioner("NZL")).contains(Region.TREDJELANDS_BORGER);
        assertThat(repo.finnRegioner("USA")).contains(Region.TREDJELANDS_BORGER);
    }

    @Test
    public void skal_verifisere_høyest_rangert_region_er_norden(){
        List<String> landkoder = new ArrayList<>();
        landkoder.add("NOR");
        landkoder.add("FRA");
        Region region = repo.finnHøyestRangertRegion(landkoder);
        assertThat(region).isEqualByComparingTo(Region.NORDEN);
    }

    @Test
    public void skal_verifisere_høyest_rangert_region_er_EØS(){
        List<String> landkoder = new ArrayList<>();
        landkoder.add("USA");
        landkoder.add("FRA");
        Region region = repo.finnHøyestRangertRegion(landkoder);
        assertThat(region).isEqualByComparingTo(Region.EOS);
    }

    @Test
    public void skal_verifisere_høyest_rangert_region_er_ikke_norden(){
        List<String> landkoder = new ArrayList<>();
        landkoder.add("FRA");
        Region region = repo.finnHøyestRangertRegion(landkoder);
        assertThat(region).isNotEqualByComparingTo(Region.NORDEN);
    }

    @Test
    public void skal_verifisere_høyest_rangert_region_er_udefinert(){
        List<String> landkoder = new ArrayList<>();
        landkoder.add("USA");
        landkoder.add("CAN");
        Region region = repo.finnHøyestRangertRegion(landkoder);
        assertThat(region).isEqualByComparingTo(Region.TREDJELANDS_BORGER);
    }
}
