package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;


import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.testutil.BeregningRevurderingTestUtil;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.KontrollerFaktaBeregningStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningInfotrygdsakTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagFraTilstøtendeYtelseTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettInntektskategoriFraSøknadTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.OpprettBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.EndringBeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.EndringBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class EndringBeregningsgrunnlagDtoTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final BigDecimal INNTEKT_PR_MND = BigDecimal.valueOf(15000L);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;
    @Inject
    private EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste;
    @Inject
    private BeregningsgrunnlagTestUtil testUtil;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private KontrollerFaktaBeregningStegImpl steg;

    @Inject
    private BeregningInfotrygdsakTjeneste beregningInfotrygdsakTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private RevurderingTjeneste revurderingTjeneste;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingUtil;
    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningRevurderingTestUtil revurderingTestUtil;

    private int refusjonOver6GPrMnd;
    private Behandling behandling;

    @Inject
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    @Inject
    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste;
    @Inject
    private BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste;
    @Inject
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;
    @Inject
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    @Inject
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Inject
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    @Inject
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;
    @Inject
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Inject
    private VirksomhetTjeneste virksomhetTjeneste;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(BeregningIAYTestUtil.AKTØR_ID)
            .medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING));
        scenario.medSøknadHendelse().medFødselsDato(SKJÆRINGSTIDSPUNKT_OPPTJENING).medAntallBarn(1);
        behandling = scenario.lagre(repositoryProvider);
        refusjonOver6GPrMnd = testUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).intValue()*6/12 +1;
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, OppgittDekningsgradEntitet.bruk100());
        OppgittFordelingEntitet oppgittFordelingEntitet = new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny().medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER).build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordelingEntitet);
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        steg = lagKontrollerFaktaBeregningSteg();

        //setter til feil verdi, slik at testen går på nye regler
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void tearDown() {
        //setter tilbake til riktig verdi
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    private KontrollerFaktaBeregningStegImpl lagKontrollerFaktaBeregningSteg() {
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = lagHentGrunnlagsdataTjeneste();
        OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste = new OpprettBeregningsgrunnlagTjeneste(repositoryProvider, fastsettSkjæringstidspunktOgStatuser, fastsettInntektskategoriFraSøknadTjeneste, beregningsgrunnlagFraTilstøtendeYtelseTjeneste, fastsettBeregningsgrunnlagPerioderTjeneste, hentGrunnlagsdataTjeneste);
        return new KontrollerFaktaBeregningStegImpl(repositoryProvider, aksjonspunktUtlederForBeregning, opprettBeregningsgrunnlagTjeneste, beregningInfotrygdsakTjeneste);
    }

    private HentGrunnlagsdataTjeneste lagHentGrunnlagsdataTjeneste() {
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = lagIAYRegisterInnhentingTjeneste();
        return new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste,
            inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
    }

    private IAYRegisterInnhentingTjeneste lagIAYRegisterInnhentingTjeneste() {
        InnhentingSamletTjeneste innhentingSamletTjeneste = mockInnhentingSamletTjeneste();
        return new IAYRegisterInnhentingFPTjenesteImpl(inntektArbeidYtelseTjeneste,
            repositoryProvider, virksomhetTjeneste, skjæringstidspunktTjeneste, innhentingSamletTjeneste, personopplysningTjeneste, opplysningsPeriodeTjeneste);
    }

    private InnhentingSamletTjeneste mockInnhentingSamletTjeneste() {
        InnhentingSamletTjeneste innhentingSamletTjeneste = mock(InnhentingSamletTjeneste.class);
        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(Collections.emptyList(), Collections.emptyList(), InntektsKilde.INNTEKT_BEREGNING);
        when(innhentingSamletTjeneste.getInntektsInformasjon(any(), any(), any(), any())).thenReturn(inntektsInformasjon);
        return innhentingSamletTjeneste;
    }

    private void utførSteg(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        resultat.getAksjonspunktListe().forEach(ap -> repositoryProvider.getAksjonspunktRepository()
            .leggTilAksjonspunkt(behandling, ap));
    }

    @Test
    public void lag_fakta_om_beregning_med_endring_dto() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123243";
        String orgnr2 = "123424256780";
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0, 10);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjonOver6GPrMnd, TIDENES_ENDE);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());

        //Assert
        assertThat(dtoOpt.isPresent()).isTrue();
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
    }

    @Ignore("PFP-278")
    @Test
    public void lag_endring_dto_med_riktige_arbeidsprosenter_for_andel_med_sammenhengende_i_samme_periode_gradering() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123243";
        String orgnr2 = "123424256780";
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        BigDecimal arbeidsprosent5 = BigDecimal.valueOf(60);

        LocalDate opphørsdatoRefusjon = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5).plusDays(1);

        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(1), opphørsdatoRefusjon.minusDays(1), arbeidsprosent5));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjonOver6GPrMnd, TIDENES_ENDE);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());

        //Assert
        assertThat(dtoOpt.isPresent()).isTrue();
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        assertThat(faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler().size()).isEqualTo(2);
        EndringBeregningsgrunnlagAndelDto endringAndel = faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler()
            .stream().filter(andel -> andel.getArbeidsforhold().getArbeidsgiverId().equals(orgnr)).findFirst().get();
        assertThat(endringAndel.getAndelIArbeid()).containsExactlyInAnyOrder(arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4);

    }


    @Ignore("PFP-278")
    @Test
    public void lag_endring_dto_med_riktige_arbeidsprosenter_for_andel_uten_sammenhengende_gradering_i_samme_periode() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123243";
        String orgnr2 = "123424256780";
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        BigDecimal arbeidsprosent5 = BigDecimal.valueOf(60);

        LocalDate opphørsdatoRefusjon = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5).plusDays(1);

        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode.getFom(), arbeidsperiode.getTomOrNull(), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(2), opphørsdatoRefusjon.minusDays(1), arbeidsprosent5));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), testUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).intValue()*6/12 + 1, TIDENES_ENDE);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());

        //Assert
        assertThat(dtoOpt.isPresent()).isTrue();
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        assertThat(faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler().size()).isEqualTo(2);
        EndringBeregningsgrunnlagAndelDto endringAndel = faktaOmBeregningDto.getEndringBeregningsgrunnlag().getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler()
            .stream().filter(andel -> andel.getArbeidsforhold().getArbeidsgiverId().equals(orgnr)).findFirst().get();
        assertThat(endringAndel.getAndelIArbeid()).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4);

    }

    @Test
    public void lag_fakta_om_beregning_for_revurdering_gammel_andel_med_gradering_ingen_refusjon_avkortet_lik_0() {

        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "534";
        String orgnr2 = "23423423";
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);

        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr1, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);

        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr1, 234223);
        gjeldendeBruttoBrÅr.put(orgnr2, 234223);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(orgnr1, 0);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, Collections.emptyMap(), opprinneligePerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap());
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING);


        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(10), arbeidsprosent1));
        revurderingTestUtil.avsluttBehandling(behandling);
        Behandling revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        inntektsmeldingUtil.opprettInntektPåRevurdering(revurdering, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger, null , null);
        inntektsmeldingUtil.opprettInntektsmelding(revurdering, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 60000);
        opptjeningTestUtil.leggTilOpptjening(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);

        //Act
        utførSteg(revurdering);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(revurdering);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(revurdering, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        List<EndringBeregningsgrunnlagArbeidsforholdDto> endredeArbeidsforhold = endringBg.getEndredeArbeidsforhold();
        assertThat(endredeArbeidsforhold.size()).isEqualTo(1);
        assertThat(endredeArbeidsforhold.get(0).getPerioderMedGraderingEllerRefusjon().size()).isEqualTo(1);
        assertThat(endredeArbeidsforhold.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom()).isEqualTo(graderinger.get(0).getPeriode().getFomDato());
    }

    @Test
    public void lag_fakta_om_beregning_for_gammel_andel_med_gradering_ingen_refusjon_avkortet_lik_0() {

        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "534";
        String orgnr2 = "23423423";
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr1, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);

        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr1, 234223);
        gjeldendeBruttoBrÅr.put(orgnr2, 234223);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(orgnr1, 0);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, Collections.emptyMap(), opprinneligePerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap());
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(10), arbeidsprosent1));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 60000);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        List<EndringBeregningsgrunnlagArbeidsforholdDto> endredeArbeidsforhold = endringBg.getEndredeArbeidsforhold();
        assertThat(endredeArbeidsforhold.size()).isEqualTo(1);
        assertThat(endredeArbeidsforhold.get(0).getPerioderMedGraderingEllerRefusjon().size()).isEqualTo(1);
        assertThat(endredeArbeidsforhold.get(0).getPerioderMedGraderingEllerRefusjon().get(0).getFom()).isEqualTo(graderinger.get(0).getPeriode().getFomDato());
    }


    @Test
    public void gammelt_refusjonskrav_skal_gi_endret_arbeidsforhold() {

        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "534";
        String orgnr2 = "23423423";
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));

        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr1, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr1, 0);
        gjeldendeBruttoBrÅr.put(orgnr2, 23141);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(orgnr1, 0);
        avkortetPrÅr.put(orgnr2, 0);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, Collections.emptyMap(), opprinneligePerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap());
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), 70000, 10);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList());
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        revurderingTestUtil.avsluttBehandling(behandling);
        Behandling revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        inntektsmeldingUtil.opprettInntektPåRevurdering(revurdering, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger, null , null);
        opptjeningPeriodeMap.put(orgnr1, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);

        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5).minusDays(1)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5), null));
        testUtil.lagBeregningsgrunnlagForEndring(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder);
        iayTestUtil.byggArbeidForBehandling(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);

        //Act
        utførSteg(revurdering);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(revurdering);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(revurdering, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        List<EndringBeregningsgrunnlagArbeidsforholdDto> endredeArbeidsforhold = endringBg.getEndredeArbeidsforhold();
        assertThat(endredeArbeidsforhold.size()).isEqualTo(2);
    }

    @Test
    public void gammelt_refusjonskrav_skal_ikkje_gi_aksjonspunkt() {

        String arbId1 = "123";
        String orgnr1 = "123456780";
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr1, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr1, 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(orgnr1, 0);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, Collections.emptyMap(), opprinneligePerioder, opprinneligePeriodeÅrsaker, Collections.emptyMap());
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), 70000, 10);
        List<Gradering> graderinger = new ArrayList<>();
        revurderingTestUtil.avsluttBehandling(behandling);
        Behandling revurdering = revurderingTjeneste.opprettAutomatiskRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        inntektsmeldingUtil.opprettInntektPåRevurdering(revurdering, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, graderinger, null , null);
        opptjeningTestUtil.leggTilOpptjening(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null));
        testUtil.lagBeregningsgrunnlagForEndring(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder);
        iayTestUtil.byggArbeidForBehandling(revurdering, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1), INNTEKT_PR_MND);

        //Act
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(revurdering);
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(revurdering, beregningsgrunnlag.get());
        assertThat(dtoOpt.isPresent()).isFalse();
    }

    @Test
    public void fordeling_forrige_behandling_skal_settes_lik_beløp_fra_inntektsmelding_om_førstegangsbehandling_og_andel_ikke_lagt_til_av_saksbehandler() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        // Act
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        EndringBeregningsgrunnlagAndelDto endringAndel = endringBg.getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler().get(0);

        //Assert
        assertThat(endringAndel.getFordelingForrigeBehandling()).isEqualByComparingTo(BigDecimal.valueOf(inntekt));
    }

    @Test
    public void fordeling_forrige_behandling_skal_settes_lik_null_om_førstegangsbehandling_og_andel_lagt_til_av_saksbehandler() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Long andelsnrNyAndel = 132L;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Optional<Beregningsgrunnlag> beregningsgrunnlagGjeldende = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);
        testUtil.leggTilAndelLagtTilAvSaksbehandler(behandling, beregningsgrunnlagGjeldende.get(), 0, orgnr, andelsnrNyAndel, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        // Act
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler()).hasSize(3);
        EndringBeregningsgrunnlagAndelDto endringAndel = endringBg.getEndringBeregningsgrunnlagPerioder().get(1).getEndringBeregningsgrunnlagAndeler().stream().filter(andel -> andel.getAndelsnr().equals(andelsnrNyAndel)).findFirst().get();

        //Assert
        assertThat(endringAndel.getFordelingForrigeBehandling()).isNull();

    }

    @Test
    public void fordeling_forrige_behandling_skal_settes_lik_0_om_førstegangsbehandling_og_andel_tilkom_etter_stp() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "4353453";
        String orgnr2 = "65555555";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Periode arbeidsperiode2 = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10));

        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode2, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);

        // Act
        Optional<FaktaOmBeregningDto> dtoOpt = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, beregningsgrunnlag.get());
        FaktaOmBeregningDto faktaOmBeregningDto  = dtoOpt.get();
        assertThat(faktaOmBeregningDto.getFaktaOmBeregningTilfeller()).contains(FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        EndringBeregningsgrunnlagDto endringBg = faktaOmBeregningDto.getEndringBeregningsgrunnlag();
        assertThat(endringBg.getEndringBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        EndringBeregningsgrunnlagAndelDto endringAndel = endringBg.getEndringBeregningsgrunnlagPerioder()
            .stream().filter(periode -> periode.getTom() == null).findFirst().get()
            .getEndringBeregningsgrunnlagAndeler().stream().filter(andel -> andel.getAndelsnr().equals(2L)).findFirst().get();

        //Assert
        assertThat(endringAndel.getFordelingForrigeBehandling()).isEqualByComparingTo(BigDecimal.ZERO);
    }



    @Test
    public void skal_ikkje_kunne_endre_refusjon_arbeidsforhold_tilkom_etter_stp_finnes_i_gjeldende_bg_søker_gradering_total_refusjon_over_6G() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Periode arbeidsperiode2 = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(12));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode2, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING)).findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelTilkomEtterStp = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr)).findFirst();
        assertThat(andelTilkomEtterStp.isPresent()).isTrue();
        assertThat(andelTilkomEtterStp.get().getAvkortetPrÅr()).isNull();

        // Act
        boolean skalKunneEndreRefusjon = endringBeregningsgrunnlagDtoTjeneste.skalKunneEndreRefusjon(behandling, graderingPeriode.get(),
            andelTilkomEtterStp.get(), beregningsgrunnlag.get().getSkjæringstidspunkt());

        // Assert
        assertThat(skalKunneEndreRefusjon).isFalse();
    }


    @Test
    public void skal_kunne_endre_refusjon_arbeidsforhold_tilkom_før_stp_finnes_i_gjeldende_bg_søker_gradering_total_refusjon_over_6G() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING)).findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelMedGradering = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr)).findFirst();
        assertThat(andelMedGradering.isPresent()).isTrue();
        assertThat(andelMedGradering.get().getAvkortetPrÅr()).isNull();

        // Act
        boolean skalKunneEndreRefusjon = endringBeregningsgrunnlagDtoTjeneste.skalKunneEndreRefusjon(behandling, graderingPeriode.get(),
            andelMedGradering.get(), beregningsgrunnlag.get().getSkjæringstidspunkt());

        // Assert
        assertThat(skalKunneEndreRefusjon).isTrue();

    }


    @Test
    public void skal_kunne_endre_refusjon_arbeidsforhold_tilkom_før_stp_finnes_i_gjeldende_bg_brutto_lik_0_avkortet_til_0_søker_gradering_total_refusjon_over_6G() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        HashMap<String, Integer> avkortet = new HashMap<>();
        HashMap<String, Integer> bruttoPrÅr = new HashMap<>();
        bruttoPrÅr.put(orgnr, 0);
        avkortet.put(orgnr, 0);
        List<LocalDateInterval> perioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortet,
            bruttoPrÅr, Collections.emptyMap(), perioder, Collections.singletonList(Collections.emptyList()), Collections.emptyMap());
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING)).findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelMedGradering = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr)).findFirst();
        assertThat(andelMedGradering.isPresent()).isTrue();
        assertThat(andelMedGradering.get().getAvkortetPrÅr()).isNull();

        // Act
        boolean skalKunneEndreRefusjon = endringBeregningsgrunnlagDtoTjeneste.skalKunneEndreRefusjon(behandling, graderingPeriode.get(),
            andelMedGradering.get(), beregningsgrunnlag.get().getSkjæringstidspunkt());

        // Assert
        assertThat(skalKunneEndreRefusjon).isTrue();

    }

    @Test
    public void skal_ikke_kunne_endre_refusjon_arbeidsforhold_tilkom_før_stp_finnes_i_gjeldende_bg_brutto_ulik_0_ikke_avkortet_til_0_søker_gradering_total_refusjon_over_6G() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        HashMap<String, Integer> avkortet = new HashMap<>();
        HashMap<String, Integer> bruttoPrÅr = new HashMap<>();
        bruttoPrÅr.put(orgnr, 23422);
        List<LocalDateInterval> perioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortet,
            bruttoPrÅr, Collections.emptyMap(), perioder, Collections.singletonList(Collections.emptyList()), Collections.emptyMap());
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()
            .stream()
            .filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING))
            .findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelMedGradering = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr))
            .findFirst();
        assertThat(andelMedGradering.isPresent()).isTrue();
        assertThat(andelMedGradering.get().getAvkortetPrÅr()).isNull();

        // Act
        boolean skalKunneEndreRefusjon = endringBeregningsgrunnlagDtoTjeneste.skalKunneEndreRefusjon(behandling, graderingPeriode.get(),
            andelMedGradering.get(), beregningsgrunnlag.get().getSkjæringstidspunkt());

        // Assert
        assertThat(skalKunneEndreRefusjon).isFalse();
    }

    @Test
    public void skal_kunne_endre_refusjon_arbeidsforhold_tilkom_før_stp_finnes_ikkje_i_gjeldende_bg_søker_gradering_total_refusjon_over_6G() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        Beregningsgrunnlag gjelndendeBg = testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(gjelndendeBg.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(gjelndendeBg.getBeregningsgrunnlagPerioder().get(0).getPeriode().getFomDato()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        assertThat(gjelndendeBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(gjelndendeBg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null)).isEqualTo(orgnr2);

        // Arrange
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING)).findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelMedGradering = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr)).findFirst();
        assertThat(andelMedGradering.isPresent()).isTrue();
        assertThat(andelMedGradering.get().getAvkortetPrÅr()).isNull();

        // Act
        boolean skalKunneEndreRefusjon = endringBeregningsgrunnlagDtoTjeneste.skalKunneEndreRefusjon(behandling, graderingPeriode.get(),
            andelMedGradering.get(), beregningsgrunnlag.get().getSkjæringstidspunkt());

        // Assert
        assertThat(skalKunneEndreRefusjon).isTrue();

    }


    @Test
    public void skal_sette_skal_kunne_endre_refusjon_på_riktig_periode() {

        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        String arbId2 = "123234";
        String orgnr2 = "3242521";
        Integer inntekt = 10000;
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr2, arbeidsperiode);
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), INNTEKT_PR_MND);
        testUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), null, BigDecimal.valueOf(50));
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.singletonList(gradering), 0, inntekt);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            Collections.emptyList(), refusjonOver6GPrMnd, inntekt);

        //Act
        utførSteg(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlag = repositoryProvider.getBeregningsgrunnlagRepository().hentBeregningsgrunnlag(behandling);

        // Assert
        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        assertThat(beregningsgrunnlag.get().getBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<BeregningsgrunnlagPeriode> graderingPeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getPeriodeÅrsaker().contains(PeriodeÅrsak.GRADERING)).findFirst();
        assertThat(graderingPeriode.isPresent()).isTrue();
        assertThat(graderingPeriode.get().getPeriode().getFomDato()).isEqualTo(gradering.getPeriode().getFomDato());
        assertThat(graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> andelMedGradering = graderingPeriode.get().getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(andel -> Objects.equals(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).orElse(null), orgnr))
            .findFirst();
        assertThat(andelMedGradering.isPresent()).isTrue();
        assertThat(andelMedGradering.get().getAvkortetPrÅr()).isNull();

        // Act
        Optional<EndringBeregningsgrunnlagDto> endringBgDto = endringBeregningsgrunnlagDtoTjeneste.lagEndringAvBeregningsgrunnlagDto(behandling, beregningsgrunnlag.get());

        // Assert
        assertThat(endringBgDto.isPresent()).isTrue();
        assertThat(endringBgDto.get().getEndringBeregningsgrunnlagPerioder()).hasSize(2);
        Optional<EndringBeregningsgrunnlagPeriodeDto> periodeUtenGraderingOpt = endringBgDto.get().getEndringBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getFom().equals(SKJÆRINGSTIDSPUNKT_OPPTJENING)).findFirst();
        assertThat(periodeUtenGraderingOpt).hasValueSatisfying(periodeUtenGradering ->
            assertThat(periodeUtenGradering.isSkalKunneEndreRefusjon()).isFalse());

        Optional<EndringBeregningsgrunnlagPeriodeDto> periodeMedGraderingOpt = endringBgDto.get().getEndringBeregningsgrunnlagPerioder().stream().filter(periode -> periode.getFom().equals(gradering.getPeriode().getFomDato())).findFirst();
        assertThat(periodeMedGraderingOpt).hasValueSatisfying(periodeMedGradering ->
            assertThat(periodeMedGradering.isSkalKunneEndreRefusjon()).isTrue());

    }

    @Test
    public void skal_legge_til_andel_som_er_lagt_til_manuelt_ved_forrige_faktaavklaring() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);

        List<List<PeriodeÅrsak>> periodeÅrsakerIGjeldendeGrunnlag = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> perioderIGjeldendeGrunnlag = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr, 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        Map<String, List<Boolean>> lagtTilAvSaksbehandler = new HashMap<>();
        lagtTilAvSaksbehandler.put(orgnr, Arrays.asList(false, true));
        Map<String, List<Inntektskategori>> inntektskategori = new HashMap<>();
        inntektskategori.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.SJØMANN));
        testUtil.lagForrigeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, lagtTilAvSaksbehandler, perioderIGjeldendeGrunnlag, periodeÅrsakerIGjeldendeGrunnlag, inntektskategori);


        List<List<PeriodeÅrsak>> periodeÅrsakerINyttBG = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioderINyttBG = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1).plusDays(1), null));
        Map<String, List<Boolean>> lagtTilAvSaksbehandlerEndring = new HashMap<>();
        lagtTilAvSaksbehandlerEndring.put(orgnr, Arrays.asList(false));
        Map<String, List<Inntektskategori>> inntektskategoriEndring = new HashMap<>();
        inntektskategoriEndring.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER));
        Beregningsgrunnlag bg = testUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, periodeÅrsakerINyttBG, perioderINyttBG, lagtTilAvSaksbehandlerEndring, inntektskategoriEndring, Collections.emptyMap());
        List<EndringBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        EndringBeregningsgrunnlagPeriodeDto endringPeriode = new EndringBeregningsgrunnlagPeriodeDto();

        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);

        // Act
        endringBeregningsgrunnlagDtoTjeneste.leggTilAndelerSomErLagtTilManueltVedForrigeFaktaavklaring(bg.getBeregningsgrunnlagPerioder().get(0), endringAndeler, behandling, endringPeriode);

        // Assert
        assertThat(endringAndeler).hasSize(1);
        assertThat(endringAndeler.get(0).getInntektskategori()).isEqualByComparingTo(Inntektskategori.SJØMANN);

        //Arrange
        endringAndeler = new ArrayList<>();
        endringPeriode = new EndringBeregningsgrunnlagPeriodeDto();

        // Act
        endringBeregningsgrunnlagDtoTjeneste.leggTilAndelerSomErLagtTilManueltVedForrigeFaktaavklaring(bg.getBeregningsgrunnlagPerioder().get(1), endringAndeler, behandling, endringPeriode);

        // Assert
        assertThat(endringAndeler).hasSize(1);
        assertThat(endringAndeler.get(0).getInntektskategori()).isEqualByComparingTo(Inntektskategori.SJØMANN);

    }


    @Test
    public void skal_ikkje_legge_til_andel_som_er_lagt_til_manuelt_ved_forrige_faktaavklaring_om_perioden_i_det_nye_grunnlaget_overlapper_med_fleire_enn_1_periode_i_forrige() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);

        List<List<PeriodeÅrsak>> periodeÅrsakerIGjeldendeGrunnlag = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioderIGjeldendeGrunnlag = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1).plusDays(1), null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr, 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        Map<String, List<Boolean>> lagtTilAvSaksbehandler = new HashMap<>();
        lagtTilAvSaksbehandler.put(orgnr, Arrays.asList(false, true));
        Map<String, List<Inntektskategori>> inntektskategori = new HashMap<>();
        inntektskategori.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.SJØMANN));
        testUtil.lagForrigeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, lagtTilAvSaksbehandler, perioderIGjeldendeGrunnlag, periodeÅrsakerIGjeldendeGrunnlag, inntektskategori);


        List<List<PeriodeÅrsak>> periodeÅrsakerINyttBG = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> perioderINyttBG = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, List<Boolean>> lagtTilAvSaksbehandlerEndring = new HashMap<>();
        lagtTilAvSaksbehandlerEndring.put(orgnr, Arrays.asList(false));
        Map<String, List<Inntektskategori>> inntektskategoriEndring = new HashMap<>();
        inntektskategoriEndring.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER));
        Beregningsgrunnlag bg = testUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, periodeÅrsakerINyttBG, perioderINyttBG, lagtTilAvSaksbehandlerEndring, inntektskategoriEndring, Collections.emptyMap());
        List<EndringBeregningsgrunnlagAndelDto> endringAndeler = new ArrayList<>();
        EndringBeregningsgrunnlagPeriodeDto endringPeriode = new EndringBeregningsgrunnlagPeriodeDto();

        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(1);

        // Act
        endringBeregningsgrunnlagDtoTjeneste.leggTilAndelerSomErLagtTilManueltVedForrigeFaktaavklaring(bg.getBeregningsgrunnlagPerioder().get(0), endringAndeler, behandling, endringPeriode);

        // Assert
        assertThat(endringAndeler).hasSize(0);

    }
}
