package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

/**
 * Adapter for å evaluere fødselsvilkåret.
 */
@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_1)
public class InngangsvilkårFødselMor implements Inngangsvilkår {

    private InngangsvilkårOversetter inngangsvilkårOversetter;

    InngangsvilkårFødselMor() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårFødselMor(InngangsvilkårOversetter inngangsvilkårOversetter) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        FødselsvilkårGrunnlag grunnlag = inngangsvilkårOversetter.oversettTilRegelModellFødsel(behandling);

        Evaluation evaluation = new FødselsvilkårMor().evaluer(grunnlag);

        return inngangsvilkårOversetter.tilVilkårData(VilkårType.FØDSELSVILKÅRET_MOR, evaluation, grunnlag);
    }
}
