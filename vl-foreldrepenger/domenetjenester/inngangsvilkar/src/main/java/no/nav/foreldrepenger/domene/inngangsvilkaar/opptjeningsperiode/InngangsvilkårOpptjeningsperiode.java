package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjeningsperiode;

import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_21)
public class InngangsvilkårOpptjeningsperiode implements Inngangsvilkår {

    private InngangsvilkårOversetter inngangsvilkårOversetter;
    private Period antallMånederOpptjeningsperiode;

    InngangsvilkårOpptjeningsperiode() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårOpptjeningsperiode(InngangsvilkårOversetter inngangsvilkårOversetter,
                                            @KonfigVerdi(value = "opptjeningsperiode.lengde") Period antallMånederOpptjeningsperiode) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
        this.antallMånederOpptjeningsperiode = antallMånederOpptjeningsperiode;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        OpptjeningsperiodeGrunnlag grunnlag = inngangsvilkårOversetter.oversettTilRegelModellOpptjeningsperiode(behandling);
        grunnlag.setPeriodeLengde(antallMånederOpptjeningsperiode);

        final OpptjeningsPeriode data = new OpptjeningsPeriode();
        Evaluation evaluation = new RegelFastsettOpptjeningsperiode().evaluer(grunnlag, data);

        VilkårData resultat = inngangsvilkårOversetter.tilVilkårData(VilkårType.OPPTJENINGSPERIODEVILKÅR, evaluation, grunnlag);
        resultat.setEkstraVilkårresultat(data);
        return resultat;
    }
}
