package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaBeregningTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018,9,30);
    
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();

    @Inject
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    @Inject
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste;

    @Inject
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;
    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;
    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private Behandling behandling;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(AKTØR_ID));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void returnererTrueHvisBrukerHarStatusTY() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.TILSTØTENDE_YTELSE);

        //Act
        boolean brukerHarStatusTY = kontrollerFaktaBeregningTjeneste.brukerMedAktivitetStatusTY(behandling);

        //Assert
        assertThat(brukerHarStatusTY).isTrue();
    }

    @Test
    public void skalTesteAtAksjonspunktOpprettesNårBrukerHarLønnsendringUtenInntektsmelding() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), true);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        //Act
        boolean brukerHarLønnsendring = kontrollerFaktaBeregningTjeneste.brukerHarHattLønnsendringOgManglerInntektsmelding(behandling);

        //Assert
        assertThat(brukerHarLønnsendring).isTrue();
    }

    @Test
    public void returnererFalseOmBeregningsgrunnlagIkkjeHarPerioderMedPeriodeårsakerGraderingEllerEndringIRefusjon() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.NATURALYTELSE_BORTFALT));
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder);

        //Act
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    @Test
    public void returnererFalseForNyInntektsmeldingUtenRefusjonskrav() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        //Act
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 1
    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Ja
    // Returnerer true
    @Test
    public void returnererTrueForGraderingOgArbeidsforholdetTilkomEtterSkjæringstidpunktet() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering));
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 2
    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer true
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGStørreEnnNullBeregningsgrunnlagsandelAvkortetTilNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 234223);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 3
    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer false
    @Test
    public void returnererFalseForGraderingGjeldendeBruttoBGStørreEnnNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 234223);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 63274);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 4
    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon større enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGLikNullTotalRefusjonStørreEnn6G() {
        //Arrange
        int seksG = beregningTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2 = seksG + 12;
        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "234232";
        String orgnr2 = "3353533";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 63274);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList());
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> refusjonPrÅr = new HashMap<>();
        refusjonPrÅr.put(orgnr2, refusjon2);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder, refusjonPrÅr);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2 / 12);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering));
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 4
    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon mindre enn 6G for alle arbeidsforhold
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererFalseForGraderingGjeldendeBruttoBGLikNullTotalRefusjonMindreEnn6G() {
        //Arrange
        int seksG = beregningTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2 = seksG - 12;
        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "234232";
        String orgnr2 = "3353533";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 63274);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList());
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> refusjonPrÅr = new HashMap<>();
        refusjonPrÅr.put(orgnr2, refusjon2);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder,refusjonPrÅr);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2 / 12);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering));
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 4 (med avkortet til 0)
    // Gradering: Ja
    // Refusjon: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Total refusjon større enn 6 G
    // Returnerer True
    @Test
    public void returnererTrueForGraderingGjeldendeBruttoBGLikNullOgAvkortetTilNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "123342";
        String orgnr2 = "12345435346780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList());
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, 60000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 4 (uten gjeldende BG)
    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon fra alle arbeidsgivere > 6G: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ikke definert
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ikke definert
    // Returnerer True
    @Test
    public void returnererTrueForGraderingUtenGjeldendeBGMedTotalRefusjonStørreEnn6G() {
        //Arrange
        int seksG = beregningTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2 = seksG + 12;
        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "234232";
        String orgnr2 = "3353533";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList());
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> refusjonPrÅr = new HashMap<>();
        refusjonPrÅr.put(orgnr2, refusjon2);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder, refusjonPrÅr);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2 / 12);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 4 (uten gjeldende BG)
    // Gradering: Ja
    // Refusjon: Nei
    // Total refusjon fra alle arbeidsgivere > 6G: Nei
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ikke definert
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ikke definert
    // Returnerer True
    @Test
    public void returnererFalseForGraderingUtenGjeldendeBGMedTotalRefusjonMindreEnn6G() {
        //Arrange
        int seksG = beregningTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING).multiply(BigDecimal.valueOf(6)).intValue();
        int refusjon2 = seksG - 12;
        String arbId1 = "123";
        String orgnr1 = "123456780";
        String arbId2 = "234232";
        String orgnr2 = "3353533";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker = Arrays.asList(Collections.emptyList());
        List<LocalDateInterval> perioder = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> refusjonPrÅr = new HashMap<>();
        refusjonPrÅr.put(orgnr2, refusjon2);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, periodePeriodeÅrsaker, perioder, refusjonPrÅr);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2 / 12);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 0);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }


    // CASE 5 (andel avkortet til 0)
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonGjeldendeBruttoBGLikNullOgAvkortetTilNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 5
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonGjeldendeBruttoBGLikNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 42322);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 5 (uten gjeldende BG)
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ikke definert
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ikke definert
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonUtenGjeldendeBG() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 6
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer False
    @Test
    public void returnererFalseForGraderingOgRefusjonArbeidsfholdTilkomEtterStpGjeldendeBruttoBGStørreEnnNull() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23422);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 42322);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 6 (andel avkortet til 0)
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer False
    @Test
    public void returnererFalseForGraderingOgRefusjonArbeidsfholdTilkomEtterStpGjeldendeBruttoBGStørreEnn0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23422);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 7 (andel avkortet til 0)
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer False
    @Test
    public void returnererFalseForGraderingOgRefusjonGjeldendeBruttoBGStørreEnn0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23422);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 7
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer False
    @Test
    public void returnererFalseForGraderingOgRefusjonGjeldendeBruttoBGStørreEnn0AndelAvkortetTil0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23422);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23422);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }


    // CASE 8 (andel avkortet til 0)
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonGjeldendeBruttoBGLik0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 8
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererTrueForGraderingOgRefusjonGjeldendeBruttoBGLik0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 34534);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 9
    // Gradering: Ja
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ikke definert
    // Returnerer True
    @Test
    public void returnererFalseForGraderingOgRefusjonUtenGjeldendeBG() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), null, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.singletonList(gradering), 100);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }


    // CASE 10
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererTrueForRefusjonArbfholdTilkomEtterStpRefusjonGjeldendeBruttoBGLik0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 34534);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 10 (andel avkortet til 0)
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer True
    @Test
    public void returnererTrueForRefusjonArbfholdTilkomEtterStpRefusjonGjeldendeBruttoBGLik0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 10 (uten gjeldende bg)
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ikke definert
    // Returnerer True
    @Test
    public void returnererTrueForRefusjonArbfholdTilkomEtterStpRefusjonUtenGjeldendeBG() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }


    // CASE 11 (andel avkortet til 0)
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonArbfholdTilkomEtterStpRefusjonGjeldendeBruttoBGStørreEnn0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23542);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 11
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Ja
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonArbfholdTilkomEtterStpRefusjonGjeldendeBruttoBGStørreEnn0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23542);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 54232);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 12
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonGjeldendeBruttoBGStørreEnn0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23542);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 54232);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 12 (andel avkortet til 0)
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ja
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer False
    @Test
    public void returnererFalseForRefusjonGjeldendeBruttoBGStørreEnn0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23542);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    // CASE 13 (andel avkortet til 0)
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Ja
    // Returnerer True
    @Test
    public void returnererTrueForRefusjonGjeldendeBruttoBGLik0AndelAvkortetTil0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 13
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Nei
    // Gjeldende Beregningsgrunnlagsandel avkortet til 0: Nei
    // Returnerer True
    @Test
    public void returnererTrueForRefusjonGjeldendeBruttoBGLik0() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        List<List<PeriodeÅrsak>> opprinneligePeriodeÅrsaker = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> opprinneligePerioder = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        avkortetPrÅr.put(ArbeidsforholdRef.ref(orgnr1).getReferanse(), 23423);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, opprinneligePerioder, opprinneligePeriodeÅrsaker);
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isTrue();
    }

    // CASE 14
    // Gradering: Nei
    // Refusjon: Ja
    // Tilkom etter skjæringstidspunktet: Nei
    // Gjeldende brutto BG > 0: Ikke definert
    // Returnerer false
    @Test
    public void returnererFalseForRefusjonUtenGjeldendeBeregningsgrunnlag() {
        //Arrange
        String arbId1 = "123";
        String orgnr1 = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        beregningTestUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr1, arbId1, SKJÆRINGSTIDSPUNKT_OPPTJENING, 1000);
        //Act
        fastsettPerioder(behandling);
        boolean manuellBehandlingForEndringAvBG = kontrollerFaktaBeregningTjeneste.vurderManuellBehandlingForEndretBeregningsgrunnlag(behandling);

        //Assert
        assertThat(manuellBehandlingForEndringAvBG).isFalse();
    }

    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisBareArbeidstaker() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = kontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandling);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon.isEmpty()).isTrue();
    }

    @Test
    public void ikkeFrilansISammeArbeidsforholdHvisFrilansHosAnnenOppdragsgiver() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        String orgnrFrilans = "987654320";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2),
            null, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnrFrilans), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, false, BigDecimal.TEN, false);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = kontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandling);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon.isEmpty()).isTrue();
    }

    @Test
    public void frilansISammeArbeidsforhold() {
        //Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), null, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, false, BigDecimal.TEN, false);
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.KOMBINERT_AT_FL);

        //Act
        Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon = kontrollerFaktaBeregningFrilanserTjeneste.brukerErArbeidstakerOgFrilanserISammeOrganisasjon(behandling);

        //Assert
        assertThat(brukerErArbeidstakerOgFrilanserISammeOrganisasjon.isEmpty()).isFalse();
    }

    private void fastsettPerioder(Behandling behandling) {
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling);
        Beregningsgrunnlag beregningsgrunnlag = Kopimaskin.deepCopy(bg);
        fastsettBeregningsgrunnlagPeriodeTjeneste.fastsettPerioder(behandling, beregningsgrunnlag);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGjeldendeBeregningsgrunnlag(bg.getGjeldendeBeregningsgrunnlag().orElse(null));
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
    }

}
