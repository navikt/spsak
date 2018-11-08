package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.InngangsvilkårTjeneste;
import no.nav.foreldrepenger.inngangsvilkaar.VilkårTypeRef;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InngangsvilkårTjenesteTest {

    @Inject
    InngangsvilkårTjeneste inngangsvilkårTjeneste;

    @Test
    public void skal_slå_opp_inngangsvilkår() throws Exception {
        sjekkVilkårKonfigurasjon(VilkårType.FØDSELSVILKÅRET_MOR);
        sjekkVilkårKonfigurasjon(VilkårType.MEDLEMSKAPSVILKÅRET);
        sjekkVilkårKonfigurasjon(VilkårType.SØKNADSFRISTVILKÅRET);
        sjekkVilkårKonfigurasjon(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD);
        sjekkVilkårKonfigurasjon(VilkårType.OMSORGSVILKÅRET);
        sjekkVilkårKonfigurasjon(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD);
        sjekkVilkårKonfigurasjon(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);
    }

    private void sjekkVilkårKonfigurasjon(VilkårType vilkårType) {
        Inngangsvilkår vilkår = inngangsvilkårTjeneste.finnVilkår(vilkårType);
        assertThat(vilkår).isNotNull();
        assertThat(vilkår).isSameAs(inngangsvilkårTjeneste.finnVilkår(vilkårType));
        assertThat(vilkår.getClass()).hasAnnotation(ApplicationScoped.class);
        assertThat(vilkår.getClass()).hasAnnotation(VilkårTypeRef.class);
    }

}
