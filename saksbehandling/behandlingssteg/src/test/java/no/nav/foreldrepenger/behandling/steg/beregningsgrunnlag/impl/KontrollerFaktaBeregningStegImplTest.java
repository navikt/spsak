package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.OpplysningsPeriodeTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsgrunnlagFraTilstøtendeYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettInntektskategoriFraSøknadTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.OpprettBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaBeregningStegImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);

    private static final AktørId AKTØR_ID = BeregningIAYTestUtil.AKTØR_ID;

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());


    private KontrollerFaktaBeregningStegImpl steg;

    @Inject
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    @Inject
    private FastsettInntektskategoriFraSøknadTjeneste fastsettInntektskategoriFraSøknadTjeneste;
    @Inject
    private BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste;
    @Inject
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    @Inject
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Inject
    private VirksomhetTjeneste virksomhetTjeneste;
    @Inject
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);

    @Inject
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPerioderTjeneste;

    private BehandlingskontrollKontekst kontekst = mock(BehandlingskontrollKontekst.class);
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;

    // Test utils
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;
    private BeregningIAYTestUtil iayTestUtil;
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    private BeregningArbeidsgiverTestUtil virksomhetTestUtil;

    @Before
    public void setUp() {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(any())).thenReturn(LocalDate.now());
        opplysningsPeriodeTjeneste = new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste, Period.of(1, 0, 0), Period.of(0, 4, 0));
        virksomhetTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        opptjeningTestUtil = new BeregningOpptjeningTestUtil(resultatRepositoryProvider, virksomhetTestUtil);
        iayTestUtil = new BeregningIAYTestUtil(repositoryProvider, inntektArbeidYtelseTjeneste);
        inntektsmeldingTestUtil = new BeregningInntektsmeldingTestUtil(repositoryProvider, virksomhetTestUtil);
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = lagHentGrunnlagsdataTjeneste();
        OpprettBeregningsgrunnlagTjeneste opprettBeregningsgrunnlagTjeneste = new OpprettBeregningsgrunnlagTjeneste(resultatRepositoryProvider,
            fastsettSkjæringstidspunktOgStatuser, fastsettInntektskategoriFraSøknadTjeneste,
            beregningsgrunnlagFraTilstøtendeYtelseTjeneste, fastsettBeregningsgrunnlagPerioderTjeneste, hentGrunnlagsdataTjeneste);
        steg = new KontrollerFaktaBeregningStegImpl(resultatRepositoryProvider, aksjonspunktUtlederForBeregning, opprettBeregningsgrunnlagTjeneste);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger
            .forAktør(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        when(kontekst.getBehandlingId()).thenReturn(behandling.getId());

        //setter til feil verdi, slik at testen går på nye regler
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void tearDown() {
        //setter tilbake til riktig verdi
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    private HentGrunnlagsdataTjeneste lagHentGrunnlagsdataTjeneste() {
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = lagIAYRegisterInnhentingTjeneste();
        return new HentGrunnlagsdataTjenesteImpl(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste,
            inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
    }

    private IAYRegisterInnhentingTjeneste lagIAYRegisterInnhentingTjeneste() {
        InnhentingSamletTjeneste innhentingSamletTjeneste = mockInnhentingSamletTjeneste();
        return new IAYRegisterInnhentingFPTjenesteImpl(inntektArbeidYtelseTjeneste,
            repositoryProvider, resultatRepositoryProvider, virksomhetTjeneste, skjæringstidspunktTjeneste, innhentingSamletTjeneste, opplysningsPeriodeTjeneste);
    }

    private InnhentingSamletTjeneste mockInnhentingSamletTjeneste() {
        InnhentingSamletTjeneste innhentingSamletTjeneste = mock(InnhentingSamletTjeneste.class);
        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(Collections.emptyList(), Collections.emptyList(), InntektsKilde.INNTEKT_BEREGNING);
        when(innhentingSamletTjeneste.getInntektsInformasjon(any(), any(), any(), any())).thenReturn(inntektsInformasjon);
        return innhentingSamletTjeneste;
    }


    @Test
    public void skal_kunne_opprette_kombinerte_aksjonpunkter_med_TY_tidsbegrenset_atfl_i_samme_org_nyoppstartet_fl_lønnsendring_endret_bg() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        String arbId2 = "2242313414";
        String orgnr2 = "8923432242402";
        String arbId3 = "436743223";
        String orgnr3 = "383482232";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();
        HashMap<String, ReferanseType> referansetypeMap = new HashMap<>();
        HashMap<String, String> referanseMap = new HashMap<>();
        String ref1 = "1";
        leggTilOpptjening(orgnr, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref1, OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        String ref2 = "2";
        leggTilOpptjening(orgnr2, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref2, OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        String ref3 = "3";
        leggTilOpptjening(orgnr2, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref3, OpptjeningAktivitetType.FRILANS, ReferanseType.ORG_NR);
        String ref4 = "4";
        leggTilOpptjening(AKTØR_ID.getId(), opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref4, OpptjeningAktivitetType.SYKEPENGER, ReferanseType.AKTØR_ID);
        String ref5 = "5";
        leggTilOpptjening(orgnr3, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref5, OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        opptjeningTestUtil.leggTilOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, behandling.getBehandlingsresultat());
        iayTestUtil.leggTilOppgittOpptjeningForFL(behandling, true);
        leggTilAT(arbId, orgnr);
        leggTilTidsbegrenset(arbId2, orgnr2);
        leggTilFrilans(arbId2, orgnr2);
        leggTilATMedLønnsendring(arbId3, orgnr3);
        List<YtelseStørrelse> ytelseStørrelseList = Arrays.asList(lagYtelseStørrelseUtenVirksomhet(BigDecimal.TEN),
            lagYtelseStørrelseForVirksomhet(BigDecimal.valueOf(23131), orgnr2));
        leggTilTilstøtendeYtelse(ytelseStørrelseList, behandling, Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 60000);

        // Act
        BehandleStegResultat resultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(resultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = resultatRepositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling);
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(FaktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON,
            FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE,
            FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
            FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL,
            FaktaOmBeregningTilfelle.VURDER_LØNNSENDRING,
            FaktaOmBeregningTilfelle.FASTSETT_ENDRET_BEREGNINGSGRUNNLAG);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriode periode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(4);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erFrilanser()).count()).isEqualTo(1);
        assertArbeidstakerAndeler(Arrays.asList(orgnr, orgnr2, orgnr3), andeler);
    }

    @Test
    public void skal_opprette_andeler_for_AT_som_ikkje_kommer_frå_iay_ved_TY() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        String orgnr2 = "8923432242402";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();
        HashMap<String, ReferanseType> referansetypeMap = new HashMap<>();
        HashMap<String, String> referanseMap = new HashMap<>();
        String ref1 = "1";
        leggTilOpptjening(orgnr, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref1, OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        String ref4 = "4";
        leggTilOpptjening(AKTØR_ID.getId(), opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref4, OpptjeningAktivitetType.SYKEPENGER, ReferanseType.AKTØR_ID);
        opptjeningTestUtil.leggTilOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, behandling.getBehandlingsresultat());
        leggTilAT(arbId, orgnr);
        List<YtelseStørrelse> ytelseStørrelseList = Arrays.asList(lagYtelseStørrelseForVirksomhet(BigDecimal.valueOf(23131), orgnr2));
        leggTilTilstøtendeYtelse(ytelseStørrelseList, behandling, Arbeidskategori.ARBEIDSTAKER);

        // Act
        BehandleStegResultat resultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(resultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = resultatRepositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling);
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriode periode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(2);
        assertArbeidstakerAndeler(Arrays.asList(orgnr, orgnr2), andeler);
    }

    @Test
    public void skal_opprette_andeler_for_SN_som_ikkje_kommer_frå_iay_ved_TY() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();
        HashMap<String, ReferanseType> referansetypeMap = new HashMap<>();
        HashMap<String, String> referanseMap = new HashMap<>();
        String ref1 = "1";
        leggTilOpptjening(orgnr, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref1,
            OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        String ref4 = "4";
        leggTilOpptjening(AKTØR_ID.getId(), opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref4,
            OpptjeningAktivitetType.SYKEPENGER, ReferanseType.AKTØR_ID);
        opptjeningTestUtil.leggTilOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, behandling.getBehandlingsresultat());
        leggTilAT(arbId, orgnr);
        List<YtelseStørrelse> ytelseStørrelseList = Arrays.asList(lagYtelseStørrelseUtenVirksomhet(BigDecimal.valueOf(23131)));
        leggTilTilstøtendeYtelse(ytelseStørrelseList, behandling, Arbeidskategori.JORDBRUKER);

        // Act
        BehandleStegResultat resultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(resultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = resultatRepositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling);
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriode periode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(2);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).count()).isEqualTo(1);
        assertArbeidstakerAndeler(Arrays.asList(orgnr), andeler);
    }

    @Test
    public void skal_kunne_opprette_kombinerte_aksjonpunkter_med_TY_SN_ny_i_arbeidslivet_nyoppstartet_fl() {
        // Arrange
        String arbId = "213414";
        String orgnr = "8998242402";
        String orgnr2 = "65756756";
        String orgnr3 = "23423423";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();
        HashMap<String, ReferanseType> referansetypeMap = new HashMap<>();
        HashMap<String, String> referanseMap = new HashMap<>();
        String ref1 = "1";
        leggTilOpptjening(orgnr, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref1,
            OpptjeningAktivitetType.ARBEID, ReferanseType.ORG_NR);
        String ref2 = "2";
        leggTilOpptjening(orgnr2, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref2,
            OpptjeningAktivitetType.FRILANS, ReferanseType.ORG_NR);
        String ref3 = "3";
        leggTilOpptjening(orgnr3, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref3,
            OpptjeningAktivitetType.NÆRING, ReferanseType.ORG_NR);
        String ref4 = "4";
        leggTilOpptjening(AKTØR_ID.getId(), opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref4,
            OpptjeningAktivitetType.SYKEPENGER, ReferanseType.AKTØR_ID);
        opptjeningTestUtil.leggTilOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, behandling.getBehandlingsresultat());
        leggTilAT(arbId, orgnr);
        iayTestUtil.leggTilOppgittOpptjeningForFLOgSN(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, true, true);
        List<YtelseStørrelse> ytelseStørrelseList = Arrays.asList(lagYtelseStørrelseUtenVirksomhet(BigDecimal.valueOf(23131)));
        leggTilTilstøtendeYtelse(ytelseStørrelseList, behandling, Arbeidskategori.JORDBRUKER);

        // Act
        BehandleStegResultat resultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(resultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = resultatRepositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling);
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE, FaktaOmBeregningTilfelle.VURDER_NYOPPSTARTET_FL, FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriode periode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(3);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).count()).isEqualTo(1);
        assertArbeidstakerAndeler(Arrays.asList(orgnr), andeler);
    }

    @Test
    public void skal_få_TY_aksjonspunkt_om_ulike_SN_forhold_kommer_fra_infotrygd_og_opptjening_med_opptjening_som_prioritert_inntektskategori() {
        // Arrange
        String orgnr3 = "23423423";
        HashMap<String, Periode> opptjeningMap = new HashMap<>();
        HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap = new HashMap<>();
        HashMap<String, ReferanseType> referansetypeMap = new HashMap<>();
        HashMap<String, String> referanseMap = new HashMap<>();
        String ref2 = "2";
        leggTilOpptjening(orgnr3, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref2,
            OpptjeningAktivitetType.NÆRING, ReferanseType.ORG_NR);
        String ref4 = "4";
        leggTilOpptjening(AKTØR_ID.getId(), opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, ref4,
            OpptjeningAktivitetType.SYKEPENGER, ReferanseType.AKTØR_ID);
        opptjeningTestUtil.leggTilOpptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningMap, aktivitetTypeMap, referansetypeMap, referanseMap, behandling.getBehandlingsresultat());
        iayTestUtil.lagOppgittOpptjeningForSN(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, true, VirksomhetType.FISKE);
        List<YtelseStørrelse> ytelseStørrelseList = Arrays.asList(lagYtelseStørrelseUtenVirksomhet(BigDecimal.valueOf(23131)));
        leggTilTilstøtendeYtelse(ytelseStørrelseList, behandling, Arbeidskategori.JORDBRUKER);

        // Act
        BehandleStegResultat resultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(resultat.getAksjonspunktListe()).containsExactly(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Beregningsgrunnlag bg = resultatRepositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling);
        assertThat(bg.getFaktaOmBeregningTilfeller()).containsExactlyInAnyOrder(
            FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE, FaktaOmBeregningTilfelle.VURDER_SN_NY_I_ARBEIDSLIVET);
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        BeregningsgrunnlagPeriode periode = bg.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = periode.getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler.size()).isEqualTo(1);
        assertThat(andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).count()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel snAndel = andeler.stream().filter(andel -> andel.getAktivitetStatus().erSelvstendigNæringsdrivende()).findFirst().get();
        assertThat(snAndel.getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
        assertThat(snAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi()).isEqualTo(BigDecimal.ZERO);
    }

    private void leggTilOpptjening(String orgnr, HashMap<String, Periode> opptjeningMap, HashMap<String, OpptjeningAktivitetType> aktivitetTypeMap,
                                   HashMap<String, ReferanseType> referansetypeMap, HashMap<String,
        String> referanseMap, String ref1, OpptjeningAktivitetType opptjeningAktivitetType, ReferanseType referanseType) {
        opptjeningMap.put(ref1, Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING));
        aktivitetTypeMap.put(ref1, opptjeningAktivitetType);
        referansetypeMap.put(ref1, referanseType);
        referanseMap.put(ref1, orgnr);
    }


    private void leggTilAT(String arbId, String orgnr) {
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
    }

    private void leggTilATMedLønnsendring(String arbId, String orgnr) {
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), true);
    }

    private void leggTilFrilans(String arbId2, String orgnr2) {
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, false, BigDecimal.TEN, false);
    }

    private void leggTilTidsbegrenset(String arbId2, String orgnr2) {
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
    }

    private void assertArbeidstakerAndeler(List<String> orgnrs, List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        orgnrs.forEach(orgnr -> assertThat(andeler.stream().filter(andel -> andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).isPresent() &&
            andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).get().getOrgnr().equals(orgnr)).count()).isEqualTo(1));
    }

    private void leggTilTilstøtendeYtelse(List<YtelseStørrelse> ytelseStørrelseList, Behandling behandling, Arbeidskategori arbeidstaker) {
        iayTestUtil.leggTilAktørytelse(behandling,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(10), RelatertYtelseTilstand.LØPENDE,
            "4328893", RelatertYtelseType.SYKEPENGER, ytelseStørrelseList, arbeidstaker);
    }

    private YtelseStørrelse lagYtelseStørrelseUtenVirksomhet(BigDecimal beløp) {
        return YtelseStørrelseBuilder.ny()
            .medBeløp(beløp)
            .medHyppighet(InntektPeriodeType.MÅNEDLIG)
            .build();
    }

    private YtelseStørrelse lagYtelseStørrelseForVirksomhet(BigDecimal beløp, String orgnr) {
        return YtelseStørrelseBuilder.ny()
            .medBeløp(beløp)
            .medHyppighet(InntektPeriodeType.MÅNEDLIG)
            .medVirksomhet(virksomhetTestUtil.forArbeidsgiverVirksomhet(orgnr).getVirksomhet())
            .build();
    }

}
