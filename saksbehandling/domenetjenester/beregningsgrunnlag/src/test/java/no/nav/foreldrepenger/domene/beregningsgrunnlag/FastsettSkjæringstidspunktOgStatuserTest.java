package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ReferanseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class FastsettSkjæringstidspunktOgStatuserTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate DAGEN_FØR_SFO = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1);

    private static final Long DEKNINGSGRAD = 100L;

    private static final String ORG_NUMMER = "654";
    private static final String ORG_NUMMER2 = "321";
    private static final String ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD = "222333444";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(entityManager);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private FastsettSkjæringstidspunktOgStatuser tjeneste;

    private Behandling behandling;

    private List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();

    @Mock
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;

    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    private SatsRepository beregningRepository = repositoryProvider.getSatsRepository();
    private VirksomhetEntitet virksomhet3;

    @Inject
    private BeregningIAYTestUtil iayTestUtil;

    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    @Before
    public void setup() {

        repositoryProvider.getVirksomhetRepository().lagre(new VirksomhetEntitet.Builder().medNavn("Virksomhet1").medOrgnr(ORG_NUMMER).oppdatertOpplysningerNå().build());
        repositoryProvider.getVirksomhetRepository().lagre(new VirksomhetEntitet.Builder().medNavn("Virksomhet2").medOrgnr(ORG_NUMMER2).oppdatertOpplysningerNå().build());
        virksomhet3 = new VirksomhetEntitet.Builder().medNavn("Virksomhet3").medOrgnr(ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD).oppdatertOpplysningerNå().build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet3);
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste,
            skjæringstidspunktTjeneste, null, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        tjeneste = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel, oversetterFraRegel);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        when(opptjeningInntektArbeidYtelseTjeneste.hentGodkjentAktivitetTyper(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING))
            .thenReturn(aktiviteter);

        Opptjening opptjening = new Opptjening(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), DAGEN_FØR_SFO);
        when(opptjeningInntektArbeidYtelseTjeneste.hentOpptjening(behandling)).thenReturn(opptjening);
    }

    @Test
    public void testForIngenOpptjeningsaktiviteter() {

        // Arrange

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        assertThat(grunnlag.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.UDEFINERT);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.UDEFINERT);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedUbruttAktivitet() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedAvbruttAktivitet() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3).plusDays(1), grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedLangvarigMilitærtjeneste() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3),
            OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3).plusDays(1), grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.MILITÆR_ELLER_SIVIL, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.MILITÆR_ELLER_SIVIL, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedKortvarigMilitærtjeneste() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(4),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(2),
            OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(3).plusDays(1), grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedKortvarigArbeidsforhold() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(4),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(2),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(2),
            OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(2).plusDays(1), grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.MILITÆR_ELLER_SIVIL);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedMangeAvsluttedeAktiviteter() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1),
            OpptjeningAktivitetType.ARBEIDSAVKLARING,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.MILITÆR_ELLER_SIVIL);
    }

    @Test
    public void testSkjæringstidspunktForKombinertArbeidstakerOgFrilanser() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.FRILANS,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.KOMBINERT_AT_FL);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER);
    }

    @Test
    public void testSkjæringstidspunktForFlereFrilansaktiviteter() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.FRILANS,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(6),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.FRILANS,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER2,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.FRILANSER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.FRILANSER);
    }

    @Test
    public void testSkjæringstidspunktForFlereArbeidsforholdIUlikeVirksomheter() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(6),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER2,
            ReferanseType.ORG_NR));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForFlereArbeidsforholdISammeVirksomhet() {


        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD,
            ReferanseType.ORG_NR));
        when(opptjeningInntektArbeidYtelseTjeneste.hentGodkjentAktivitetTyper(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING))
            .thenReturn(aktiviteter);
        String arbId1 = "xyz";
        String arbId2 = "abc";
        String arbId3 = "def";
        opprettInntektsmelding(virksomhet3,
            arbId1, arbId2, arbId3);


        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), DAGEN_FØR_SFO, arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), DAGEN_FØR_SFO, arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4), DAGEN_FØR_SFO, arbId3, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER_MED_FLERE_ARBEIDSFORHOLD));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.ARBEIDSTAKER);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER);
    }

    private void opprettInntektsmelding(Virksomhet virksomhet, String... arbIdListe) {

        for (String arbId : arbIdListe) {

            Inntektsmelding im = InntektsmeldingBuilder.builder()
                .medVirksomhet(virksomhet)
                .medInnsendingstidspunkt(LocalDateTime.now())
                .medArbeidsforholdId(arbId)
                .medBeløp(BigDecimal.valueOf(100000))
                .medJournalpostId(new JournalpostId(arbId))
                .medStartDatoPermisjon(LocalDate.now())
                .build();
            repositoryProvider.getInntektArbeidYtelseRepository().lagre(behandling, im);

        }
    }

    @Test
    public void testSkjæringstidspunktForKombinertArbeidstakerOgSelvstendig() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(4),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.NÆRING,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.KOMBINERT_AT_SN);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedSykepenger() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.SYKEPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.TILSTØTENDE_YTELSE);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER);
    }

    @Test
    public void testSkjæringstidspunktForDagpengemottakerMedSykepenger() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2),
            OpptjeningAktivitetType.DAGPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.SYKEPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.TILSTØTENDE_YTELSE);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.DAGPENGER);
    }

    @Test
    public void testSkjæringstidspunktForAAPmottakerMedSykepenger() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2),
            OpptjeningAktivitetType.ARBEIDSAVKLARING,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.SYKEPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.TILSTØTENDE_YTELSE);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSAVKLARINGSPENGER);
    }

    @Test
    public void testSkjæringstidspunktForArbeidstakerMedAlleAktiviteterUnntattTYogAAP() {

        // Arrange
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10),
            SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2),
            OpptjeningAktivitetType.ARBEID,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            ORG_NUMMER,
            ReferanseType.ORG_NR));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.DAGPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.FRILANS,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.NÆRING,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.VARTPENGER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.SLUTTPAKKE,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.UTDANNINGSPERMISJON,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.VENTELØNN,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.VIDERE_ETTERUTDANNING,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));
        aktiviteter.add(new OpptjeningAktivitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2),
            DAGEN_FØR_SFO,
            OpptjeningAktivitetType.ETTERLØNN_ARBEIDSGIVER,
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT));

        // Act
        Beregningsgrunnlag grunnlag = tjeneste.fastsettSkjæringstidspunktOgStatuser(behandling);

        // Assert
        verifiserBeregningsgrunnlag(SKJÆRINGSTIDSPUNKT_OPPTJENING, grunnlag);
        verifiserAktivitetStatuser(grunnlag, AktivitetStatus.KOMBINERT_AT_FL_SN,
            AktivitetStatus.DAGPENGER, AktivitetStatus.MILITÆR_ELLER_SIVIL);
        verifiserBeregningsgrunnlagPerioder(grunnlag, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER,
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatus.DAGPENGER,
            AktivitetStatus.MILITÆR_ELLER_SIVIL, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER,
            AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.ARBEIDSTAKER);
    }

    private void verifiserBeregningsgrunnlag(LocalDate skjæringstidspunkt, Beregningsgrunnlag grunnlag) {
        long gVerdi = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, skjæringstidspunkt).getVerdi();
        assertThat(grunnlag.getSkjæringstidspunkt()).isEqualTo(skjæringstidspunkt);
        assertThat(grunnlag.getDekningsgrad()).isEqualTo(DEKNINGSGRAD);
        assertThat(grunnlag.getOpprinneligSkjæringstidspunkt()).isEqualTo(skjæringstidspunkt);
        assertThat(grunnlag.getGrunnbeløp().getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(gVerdi));
        assertThat(grunnlag.getRedusertGrunnbeløp().getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(gVerdi));
    }

    private void verifiserBeregningsgrunnlagPerioder(Beregningsgrunnlag grunnlag, AktivitetStatus... expectedArray) {
        assertThat(grunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode bgPeriode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<AktivitetStatus> actualList = bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(BeregningsgrunnlagPrStatusOgAndel::getAktivitetStatus).collect(Collectors.toList());
        assertThat(actualList).containsOnly(expectedArray);
        assertThat(actualList).hasSameSizeAs(expectedArray);
        bgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(this::erArbeidstakerEllerFrilans)
            .forEach(this::verifiserBeregningsperiode);
    }

    private boolean erArbeidstakerEllerFrilans(BeregningsgrunnlagPrStatusOgAndel bgpsa) {
        return (AktivitetStatus.ARBEIDSTAKER.equals(bgpsa.getAktivitetStatus()))
            || (AktivitetStatus.FRILANSER.equals(bgpsa.getAktivitetStatus()));
    }

    private void verifiserBeregningsperiode(BeregningsgrunnlagPrStatusOgAndel bgpsa) {
        assertThat(bgpsa.getBeregningsperiodeFom()).isNotNull();
        assertThat(bgpsa.getBeregningsperiodeTom()).isNotNull();
    }

    private void verifiserAktivitetStatuser(Beregningsgrunnlag grunnlag, AktivitetStatus... expectedArray) {
        List<AktivitetStatus> actualList = grunnlag.getAktivitetStatuser().stream()
            .map(BeregningsgrunnlagAktivitetStatus::getAktivitetStatus).collect(Collectors.toList());
        assertThat(actualList).containsOnly(expectedArray);
    }

}
