package no.nav.foreldrepenger.domene.beregning.ytelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class BeregnFeriepengerTjenesteTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_MOR = LocalDate.of(2018, 12, 1);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_FAR = SKJÆRINGSTIDSPUNKT_MOR.plusWeeks(6);
    private static final int DAGSATS = 123;
    public static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(entityManager);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(entityManager);

    private BeregnFeriepengerTjeneste tjeneste;

    @Before
    public void setUp() {
        tjeneste = new BeregnFeriepengerTjeneste();
    }

    @Test
    public void skalBeregneFeriepenger() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag().medDekningsgrad(100L)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_MOR)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_MOR)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();
        Behandling morsBehandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningsresultatPerioder morsBeregningsresultat = lagBeregningsresultatFP(SKJÆRINGSTIDSPUNKT_MOR, SKJÆRINGSTIDSPUNKT_FAR, Inntektskategori.ARBEIDSTAKER);

        // Act
        tjeneste.beregnFeriepenger(morsBehandling, morsBeregningsresultat, beregningsgrunnlag);

        // Assert
        assertThat(morsBeregningsresultat.getBeregningsresultatFeriepenger()).hasValueSatisfying(this::assertBeregningsresultatFeriepenger);
    }

    @Test
    public void skalIkkeBeregneFeriepenger() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag().medDekningsgrad(100L)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_MOR)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_MOR)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();
        Behandling morsBehandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningsresultatPerioder morsBeregningsresultat = lagBeregningsresultatFP(SKJÆRINGSTIDSPUNKT_MOR, SKJÆRINGSTIDSPUNKT_MOR.plusMonths(6), Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);

        // Act
        tjeneste.beregnFeriepenger(morsBehandling, morsBeregningsresultat, beregningsgrunnlag);

        //Assert
        assertThat(morsBeregningsresultat.getBeregningsresultatFeriepenger()).hasValueSatisfying(resultat -> {
            assertThat(resultat.getBeregningsresultatFeriepengerPrÅrListe()).isEmpty();
            assertThat(resultat.getFeriepengerPeriodeFom()).isNull();
            assertThat(resultat.getFeriepengerPeriodeTom()).isNull();
            assertThat(resultat.getFeriepengerRegelInput()).isNotNull();
            assertThat(resultat.getFeriepengerRegelSporing()).isNotNull();
        });
    }

    private void assertBeregningsresultatFeriepenger(BeregningsresultatFeriepenger feriepenger) {
        assertThat(feriepenger.getFeriepengerPeriodeFom()).as("FeriepengerPeriodeFom").isEqualTo(SKJÆRINGSTIDSPUNKT_MOR);
        assertThat(feriepenger.getFeriepengerPeriodeTom()).as("FeriepengerPeriodeTom").isEqualTo(SKJÆRINGSTIDSPUNKT_FAR);
        List<BeregningsresultatFeriepengerPrÅr> beregningsresultatFeriepengerPrÅrListe = feriepenger.getBeregningsresultatFeriepengerPrÅrListe();
        assertThat(beregningsresultatFeriepengerPrÅrListe).as("beregningsresultatFeriepengerPrÅrListe").hasSize(2);
        BeregningsresultatFeriepengerPrÅr prÅr1 = beregningsresultatFeriepengerPrÅrListe.get(0);
        assertThat(prÅr1.getOpptjeningsår()).as("prÅr1.opptjeningsår").isEqualTo(LocalDate.of(2018, 12, 31));
        assertThat(prÅr1.getÅrsbeløp().getVerdi()).as("prÅr1.årsbeløp").isEqualTo(BigDecimal.valueOf(263)); // DAGSATS * 21 * 0.102
        BeregningsresultatAndel andelÅr1 = prÅr1.getBeregningsresultatAndel();
        assertThat(andelÅr1).isNotNull();
        assertThat(andelÅr1.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(2);
        BeregningsresultatFeriepengerPrÅr prÅr2 = beregningsresultatFeriepengerPrÅrListe.get(1);
        assertThat(prÅr2.getOpptjeningsår()).as("prÅr2.opptjeningsår").isEqualTo(LocalDate.of(2019, 12, 31));
        assertThat(prÅr2.getÅrsbeløp().getVerdi()).as("prÅr2.årsbeløp").isEqualTo(BigDecimal.valueOf(113)); // DAGSATS * 9 * 0.102
        BeregningsresultatAndel andelÅr2 = prÅr2.getBeregningsresultatAndel();
        assertThat(andelÅr2).isNotNull();
        assertThat(andelÅr2.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(2);
    }

    private BeregningsresultatPerioder lagBeregningsresultatFP(LocalDate periodeFom, LocalDate periodeTom, Inntektskategori inntektskategori) {
        BeregningsresultatPerioder beregningsresultat = BeregningsresultatPerioder.builder().medRegelInput("input").medRegelSporing("sporing").build();
        BeregningsresultatPeriode beregningsresultatPeriode = BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(periodeFom, periodeTom)
            .build(beregningsresultat);
        BeregningsresultatAndel.builder()
            .medInntektskategori(inntektskategori)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medDagsats(DAGSATS)
            .medDagsatsFraBg(DAGSATS)
            .medBrukerErMottaker(true)
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medStillingsprosent(BigDecimal.valueOf(100))
            .build(beregningsresultatPeriode);
        return beregningsresultat;
    }
}
