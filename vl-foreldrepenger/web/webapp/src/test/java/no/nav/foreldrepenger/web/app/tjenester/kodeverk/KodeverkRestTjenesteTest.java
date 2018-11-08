package no.nav.foreldrepenger.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() {
        HentKodeverkTjeneste hentKodeverkTjeneste = Mockito.mock(HentKodeverkTjeneste.class);
        Mockito.when(hentKodeverkTjeneste.hentGruppertKodeliste()).thenReturn(getGruppertKodeliste());
        VilkårKodeverkRepository vilkårKodeverkRepository = Mockito.mock(VilkårKodeverkRepository.class);
        Mockito.when(vilkårKodeverkRepository.finnAvslagårsakerGruppertPåVilkårType()).thenReturn(getAvslagsårsaker());

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste(hentKodeverkTjeneste, vilkårKodeverkRepository);
        Map<String, Object> gruppertKodeliste = tjeneste.hentGruppertKodeliste();

        assertThat(gruppertKodeliste.keySet()).containsOnly(FagsakStatus.class.getSimpleName(), KlageMedholdÅrsak.class.getSimpleName(),
                Avslagsårsak.class.getSimpleName());
        assertThat(gruppertKodeliste.get(FagsakStatus.class.getSimpleName()))
                .isEqualTo(Arrays.asList(FagsakStatus.AVSLUTTET, FagsakStatus.OPPRETTET));

        @SuppressWarnings("unchecked")
        Map<String, List<Avslagsårsak>> map = (Map<String, List<Avslagsårsak>>) gruppertKodeliste.get(Avslagsårsak.class.getSimpleName());
        assertThat(map.keySet()).containsOnly(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD.getKode(), VilkårType.MEDLEMSKAPSVILKÅRET.getKode());
        assertThat(map.get(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD.getKode()))
                .isEqualTo(Arrays.asList(Avslagsårsak.SØKT_FOR_SENT, Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O));
    }

    private static Map<String, List<Kodeliste>> getGruppertKodeliste() {
        Map<String, List<Kodeliste>> map = new HashMap<>();
        map.put(FagsakStatus.class.getSimpleName(), Arrays.asList(FagsakStatus.AVSLUTTET, FagsakStatus.OPPRETTET));
        map.put(KlageMedholdÅrsak.class.getSimpleName(), Arrays.asList(KlageMedholdÅrsak.ULIK_VURDERING));
        return map;
    }

    private static Map<VilkårType, List<Avslagsårsak>> getAvslagsårsaker() {
        Map<VilkårType, List<Avslagsårsak>> map = new HashMap<>();
        map.put(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, Arrays.asList(Avslagsårsak.SØKT_FOR_SENT, Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O));
        map.put(VilkårType.MEDLEMSKAPSVILKÅRET, Arrays.asList(Avslagsårsak.SØKER_ER_IKKE_MEDLEM, Avslagsårsak.SØKER_ER_UTVANDRET));
        return map;
    }
}
