package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;

import static no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlag;
import static no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlagAndel;
import static no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlagPeriode;
import static no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag.KomponenttestBeregningAssertUtil.assertSammenligningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.FastsettBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.ForeslåBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.KontrollerFaktaBeregningStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer.FastsettBeregningsgrunnlagATFLOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.InntektPrAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapterImpl;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.Tid;


@RunWith(CdiRunner.class)
public class KomponenttestBeregningArbeidstaker {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.SEPTEMBER, 1);
    private static final String ORGNR = "123456789";
    private static final String AKTØRID = "123123123123";
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    private FastsettBeregningsgrunnlagStegImpl fastsettBeregningsgrunnlagSteg;

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
    private BeregningInnhentSamletTjenesteStub innhentSamletTjenesteStub;
    @Inject
    private HistorikkTjenesteAdapterImpl historikkTjenesteAdapter;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;
    @Inject
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    private FastsettBeregningsgrunnlagATFLOppdaterer fastsettBeregningsgrunnlagATFLOppdaterer;

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    private Behandling behandling;

    private BehandlingskontrollKontekst kontekst;

    private KontrollerFaktaBeregningStegImpl kontrollerFaktaBeregningSteg;

    private ForeslåBeregningsgrunnlagStegImpl foreslåBeregningsgrunnlagSteg;

    @Before
    public void setUp() {
        beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(TpsRepo.STD_KVINNE_AKTØR_ID)
            .medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING));
        scenario.medSøknadHendelse().medFødselsDato(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1)).medAntallBarn(1);
        behandling = scenario.lagre(repositoryProvider);
        OppgittFordelingEntitet oppgittFordelingEntitet = new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny().medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER).build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordelingEntitet);
        fastsettBeregningsgrunnlagATFLOppdaterer = new FastsettBeregningsgrunnlagATFLOppdaterer(repositoryProvider, historikkTjenesteAdapter, arbeidsgiverHistorikkinnslagTjeneste);

        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        foreslåBeregningsgrunnlagSteg = innhentSamletTjenesteStub.lagForeslåBeregningsgrunnlagSteg(behandling);
        kontrollerFaktaBeregningSteg = innhentSamletTjenesteStub.lagKontrollerFaktaBeregningSteg(behandling);
    }

    @BeforeClass
    public static void setupProps() {
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @AfterClass
    public static void tearDownProps() {
        System.clearProperty("dato.for.nye.beregningsregler");
    }


    // Arbeidsgivere: 1 (virksomhet)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Full
    // Aksjonspunkt: Nei
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_uten_aksjonspunkter() {
        // Arrange
        String arbeidsforholdId = "abcd-efgh-ijkl-mnop";
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal refusjonskravPrÅr = refusjonPrMnd.multiply(BigDecimal.valueOf(12L));
        iayTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7), Tid.TIDENES_ENDE, arbeidsforholdId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR), inntektPrMnd);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, ORGNR, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjonPrMnd, inntektPrMnd);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, ORGNR);


        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, refusjonskravPrÅr);

        // Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(foreslåBeregningsgrunnlagStegResultat,
            refusjonskravPrÅr,
            inntektPrÅr,
            inntektPrÅr,
            0L, false);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg FastsettBeregningsgrunnlag
        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat,
            refusjonskravPrÅr,
            inntektPrÅr,
            null);
    }

    // Arbeidsgivere: 1 (privatperson)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Ingen
    // Aksjonspunkt: 5038 - FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_med_aksjonspunkter_arbeidsgiver_er_privatperson() {
        // Arrange
        BigDecimal inntektIRegister = BigDecimal.valueOf(70000L);
        BigDecimal inntektPrÅrRegister = inntektIRegister.multiply(BigDecimal.valueOf(12L));

        iayTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7), Tid.TIDENES_ENDE, null, arbeidsgiverTestUtil.forArbeidsgiverpPrivatperson(AKTØRID),inntektIRegister);
        lagOpptjeningMedPrivatpersonSomArbeidsgiver();

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        KontrollerFaktaBeregningStegImpl kontrollerFaktaBeregningSteg = innhentSamletTjenesteStub.lagKontrollerFaktaBeregningSteg(behandling);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, null);

        // Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(foreslåBeregningsgrunnlagStegResultat,
            null,
            inntektPrÅrRegister,
            null,
            0L, false);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat,
            null,
            inntektPrÅrRegister,
            null);

    }

    private void lagOpptjeningMedPrivatpersonSomArbeidsgiver() {
        Map<String, Periode> perioder = Collections.singletonMap(AKTØRID, Periode.månederFør(SKJÆRINGSTIDSPUNKT_OPPTJENING, 7));
        Map<String, String> referanseMap = Collections.singletonMap(AKTØRID, AKTØRID);
        Map<String, ReferanseType> referanseType = Collections.singletonMap(AKTØRID, ReferanseType.AKTØR_ID);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, perioder, Collections.emptyMap(), referanseType, referanseMap);
    }

    // Arbeidsgivere: 1 (virksomhet)
    // Arbeidsforhold: 1
    // Inntekt: < 6G
    // Refusjon: Ingen
    // Aksjonspunkt: 5038 - FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS
    @Test
    public void skal_utføre_beregning_for_arbeidstaker_med_aksjonspunkter() {
        // Arrange
        String arbeidsforholdId = "abcd-efgh-ijkl-mnop";
        BigDecimal inntektIRegister = BigDecimal.valueOf(70000L);
        BigDecimal inntektFraIM = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅrRegister = inntektIRegister.multiply(BigDecimal.valueOf(12L));
        BigDecimal inntektPrÅrIM = inntektFraIM.multiply(BigDecimal.valueOf(12L));
        Integer overstyrtPrÅr = 500000;

        iayTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7), Tid.TIDENES_ENDE, arbeidsforholdId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR),inntektIRegister);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, ORGNR, SKJÆRINGSTIDSPUNKT_OPPTJENING, BigDecimal.ZERO, inntektFraIM);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, ORGNR);

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        KontrollerFaktaBeregningStegImpl kontrollerFaktaBeregningSteg = innhentSamletTjenesteStub.lagKontrollerFaktaBeregningSteg(behandling);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);

        // Assert steg KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, BigDecimal.ZERO);

        // Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg ForeslåBeregningsgrunnlag
        assertForeslåBeregningsgrunnlagSteg(foreslåBeregningsgrunnlagStegResultat,
            BigDecimal.ZERO,
            inntektPrÅrRegister,
            inntektPrÅrIM,
            500L, true);

        // Act oppdaterer
        FastsettBeregningsgrunnlagATFLDto dto = lagATFLOppdatererDto(overstyrtPrÅr);
        fastsettBeregningsgrunnlagATFLOppdaterer.oppdater(dto, behandling);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);

        // Assert steg FastsettBeregningsgrunnlag og oppdaterer
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat,
            BigDecimal.ZERO,
            inntektPrÅrIM,
            overstyrtPrÅr);

    }


    private FastsettBeregningsgrunnlagATFLDto lagATFLOppdatererDto(Integer overstyrtPrÅr) {
        return new FastsettBeregningsgrunnlagATFLDto("Begrunnelse",lagInntektPrAndelDto(overstyrtPrÅr),null);
    }

    private List<InntektPrAndelDto> lagInntektPrAndelDto(Integer overstyrtPrÅr) {
        return Collections.singletonList(new InntektPrAndelDto(overstyrtPrÅr, 1L));
    }

    private void assertKontrollerFaktaOmBeregningSteg(BehandleStegResultat behandleStegResultat, BigDecimal refusjonskravPrÅr) {
        List<AksjonspunktDefinisjon> aksjonspunkterEtterFørsteSteg = behandleStegResultat.getAksjonspunktListe();
        Optional<Beregningsgrunnlag> beregningsgrunnlagEtterFørsteSeg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        // Assert steg KontrollerFaktaBeregning
        assertThat(aksjonspunkterEtterFørsteSteg.isEmpty()).isTrue();
        assertThat(beregningsgrunnlagEtterFørsteSeg.isPresent()).isTrue();
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagEtterFørsteSeg.get();
        assertBeregningsgrunnlag(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(AktivitetStatus.ARBEIDSTAKER));

        // Beregningsgrunnlagperiode
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertBeregningsgrunnlagPeriode(førstePeriode,
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, null),
            BigDecimal.ZERO,
            null,
            null,
            refusjonskravPrÅr);

        // BeregningsgrunnlagPrStatusOgAndel
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagAndel(
            førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            null,
            AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            refusjonskravPrÅr, null);
    }

    private void assertForeslåBeregningsgrunnlagSteg(BehandleStegResultat behandleStegResultat,
                                                     BigDecimal refusjonskravPrÅr,
                                                     BigDecimal inntektPrÅrRegister,
                                                     BigDecimal inntektPrÅrIM,
                                                     Long avvik, boolean medAksjonspunkt) {
        List<AksjonspunktDefinisjon> aksjonspunkterEtterAndreSteg = behandleStegResultat.getAksjonspunktListe();
        Optional<Beregningsgrunnlag> beregningsgrunnlagEtterAndreSteg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        // Aksjonspunkter
        if (medAksjonspunkt) {
            assertThat(aksjonspunkterEtterAndreSteg.size()).isEqualTo(1);
            assertThat(aksjonspunkterEtterAndreSteg.get(0)).isEqualTo(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);
        } else {
            assertThat(aksjonspunkterEtterAndreSteg.size()).isEqualTo(0);
        }
        assertThat(beregningsgrunnlagEtterAndreSteg.isPresent()).isTrue();

        // Sammenligningsgrunnlag
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagEtterAndreSteg.get();
        assertSammenligningsgrunnlag(beregningsgrunnlag.getSammenligningsgrunnlag(), inntektPrÅrRegister, avvik);

        BigDecimal gjeldendeInntekt = inntektPrÅrIM == null ? inntektPrÅrRegister : inntektPrÅrIM;
        // Periodenivå
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode førstePeriodeStegTo = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertBeregningsgrunnlagPeriode(førstePeriodeStegTo,
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, null), gjeldendeInntekt, null, null, refusjonskravPrÅr);


        // Andelsnivå
        for (BeregningsgrunnlagPeriode bgp : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            for (BeregningsgrunnlagPrStatusOgAndel andel : bgp.getBeregningsgrunnlagPrStatusOgAndelList()) {
                assertBeregningsgrunnlagAndel(andel,
                    gjeldendeInntekt,
                    AktivitetStatus.ARBEIDSTAKER,
                    Inntektskategori.ARBEIDSTAKER,
                    SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
                    SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), refusjonskravPrÅr, null);
            }
        }
    }
    private void assertFastsettBeregningsgrunnlag(BehandleStegResultat behandleStegResultat,
                                                  BigDecimal refusjonskravPrÅr,
                                                  BigDecimal beregnetPrÅr,
                                                  Integer overstyrtPrÅr) {
        // Aksjonspunkter
        List<AksjonspunktDefinisjon> aksjonspunkterEtterTredjeSteg = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunkterEtterTredjeSteg.isEmpty()).isTrue();

        // Beregningsgrunnlag
        Optional<Beregningsgrunnlag> beregningsgrunnlagEtterTredjeSteg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagEtterTredjeSteg.isPresent()).isTrue();
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagEtterTredjeSteg.get();
        BigDecimal overstyrtViØnskerAssertPå = null;
        BigDecimal inntektDagsatsBeregnesFra = beregnetPrÅr;
        if (overstyrtPrÅr != null) {
            inntektDagsatsBeregnesFra = BigDecimal.valueOf(overstyrtPrÅr);
            overstyrtViØnskerAssertPå = BigDecimal.valueOf(overstyrtPrÅr);
        }
        BigDecimal forventetDagsats = inntektDagsatsBeregnesFra.divide(BigDecimal.valueOf(260), 0, BigDecimal.ROUND_HALF_UP).min(BigDecimal.valueOf(2236));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);

        // Periodenivå
        BeregningsgrunnlagPeriode førstePeriodeTredjeSteg = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(førstePeriodeTredjeSteg.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertBeregningsgrunnlagPeriode(førstePeriodeTredjeSteg,
            ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, null),
            beregnetPrÅr,
            forventetDagsats.longValue(),
            overstyrtViØnskerAssertPå, refusjonskravPrÅr);

        // Andelsnivå
        BeregningsgrunnlagPrStatusOgAndel andel = førstePeriodeTredjeSteg.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertBeregningsgrunnlagAndel(andel,
            beregnetPrÅr,
            AktivitetStatus.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(3),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1),
            refusjonskravPrÅr,
            overstyrtViØnskerAssertPå);
    }
}
