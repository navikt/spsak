package no.nav.foreldrepenger.domene.inngangsvilkaar.opptjening;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningAktivitetPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningInntektPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårTypeRef;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_23)
public class InngangsvilkårForeldrepengerOpptjening implements Inngangsvilkår {

    private OpptjeningInntektArbeidYtelseTjeneste opptjeningTjeneste;
    private InngangsvilkårOversetter inngangsvilkårOversetter;

    InngangsvilkårForeldrepengerOpptjening() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårForeldrepengerOpptjening(InngangsvilkårOversetter inngangsvilkårOversetter,
                                                  OpptjeningInntektArbeidYtelseTjeneste opptjeningTjeneste) {
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
        this.opptjeningTjeneste = opptjeningTjeneste;
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {

        List<OpptjeningAktivitetPeriode> relevanteOpptjeningAktiveter = opptjeningTjeneste.hentRelevanteOpptjeningAktiveterForVilkårVurdering(behandling);
        List<OpptjeningInntektPeriode> relevanteOpptjeningInntekter = opptjeningTjeneste.hentRelevanteOpptjeningInntekterForVilkårVurdering(behandling);
        Opptjening opptjening = opptjeningTjeneste.hentOpptjening(behandling);

        LocalDate behandlingstidspunkt = LocalDate.now(FPDateUtil.getOffset()); // TODO (FC): Avklar hva denne bør være

        Opptjeningsgrunnlag grunnlag = new OpptjeningsgrunnlagAdapter(behandlingstidspunkt, opptjening.getFom(),
            opptjening.getTom())
                .mapTilGrunnlag(relevanteOpptjeningAktiveter, relevanteOpptjeningInntekter);

        // returner egen output i tillegg for senere lagring
        OpptjeningsvilkårResultat output = new OpptjeningsvilkårResultat();
        Evaluation evaluation = new Opptjeningsvilkår().evaluer(grunnlag, output);

        VilkårData vilkårData = inngangsvilkårOversetter.tilVilkårData(VilkårType.OPPTJENINGSVILKÅRET, evaluation, grunnlag);
        vilkårData.setEkstraVilkårresultat(output);

        return vilkårData;
    }

}
