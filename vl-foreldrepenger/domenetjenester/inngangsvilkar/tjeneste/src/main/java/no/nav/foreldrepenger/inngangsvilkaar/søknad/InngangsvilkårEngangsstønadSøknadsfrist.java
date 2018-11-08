package no.nav.foreldrepenger.inngangsvilkaar.søknad;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.søknadsfrist.Søknadsfristvilkår;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_3)
public class InngangsvilkårEngangsstønadSøknadsfrist implements Inngangsvilkår {

    private InngangsvilkårOversetter inngangsvilkårOversetter;

    InngangsvilkårEngangsstønadSøknadsfrist() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårEngangsstønadSøknadsfrist(InngangsvilkårOversetter inngangsvilkårOversetter) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        SoeknadsfristvilkarGrunnlag grunnlag = inngangsvilkårOversetter.oversettTilRegelModellSøknad(behandling);
        Evaluation vilkaarResultat = new Søknadsfristvilkår().evaluer(grunnlag);
        return inngangsvilkårOversetter.tilVilkårData(VilkårType.SØKNADSFRISTVILKÅRET, vilkaarResultat, grunnlag);
    }

}
