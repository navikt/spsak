package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class BeregningsresultatRepositoryImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final Repository repository = repoRule.getRepository();

    private final BeregningsresultatRepository beregningsresultatFPRepository = new BeregningsresultatRepositoryImpl(repoRule.getEntityManager());

    private BeregningsresultatPerioder.Builder beregningsresultatFPBuilder;
    private BeregningsresultatPeriode.Builder brPeriodebuilder;
    private BeregningsresultatAndel.Builder beregningsresultatAndelBuilder;
    private Behandling behandling;
    private LocalDate dagensDato;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private VirksomhetEntitet beregningVirksomhet;

    @Before
    public void setup() {
        beregningVirksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("55L")
            .medNavn("BeregningVirksomheten")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        dagensDato = LocalDate.now();

        beregningsresultatFPBuilder = BeregningsresultatPerioder.builder();
        brPeriodebuilder = BeregningsresultatPeriode.builder();
        beregningsresultatAndelBuilder = BeregningsresultatAndel.builder();

    }

    @Test
    public void lagreOgHentBeregningsresultatFPkobling() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        // Assert
        Optional<BeregningsResultat> BeregningsresultatFPKoblingOptional = beregningsresultatFPRepository.hentHvisEksistererFor(behandlingsresultat);
        assertThat(BeregningsresultatFPKoblingOptional).isPresent();
    }

    @Test
    public void lagreOgHenteBeregningsresultatFP() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        // Assert
        Long id = beregningsresultat.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Optional<BeregningsresultatPerioder> beregningsresultatFPLest = beregningsresultatFPRepository.hentHvisEksisterer(behandlingsresultat);

        assertThat(beregningsresultatFPLest).isEqualTo(Optional.of(beregningsresultat));
    }

    @Test
    public void lagreBeregningsresultatFPOgUnderliggendeTabellerMedEndringsdatoLikDagensDato() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        // Assert
        Long brId = beregningsresultat.getId();
        assertThat(brId).isNotNull();
        BeregningsresultatPeriode brPeriode = beregningsresultat.getBeregningsresultatPerioder().get(0);
        Long brPeriodeId = brPeriode.getId();
        assertThat(brPeriodeId).isNotNull();
        Long brAndelId = brPeriode.getBeregningsresultatAndelList().get(0).getId();

        repository.flushAndClear();
        BeregningsresultatPerioder beregningsresultatLest = repository.hent(BeregningsresultatPerioder.class, brId);
        BeregningsresultatPeriode brPeriodeLest = repository.hent(BeregningsresultatPeriode.class, brPeriodeId);
        BeregningsresultatAndel brAndelLest = repository.hent(BeregningsresultatAndel.class, brAndelId);

        assertThat(beregningsresultatLest.getId()).isNotNull();
        assertThat(beregningsresultatLest.getBeregningsresultatPerioder()).hasSize(1);
        assertThat(beregningsresultatLest.getRegelInput()).isEqualTo(beregningsresultat.getRegelInput());
        assertThat(beregningsresultatLest.getRegelSporing()).isEqualTo(beregningsresultat.getRegelSporing());
        assertThat(beregningsresultatLest.getEndringsdato()).isEqualTo(Optional.of(dagensDato));
        assertBeregningsresultatPeriode(brPeriodeLest, brAndelLest, brPeriode);
    }

    @Test
    public void lagreBeregningsresultatFPOgUnderliggendeTabellerMedTomEndringsdato() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.empty());

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        // Assert
        Long brId = beregningsresultat.getId();
        assertThat(brId).isNotNull();
        BeregningsresultatPeriode brPeriode = beregningsresultat.getBeregningsresultatPerioder().get(0);
        Long brPeriodeId = brPeriode.getId();
        assertThat(brPeriodeId).isNotNull();
        Long brAndelId = brPeriode.getBeregningsresultatAndelList().get(0).getId();

        repository.flushAndClear();
        BeregningsresultatPerioder beregningsresultatLest = repository.hent(BeregningsresultatPerioder.class, brId);
        BeregningsresultatPeriode brPeriodeLest = repository.hent(BeregningsresultatPeriode.class, brPeriodeId);
        BeregningsresultatAndel brAndelLest = repository.hent(BeregningsresultatAndel.class, brAndelId);

        assertThat(beregningsresultatLest.getId()).isNotNull();
        assertThat(beregningsresultatLest.getBeregningsresultatPerioder()).hasSize(1);
        assertThat(beregningsresultatLest.getRegelInput()).isEqualTo(beregningsresultat.getRegelInput());
        assertThat(beregningsresultatLest.getRegelSporing()).isEqualTo(beregningsresultat.getRegelSporing());
        assertThat(beregningsresultatLest.getEndringsdato()).isEmpty();
        assertBeregningsresultatPeriode(brPeriodeLest, brAndelLest, brPeriode);
    }

    @Test
    public void lagreBeregningsresultatFPOgFeriepenger() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));
        BeregningsresultatFeriepenger feriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerPeriodeFom(LocalDate.now())
            .medFeriepengerPeriodeTom(LocalDate.now())
            .medFeriepengerRegelInput("-")
            .medFeriepengerRegelSporing("-")
            .build(beregningsresultat);

        BeregningsresultatAndel andel = beregningsresultat.getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList().get(0);
        BeregningsresultatFeriepengerPrÅr.builder()
            .medOpptjeningsår(LocalDate.now().withMonth(12).withDayOfMonth(31))
            .medÅrsbeløp(300L)
            .build(feriepenger, andel);

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        long id = beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        // Assert
        repository.flushAndClear();
        BeregningsresultatPerioder hentetResultat = repository.hent(BeregningsresultatPerioder.class, id);
        assertThat(hentetResultat).isNotNull();
        assertThat(hentetResultat.getBeregningsresultatFeriepenger()).isPresent();
        assertThat(hentetResultat.getBeregningsresultatFeriepenger()).hasValueSatisfying(this::assertFeriepenger);
    }

    private void assertFeriepenger(BeregningsresultatFeriepenger hentetFeriepenger) {
        List<BeregningsresultatFeriepengerPrÅr> prÅrListe = hentetFeriepenger.getBeregningsresultatFeriepengerPrÅrListe();
        assertThat(prÅrListe).hasOnlyOneElementSatisfying(beregningsresultatFeriepengerPrÅr -> {
            assertThat(beregningsresultatFeriepengerPrÅr.getBeregningsresultatAndel()).isNotNull();
            assertThat(beregningsresultatFeriepengerPrÅr.getOpptjeningsår()).isNotNull();
            assertThat(beregningsresultatFeriepengerPrÅr.getÅrsbeløp()).isNotNull();
        });
    }

    private void assertBeregningsresultatPeriode(BeregningsresultatPeriode brPeriodeLest, BeregningsresultatAndel brAndelLest, BeregningsresultatPeriode brPeriodeExpected) {
        assertThat(brPeriodeLest).isEqualTo(brPeriodeExpected);
        assertThat(brPeriodeLest.getBeregningsresultatAndelList()).hasSize(1);
        assertThat(brAndelLest).isEqualTo(brPeriodeExpected.getBeregningsresultatAndelList().get(0));
        assertThat(brPeriodeLest.getBeregningsresultatPeriodeFom()).isEqualTo(brPeriodeExpected.getBeregningsresultatPeriodeFom());
        assertThat(brPeriodeLest.getBeregningsresultatPeriodeTom()).isEqualTo(brPeriodeExpected.getBeregningsresultatPeriodeTom());
    }

    @Test
    public void toBehandlingerKanHaSammeBeregningsresultatFP() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        Behandling behandling2 = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningsresultatPerioder Beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat, Beregningsresultat);
        Behandlingsresultat behandlingsresultat2 = behandlingRepository.hentResultat(behandling2.getId());
        beregningsresultatFPRepository.lagre(behandlingsresultat2, Beregningsresultat);

        // Assert
        Optional<BeregningsresultatPerioder> beregningsresultatFP1 = beregningsresultatFPRepository.hentHvisEksisterer(behandlingsresultat);
        Optional<BeregningsresultatPerioder> beregningsresultatFP2 = beregningsresultatFPRepository.hentHvisEksisterer(behandlingsresultat2);
        assertThat(beregningsresultatFP1).isPresent();
        assertThat(beregningsresultatFP2).isPresent();
        assertThat(beregningsresultatFP1).hasValueSatisfying(b -> assertThat(b).isSameAs(beregningsresultatFP2.get())); //NOSONAR
    }

    @Test
    public void slettBeregningsresultatFPOgKobling() {
        // Arrange
        BeregningsresultatPerioder beregningsresultat = buildBeregningsresultatFP(Optional.of(dagensDato));
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        Long beregningsresultatFPId = beregningsresultatFPRepository.lagre(behandlingsresultat, beregningsresultat);

        Optional<BeregningsResultat> koblingOpt = beregningsresultatFPRepository.hentHvisEksistererFor(behandlingsresultat);

        // Act
        beregningsresultatFPRepository.deaktiverBeregningsresultat(behandlingsresultat, behandlingRepository.taSkriveLås(behandling));

        //Assert
        BeregningsresultatPerioder hentetBG = repoRule.getEntityManager().find(BeregningsresultatPerioder.class, beregningsresultatFPId);
        assertThat(hentetBG).isNotNull();

        BeregningsresultatPeriode beregningsresultatPeriode = beregningsresultat.getBeregningsresultatPerioder().get(0);
        BeregningsresultatPeriode hentetBGPeriode = repoRule.getEntityManager().find(BeregningsresultatPeriode.class, beregningsresultatPeriode.getId());
        assertThat(hentetBGPeriode).isNotNull();

        BeregningsresultatAndel beregningsresultatAndel = beregningsresultatPeriode.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel hentetBRAndel = repoRule.getEntityManager().find(BeregningsresultatAndel.class, beregningsresultatAndel.getId());
        assertThat(hentetBRAndel).isNotNull();

        Optional<BeregningsresultatPerioder> deaktivertBeregningsresultatFP = beregningsresultatFPRepository.hentHvisEksisterer(behandlingsresultat);
        Optional<BeregningsResultat> deaktivertKobling = beregningsresultatFPRepository.hentHvisEksistererFor(behandlingsresultat);
        assertThat(deaktivertBeregningsresultatFP).isNotPresent();
        assertThat(deaktivertKobling).isNotPresent();
        assertThat(koblingOpt).hasValueSatisfying(kobling ->
            assertThat(kobling.erAktivt()).isFalse());
    }

    private BeregningsresultatAndel buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode) {
        return beregningsresultatAndelBuilder
            .medBrukerErMottaker(true)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medVirksomhet(beregningVirksomhet)
            .medDagsats(2160)
            .medDagsatsFraBg(2160)
            .medUtbetalingsgrad(BigDecimal.valueOf(10000,2))
            .medStillingsprosent(BigDecimal.valueOf(10000,2))
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatPerioder beregningsresultat) {
        return brPeriodebuilder
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .build(beregningsresultat);
    }

    private BeregningsresultatPerioder buildBeregningsresultatFP(Optional<LocalDate> endringsdato) {
        BeregningsresultatPerioder.Builder builder = beregningsresultatFPBuilder
            .medRegelInput("clob1")
            .medRegelSporing("clob2");
        endringsdato.ifPresent(builder::medEndringsdato);
        BeregningsresultatPerioder beregningsresultat = builder.build();
        BeregningsresultatPeriode brPeriode = buildBeregningsresultatPeriode(beregningsresultat);
        buildBeregningsresultatAndel(brPeriode);
        return beregningsresultat;
    }

}
