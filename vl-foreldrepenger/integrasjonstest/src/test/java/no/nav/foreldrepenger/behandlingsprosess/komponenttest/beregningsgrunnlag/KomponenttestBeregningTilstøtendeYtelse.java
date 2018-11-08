package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;

import static no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag.KomponenttestBeregningAssertUtil.assertBeregningsgrunnlag;
import static no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningYtelseTestUtil.YtelseArbeidsforhold;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.FastsettBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.ForeslåBeregningsgrunnlagStegImpl;
import no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl.KontrollerFaktaBeregningStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
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
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningYtelseTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.Tid;

@RunWith(CdiRunner.class)
public class KomponenttestBeregningTilstøtendeYtelse {
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.SEPTEMBER, 1);
    private static final String ORGNR = "123456789";
    private static final String INFOTRYGD_SAKSNUMMER = "000123";
    private static final BigDecimal DEKNINGSGRAD_100 = Dekningsgrad.DEKNINGSGRAD_100.tilProsentVerdi();
    private static final BigDecimal DEKNINGSGRAD_80 = Dekningsgrad.DEKNINGSGRAD_80.tilProsentVerdi();
    private static final BigDecimal DEKNINGSGRAD_65 = Dekningsgrad.DEKNINGSGRAD_65.tilProsentVerdi();
    private static final YtelseArbeidsforhold ytArbeidsforhold =
        BeregningYtelseTestUtil.lagYtelseArbeidsforhold(INFOTRYGD_SAKSNUMMER, ORGNR, Arbeidskategori.ARBEIDSTAKER);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    @FagsakYtelseTypeRef("FP")
    private ForeslåBeregningsgrunnlagStegImpl foreslåBeregningsgrunnlagSteg;
    @Inject
    @FagsakYtelseTypeRef("FP")
    private FastsettBeregningsgrunnlagStegImpl fastsettBeregningsgrunnlagSteg;

    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningInnhentSamletTjenesteStub innhentSamletTjenesteStub;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingUtil;
    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningsgrunnlagTestUtil bgTestUtil;
    @Inject
    private BeregningYtelseTestUtil byTestUtil;
    @Inject
    BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private ScenarioMorSøkerForeldrepenger scenario;
    private AksjonspunktRepository aksjonspunktRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private Behandling behandling;
    private BehandlingskontrollKontekst kontekst;
    private KontrollerFaktaBeregningStegImpl kontrollerFaktaBeregningSteg;
    private BigDecimal GRUNNBELØP;
    private BigDecimal GRUNNBELØP_6G;

    @Before
    public void setup() {
        GRUNNBELØP = bgTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        GRUNNBELØP_6G = GRUNNBELØP.multiply(BigDecimal.valueOf(6L));
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        kontrollerFaktaBeregningSteg = innhentSamletTjenesteStub.lagKontrollerFaktaBeregningSteg(behandling);
        scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(TpsRepo.STD_KVINNE_AKTØR_ID)
            .medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING));
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        behandling = scenario.lagre(repositoryProvider);

        OppgittFordelingEntitet oppgittFordelingEntitet = new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny().medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER).build()), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordelingEntitet);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, ORGNR);
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Tilstøtende ytelse: Sykepenger Dekningsgrad: 100%
    @Test
    public void skal_teste_arbeidsforhold_med_sykepenger_100() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode sykepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1L), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1L));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseSykepenger(behandling, sykepengerPeriode, inntektPrMnd, DEKNINGSGRAD_100,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_100);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat, inntektPrÅr, inntektPrÅr);
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Tilstøtende ytelse: Sykepenger Dekningsgrad:65%
    @Test
    public void skal_teste_arbeidsforhold_med_sykepenger_65() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode sykepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1L), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(15));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseSykepenger(behandling, sykepengerPeriode, inntektPrMnd, DEKNINGSGRAD_65,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_65);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat,
            GRUNNBELØP_6G.multiply(Dekningsgrad.DEKNINGSGRAD_65.getVerdi()),
            GRUNNBELØP_6G.multiply(Dekningsgrad.DEKNINGSGRAD_65.getVerdi()));
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Tilstøtende ytelse: foreldrepenger Dekningsgrad:100%
    @Test
    public void skal_teste_arbeidsforhold_med_foreldrepenger_100() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode foreldrepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(12L));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseForeldrepenger(behandling, foreldrepengerPeriode, inntektPrMnd, DEKNINGSGRAD_100,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_100);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat, inntektPrÅr, inntektPrÅr);
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Tilstøtende ytelse: foreldrepenger Dekningsgrad:80%
    @Test
    public void skal_teste_arbeidsforhold_med_foreldrepenger_80() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(35000L);
        BigDecimal inntektPrÅr = inntektPrMnd.multiply(BigDecimal.valueOf(12L));
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode foreldrepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10L));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseForeldrepenger(behandling, foreldrepengerPeriode, inntektPrMnd, DEKNINGSGRAD_80,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_80);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat, inntektPrÅr, inntektPrÅr);
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Inntekt > 6G
    // Tilstøtende ytelse: foreldrepenger Dekningsgrad:100%
    @Test
    public void skal_teste_arbeidsforhold_over6G_med_foreldrepenger_100() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(100000L);
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode foreldrepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(12L));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseForeldrepenger(behandling, foreldrepengerPeriode, inntektPrMnd, DEKNINGSGRAD_100,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_100);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat, GRUNNBELØP_6G, GRUNNBELØP_6G);
    }

    // Arbeidsgivere: 1
    // Arbeidsforhold: 1
    // Inntekt > 6G
    // Tilstøtende ytelse: foreldrepenger Dekningsgrad:80%
    @Test
    public void skal_teste_arbeidsforhold_over6G_med_foreldrepenger_80() {
        // Arrange
        BigDecimal inntektPrMnd = BigDecimal.valueOf(100000L);
        BigDecimal refusjonPrMnd = BigDecimal.valueOf(0);
        Periode arbeidsforholdPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(7L), Tid.TIDENES_ENDE);
        Periode foreldrepengerPeriode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(10L));

        opprettArbeidsforhold(arbeidsforholdPeriode, inntektPrMnd, refusjonPrMnd);
        byTestUtil.opprettYtelseForeldrepenger(behandling, foreldrepengerPeriode, inntektPrMnd, DEKNINGSGRAD_80,
            SKJÆRINGSTIDSPUNKT_OPPTJENING, ytArbeidsforhold);

        // Act steg KontrollerFaktaBeregning
        BehandleStegResultat kontrollerFaktaBeregningStegResultat = kontrollerFaktaBeregningSteg.utførSteg(kontekst);
        // Assert KontrollerFaktaBeregning
        assertKontrollerFaktaOmBeregningSteg(kontrollerFaktaBeregningStegResultat, DEKNINGSGRAD_80);

        /// Act steg ForeslåBeregningsgrunnlag
        BehandleStegResultat foreslåBeregningsgrunnlagStegResultat = foreslåBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert ForeslåBeregningsgrunnlag
        assertKontrollerAnnetSteg(foreslåBeregningsgrunnlagStegResultat);

        // Act steg FastsettBeregningsgrunnlag
        BehandleStegResultat fastsettBeregningsgrunnlagStegResultat = fastsettBeregningsgrunnlagSteg.utførSteg(kontekst);
        // Assert FastsettBeregningsgrunnlag
        Dekningsgrad dekningsgrad = Dekningsgrad.fraBigDecimal(DEKNINGSGRAD_80);
        assertFastsettBeregningsgrunnlag(fastsettBeregningsgrunnlagStegResultat,
            GRUNNBELØP_6G.multiply(dekningsgrad.getVerdi()),
            GRUNNBELØP_6G.multiply(dekningsgrad.getVerdi()));
    }

    private void assertKontrollerFaktaOmBeregningSteg(BehandleStegResultat behandleStegResultat, BigDecimal dekningsgrad) {
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        Optional<Beregningsgrunnlag> beregningsgrunnlagEtterFørsteSeg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(aksjonspunkter.isEmpty()).isFalse();
        assertThat(beregningsgrunnlagEtterFørsteSeg.isPresent()).isTrue();

        AksjonspunktDefinisjon apDef = aksjonspunktRepository.finnAksjonspunktDefinisjon(aksjonspunkter.get(0).getKode());
        assertThat(apDef.getAksjonspunktType()).isEqualTo(AksjonspunktType.MANUELL);
        assertThat(apDef).isEqualTo(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagEtterFørsteSeg.get();
        assertBeregningsgrunnlag(beregningsgrunnlag,
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1),
            Arrays.asList(AktivitetStatus.TILSTØTENDE_YTELSE));

        Dekningsgrad dekningsgradRegel = Dekningsgrad.fraBigDecimal(dekningsgrad);
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi())
            .isEqualByComparingTo(beregningsgrunnlag.getGrunnbeløp().getVerdi().multiply(dekningsgradRegel.getVerdi()));
    }

    private void assertKontrollerAnnetSteg(BehandleStegResultat behandleStegResultat) {
        List<AksjonspunktDefinisjon> aksjonspunkter = behandleStegResultat.getAksjonspunktListe();
        Optional<Beregningsgrunnlag> beregningsgrunnlagSteg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(aksjonspunkter.isEmpty()).isTrue();
        assertThat(beregningsgrunnlagSteg.isPresent()).isTrue();
    }

    private void assertFastsettBeregningsgrunnlag(BehandleStegResultat behandleStegResultat,
                                                  BigDecimal avkortetPerÅr,
                                                  BigDecimal redusertPerÅr) {
        List<AksjonspunktDefinisjon> aksjonspunkterEtterFørsteSteg = behandleStegResultat.getAksjonspunktListe();
        Optional<Beregningsgrunnlag> beregningsgrunnlagSteg = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(aksjonspunkterEtterFørsteSteg.isEmpty()).isTrue();
        assertThat(beregningsgrunnlagSteg.isPresent()).isTrue();
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagSteg.get();
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(beregningsgrunnlagPeriode.getAvkortetPrÅr()).isEqualByComparingTo(avkortetPerÅr);
        assertThat(beregningsgrunnlagPeriode.getRedusertPrÅr()).isEqualByComparingTo(redusertPerÅr);
    }

    private void opprettArbeidsforhold(Periode periode, BigDecimal inntektPerMnd, BigDecimal refusjonPerMnd) {
        String arbeidsforholdId = "abcd-efgh-ijkl-mnop";
        iayTestUtil.byggArbeidForBehandlingMedVirksomhetPåInntekt(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING,
            periode.getFom(), periode.getTom(), arbeidsforholdId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR), inntektPerMnd);
        inntektsmeldingUtil.opprettInntektsmelding(behandling, ORGNR, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjonPerMnd, refusjonPerMnd);
    }

}
