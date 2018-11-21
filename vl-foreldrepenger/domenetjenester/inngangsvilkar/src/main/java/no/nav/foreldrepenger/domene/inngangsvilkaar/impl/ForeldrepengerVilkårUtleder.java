package no.nav.foreldrepenger.domene.inngangsvilkaar.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.BEREGNINGSGRUNNLAGVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSPERIODEVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OPPTJENINGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.domene.inngangsvilkaar.impl.UtledeteVilkår.forAvklartRelasjonsvilkårTilBarn;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

@ApplicationScoped
public class ForeldrepengerVilkårUtleder implements VilkårUtleder {

    private static  final List<VilkårType> STANDARDVILKÅR = asList(
        MEDLEMSKAPSVILKÅRET,
        SØKERSOPPLYSNINGSPLIKT,
        OPPTJENINGSPERIODEVILKÅR,
        OPPTJENINGSVILKÅRET,
        BEREGNINGSGRUNNLAGVILKÅR);

    public ForeldrepengerVilkårUtleder() {
        // TODO midlertidig instansiering for å kunne ha forskjellige VilkårUtledere for FP og ES
    }

    private static UtledeteVilkår finnVilkår(Behandling behandling) {

        return forAvklartRelasjonsvilkårTilBarn(STANDARDVILKÅR);
    }

    @Override
    public UtledeteVilkår utledVilkår(Behandling behandling) {
        return finnVilkår(behandling);
    }
}
