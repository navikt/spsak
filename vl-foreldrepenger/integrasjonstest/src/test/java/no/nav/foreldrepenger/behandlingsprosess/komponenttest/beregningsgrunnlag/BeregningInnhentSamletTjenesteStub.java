package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;


import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.ForeslåBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.KontrollerFaktaBeregningStegImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningInfotrygdsakTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagFraTilstøtendeYtelseTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettInntektskategoriFraSøknadTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.OpprettBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InnhentingSamletTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.impl.InntektTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@RunWith(CdiRunner.class)
public class BeregningInnhentSamletTjenesteStub {

    private Period antallMånederMedInnhenting;
    private Period antallMånederMedOpplysningsperiode;
    @Inject
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private BeregningInfotrygdsakTjeneste beregningInfotrygdsakTjeneste;
    @Inject
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Inject
    private VirksomhetTjeneste virksomhetTjeneste;
    @Inject
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    @Inject
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Inject
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    @Inject
    private TpsTjeneste tpsTjeneste;
    @Inject
    private InfotrygdTjeneste infotrygdTjeneste;
    @Inject
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    @Inject
    private MeldekortTjeneste meldekortTjeneste;

    @Inject
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    @Inject
    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste;
    @Inject
    private BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste;
    @Inject
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;

    @Inject
    private MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel;
    @Inject
    private MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel;
    @Inject
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    @Inject
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;


    public BeregningInnhentSamletTjenesteStub() {
        // For inject
    }

    @Inject
    public BeregningInnhentSamletTjenesteStub(@KonfigVerdi(value = "opplysningsperiode.lengde.etter") Period antallMånederMedInnhenting,
                                              @KonfigVerdi(value = "opplysningsperiode.lengde") Period antallMånederMedOpplysningsperiode) {
        this.antallMånederMedOpplysningsperiode = antallMånederMedOpplysningsperiode;
        this.antallMånederMedInnhenting = antallMånederMedInnhenting;

    }

    KontrollerFaktaBeregningStegImpl lagKontrollerFaktaBeregningSteg(Behandling behandling) {
        return new KontrollerFaktaBeregningStegImpl(repositoryProvider, aksjonspunktUtlederForBeregning, lagOpprettBeregningsgrunnlagTjeneste(behandling), beregningInfotrygdsakTjeneste);
    }

    ForeslåBeregningsgrunnlagStegImpl lagForeslåBeregningsgrunnlagSteg(Behandling behandling) {
        return new ForeslåBeregningsgrunnlagStegImpl(repositoryProvider, lagForeslåBeregningsgrunnlag(behandling));
    }

    private ForeslåBeregningsgrunnlag lagForeslåBeregningsgrunnlag(Behandling behandling) {
        return new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, lagHentGrunnlagsdataTjeneste(behandling));

    }


    private OpprettBeregningsgrunnlagTjeneste lagOpprettBeregningsgrunnlagTjeneste(Behandling behandling) {

        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = lagHentGrunnlagsdataTjeneste(behandling);


        return new OpprettBeregningsgrunnlagTjeneste(repositoryProvider,
            fastsettSkjæringstidspunktOgStatuser,
            fastsettInntektskategoriFraSøknadTjeneste,
            beregningsgrunnlagFraTilstøtendeYtelseTjeneste,
            fastsettBeregningsgrunnlagPerioderTjeneste,
            hentGrunnlagsdataTjeneste);
    }

    private HentGrunnlagsdataTjeneste lagHentGrunnlagsdataTjeneste(Behandling behandling) {
        InntektTjeneste inntektTjeneste = new InntektTjenesteImpl(
            BeregningInntektsConsumerMockProducer.lagConsumerMock(behandling, inntektArbeidYtelseTjeneste, tpsTjeneste),
            repositoryProvider.getKodeverkRepository(),
            tpsTjeneste);

        InnhentingSamletTjeneste innhentingSamletTjeneste = new InnhentingSamletTjenesteImpl(
            arbeidsforholdTjeneste,
            tpsTjeneste,
            inntektTjeneste,
            infotrygdTjeneste,
            infotrygdBeregningsgrunnlagTjeneste,
            meldekortTjeneste);

        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = new IAYRegisterInnhentingFPTjenesteImpl(
            inntektArbeidYtelseTjeneste, repositoryProvider, virksomhetTjeneste,
            skjæringstidspunktTjeneste, innhentingSamletTjeneste, personopplysningTjeneste, opplysningsPeriodeTjeneste);
        return new HentGrunnlagsdataTjenesteImpl(
            repositoryProvider,
            opptjeningInntektArbeidYtelseTjeneste,
            inntektArbeidYtelseTjeneste,
            iayRegisterInnhentingTjeneste);
    }

}

