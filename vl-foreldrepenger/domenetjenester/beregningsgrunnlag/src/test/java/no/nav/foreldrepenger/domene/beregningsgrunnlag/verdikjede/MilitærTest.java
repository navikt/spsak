package no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.byggMilitærForBehandling;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class MilitærTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final AktørId AKTØR_ID = new AktørId("210195");

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private Behandling behandling;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;


    @Before
    public void setup() {
        OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjenesteImpl hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel,oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjenesteImpl kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilaserTjeneste = new KontrollerFaktaBeregningFrilanserTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(repositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilaserTjeneste);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider);
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        byggMilitærForBehandling(inntektArbeidYtelseRepository, behandling,  aktiviteter);
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjenesteImpl(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);

    }


    @Test
    public void militærKasterIkkeException() {
        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7);

        Beregningsgrunnlag foreslåttBeregningsgrunlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BigDecimal verdi = periode.getBeregnetPrÅr();
        assertThat(verdi).isNotNull();
    }
}
