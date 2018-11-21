package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.util.FPDateUtil;

@RunWith(CdiRunner.class)
public class BeregningsperiodeTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, 9, 1);
    private static final String FUNKSJONELT_TIDSOFFSET = FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE;
    private static final String FUNKSJONELT_TIDSOFFSET_AKTIVERT = FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;
    @Inject
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;
    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private Behandling behandling;
    private Arbeidsgiver arbeidsgiverA;
    private Arbeidsgiver arbeidsgiverB;


    @Before
    public void setup() {
        System.setProperty(FUNKSJONELT_TIDSOFFSET_AKTIVERT, "true");
        settSimulertNåtidTil(LocalDate.now());
        opprettArbeidsforhold();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT);
    }

    @AfterClass
    public static void after() {
        System.clearProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT);
        FPDateUtil.init();
    }

    private void settSimulertNåtidTil(LocalDate dato) {
        Period periode = Period.between(LocalDate.now(), dato);
        System.setProperty(FUNKSJONELT_TIDSOFFSET, periode.toString());
        FPDateUtil.init();
    }

    private void opprettArbeidsforhold() {
        arbeidsgiverA = beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet("123456789");
        arbeidsgiverB = beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet("987654321");
    }

    @Test
    public void skalTesteAtBeregningsperiodeBlirSattRiktig() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 15);
        //Act
        DatoIntervallEntitet periode = BeregningsperiodeTjeneste.fastsettBeregningsperiodeForATFLAndeler(skjæringstidspunkt);
        //Assert
        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2019, 2, 1));
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void skalIkkeSettesPåVentNårIkkeErATFL() {
        //Arrange
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatus.DAGPENGER);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalIkkeSettesPåVentNårNåtidErEtterFrist() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(15));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet("312"));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalAlltidSettesPåVentNårBrukerErFrilanserFørFrist() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(4));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, AktivitetStatus.FRILANSER);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skalAldriSettesPåVentNårBrukerBareHarStatusTYFørFrist() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(4));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, AktivitetStatus.TILSTØTENDE_YTELSE);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalSettesPåVentNårSisteMånedInntektIkkeMottattFørFrist() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(3));
        String orgnr = arbeidsgiverA.getIdentifikator();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        byggInntektForBehandling(arbeidsgiverA, SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(2));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skalIkkeSettesPåVentNårSisteMånedInntektIkkeMottattFørFristMenHarInntektsmelding() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(3));
        String orgnr = arbeidsgiverA.getIdentifikator();
        String arbId = "123";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalIkkeSettesPåVentNårSisteMånedInntektErMottattFørFrist() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(4));
        String orgnrA = arbeidsgiverA.getIdentifikator();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnrA));
        byggInntektForBehandling(arbeidsgiverA, SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalSettesPåVentNårSisteMånedInntektIkkeErMottattFørFristFlereArbeidsforhold() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(4));
        String orgnr = arbeidsgiverA.getIdentifikator();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        byggInntektForBehandling(arbeidsgiverA, SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        String orgnrB = arbeidsgiverB.getIdentifikator();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnrB));
        byggInntektForBehandling(arbeidsgiverB, SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(2));

        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isTrue();
    }

    @Test
    public void skalIkkeSettesPåVentNårSisteMånedInntektErMottattOgHarInntektsmeldingFørFristFlereArbeidsforhold() {
        //Arrange
        settSimulertNåtidTil(SKJÆRINGSTIDSPUNKT.plusDays(2));
        String orgnrA = arbeidsgiverA.getIdentifikator();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnrA));
        byggInntektForBehandling(arbeidsgiverA, SKJÆRINGSTIDSPUNKT.minusMonths(12), SKJÆRINGSTIDSPUNKT.minusMonths(1));
        String orgnrB = arbeidsgiverB.getIdentifikator();
        String arbIdB = "122";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), arbIdB, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnrB));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnrB, arbIdB, SKJÆRINGSTIDSPUNKT);

        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        boolean resultat = beregningsperiodeTjeneste.skalBehandlingSettesPåVent(behandling);
        //Assert
        assertThat(resultat).isFalse();
    }

    @Test
    public void skalUtledeRiktigFrist() {
        //Arrange
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, LocalDate.of(2017, 1, 1), LocalDate.of(2030, 1, 1), "123", arbeidsgiverTestUtil.forArbeidsgiverVirksomhet("321"));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT);
        //Act
        LocalDate frist = beregningsperiodeTjeneste.utledBehandlingPåVentFrist(behandling);
        //Assert
        assertThat(frist).isEqualTo(LocalDate.of(2018,9,6));
    }

    private void byggInntektForBehandling(Arbeidsgiver arbgiver, LocalDate fom, LocalDate tom) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseTjeneste.opprettBuilderForRegister(behandling);
        InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder aktørInntekt = inntektArbeidYtelseAggregatBuilder.getAktørInntektBuilder(behandling.getAktørId());
        AktørInntektEntitet.InntektBuilder inntektBuilder = aktørInntekt.getInntektBuilder(InntektsKilde.INNTEKT_BEREGNING, Opptjeningsnøkkel.forOrgnummer(arbgiver.getIdentifikator()));
        inntektBuilder.medArbeidsgiver(arbgiver);
        for (LocalDate måned = fom; !måned.isAfter(tom); måned = måned.plusMonths(1)) {
            inntektBuilder.leggTilInntektspost(lagInntektspost(måned, 20000));
        }
        aktørInntekt.leggTilInntekt(inntektBuilder);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørInntekt(aktørInntekt);
        inntektArbeidYtelseTjeneste.lagre(behandling, inntektArbeidYtelseAggregatBuilder);
    }

    private InntektEntitet.InntektspostBuilder lagInntektspost(LocalDate måned, int lønn) {
        return InntektEntitet.InntektspostBuilder.ny()
            .medBeløp(BigDecimal.valueOf(lønn))
            .medPeriode(måned.withDayOfMonth(1), måned.with(TemporalAdjusters.lastDayOfMonth()))
            .medInntektspostType(InntektspostType.LØNN);
    }

}
