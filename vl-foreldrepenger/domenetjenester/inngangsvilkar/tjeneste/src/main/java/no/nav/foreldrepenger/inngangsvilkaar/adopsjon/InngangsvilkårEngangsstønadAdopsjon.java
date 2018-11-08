package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_4)
public class InngangsvilkårEngangsstønadAdopsjon implements Inngangsvilkår {

    private InngangsvilkårOversetter inngangsvilkårOversetter;

    InngangsvilkårEngangsstønadAdopsjon() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårEngangsstønadAdopsjon(InngangsvilkårOversetter inngangsvilkårOversetter) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        AdopsjonsvilkårGrunnlag grunnlag = inngangsvilkårOversetter.oversettTilRegelModellAdopsjon(behandling);

        Evaluation evaluation = new AdopsjonsvilkårEngangsstønad().evaluer(grunnlag);

        return inngangsvilkårOversetter.tilVilkårData(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, evaluation, grunnlag);
    }
}
