package no.nav.foreldrepenger.domene.inngangsvilkaar.søknad;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.søknadsfrist.Søknadsfristvilkår;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_3)
public class InngangsvilkårSøknadsfrist implements Inngangsvilkår {

    private InngangsvilkårOversetter inngangsvilkårOversetter;

    InngangsvilkårSøknadsfrist() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårSøknadsfrist(InngangsvilkårOversetter inngangsvilkårOversetter) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        SoeknadsfristvilkarGrunnlag grunnlag = inngangsvilkårOversetter.oversettTilRegelModellSøknadsfrist(behandling);
        Evaluation vilkaarResultat = new Søknadsfristvilkår().evaluer(grunnlag);
        return inngangsvilkårOversetter.tilVilkårData(VilkårType.SØKNADSFRISTVILKÅRET, vilkaarResultat, grunnlag);
    }

}
