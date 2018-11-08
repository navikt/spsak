package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class VilkårTypeTest {

    private static String TEST_STRING =
        "{ \"fagsakYtelseType\" : { \"ES\" : { \"kategori\": \"vilkår\", \"lovreferanse\": \"§ 14-17, 1. ledd\" } " +
            ", \"FP\" : { \"kategori\": \"vilkår\", \"lovreferanse\": \"§ 14-17, 1. ledd\" } } }";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Test
    public void skal_hente_ut_riktig_lovreferanse_basert_på_fagsakYtelseType_engangsstønad() {
        VilkårType vilkårType = new VilkårType() {
            @Override
            protected String getEkstraData() {
                return TEST_STRING;
            }
        };
        assertThat(vilkårType.getLovReferanse(FagsakYtelseType.ENGANGSTØNAD)).isEqualTo("§ 14-17, 1. ledd");
    }


    @Test
    public void skal_hente_ut_riktig_lovreferanse_basert_på_fagsakYtelseType_foreldrepenger() {
        VilkårType vilkårType = new VilkårType() {
            @Override
            protected String getEkstraData() {
                return TEST_STRING;
            }
        };
        assertThat(vilkårType.getLovReferanse(FagsakYtelseType.FORELDREPENGER)).isEqualTo("§ 14-17, 1. ledd");
    }


}
