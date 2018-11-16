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

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFPKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepenger;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFeriepengerPrÅr;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class BeregningsresultatFPRepositoryImplTest {

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final Repository repository = repoRule.getRepository();

    private final BeregningsresultatFPRepository beregningsresultatFPRepository = new BeregningsresultatFPRepositoryImpl(repoRule.getEntityManager());

    private BeregningsresultatFP.Builder beregningsresultatFPBuilder;
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

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        dagensDato = LocalDate.now();

        beregningsresultatFPBuilder = BeregningsresultatFP.builder();
        brPeriodebuilder = BeregningsresultatPeriode.builder();
        beregningsresultatAndelBuilder = BeregningsresultatAndel.builder();

    }

    @Test
    public void lagreOgHentBeregningsresultatFPkobling() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Assert
        Optional<BeregningsresultatFPKobling> BeregningsresultatFPKoblingOptional = beregningsresultatFPRepository.hentBeregningsresultatFPKobling(behandling);
        assertThat(BeregningsresultatFPKoblingOptional).isPresent();
    }

    @Test
    public void lagreOgHenteBeregningsresultatFP() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Assert
        Long id = beregningsresultatFP.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Optional<BeregningsresultatFP> beregningsresultatFPLest = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);

        assertThat(beregningsresultatFPLest).isEqualTo(Optional.of(beregningsresultatFP));
    }

    @Test
    public void lagreBeregningsresultatFPOgUnderliggendeTabellerMedEndringsdatoLikDagensDato() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Assert
        Long brId = beregningsresultatFP.getId();
        assertThat(brId).isNotNull();
        BeregningsresultatPeriode brPeriode = beregningsresultatFP.getBeregningsresultatPerioder().get(0);
        Long brPeriodeId = brPeriode.getId();
        assertThat(brPeriodeId).isNotNull();
        Long brAndelId = brPeriode.getBeregningsresultatAndelList().get(0).getId();

        repository.flushAndClear();
        BeregningsresultatFP beregningsresultatFPLest = repository.hent(BeregningsresultatFP.class, brId);
        BeregningsresultatPeriode brPeriodeLest = repository.hent(BeregningsresultatPeriode.class, brPeriodeId);
        BeregningsresultatAndel brAndelLest = repository.hent(BeregningsresultatAndel.class, brAndelId);

        assertThat(beregningsresultatFPLest.getId()).isNotNull();
        assertThat(beregningsresultatFPLest.getBeregningsresultatPerioder()).hasSize(1);
        assertThat(beregningsresultatFPLest.getRegelInput()).isEqualTo(beregningsresultatFP.getRegelInput());
        assertThat(beregningsresultatFPLest.getRegelSporing()).isEqualTo(beregningsresultatFP.getRegelSporing());
        assertThat(beregningsresultatFPLest.getEndringsdato()).isEqualTo(Optional.of(dagensDato));
        assertBeregningsresultatPeriode(brPeriodeLest, brAndelLest, brPeriode);
    }

    @Test
    public void lagreBeregningsresultatFPOgUnderliggendeTabellerMedTomEndringsdato() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.empty());

        // Act
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Assert
        Long brId = beregningsresultatFP.getId();
        assertThat(brId).isNotNull();
        BeregningsresultatPeriode brPeriode = beregningsresultatFP.getBeregningsresultatPerioder().get(0);
        Long brPeriodeId = brPeriode.getId();
        assertThat(brPeriodeId).isNotNull();
        Long brAndelId = brPeriode.getBeregningsresultatAndelList().get(0).getId();

        repository.flushAndClear();
        BeregningsresultatFP beregningsresultatFPLest = repository.hent(BeregningsresultatFP.class, brId);
        BeregningsresultatPeriode brPeriodeLest = repository.hent(BeregningsresultatPeriode.class, brPeriodeId);
        BeregningsresultatAndel brAndelLest = repository.hent(BeregningsresultatAndel.class, brAndelId);

        assertThat(beregningsresultatFPLest.getId()).isNotNull();
        assertThat(beregningsresultatFPLest.getBeregningsresultatPerioder()).hasSize(1);
        assertThat(beregningsresultatFPLest.getRegelInput()).isEqualTo(beregningsresultatFP.getRegelInput());
        assertThat(beregningsresultatFPLest.getRegelSporing()).isEqualTo(beregningsresultatFP.getRegelSporing());
        assertThat(beregningsresultatFPLest.getEndringsdato()).isEmpty();
        assertBeregningsresultatPeriode(brPeriodeLest, brAndelLest, brPeriode);
    }

    @Test
    public void lagreBeregningsresultatFPOgFeriepenger() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));
        BeregningsresultatFeriepenger feriepenger = BeregningsresultatFeriepenger.builder()
            .medFeriepengerPeriodeFom(LocalDate.now())
            .medFeriepengerPeriodeTom(LocalDate.now())
            .medFeriepengerRegelInput("-")
            .medFeriepengerRegelSporing("-")
            .build(beregningsresultatFP);

        BeregningsresultatAndel andel = beregningsresultatFP.getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList().get(0);
        BeregningsresultatFeriepengerPrÅr.builder()
            .medOpptjeningsår(LocalDate.now().withMonth(12).withDayOfMonth(31))
            .medÅrsbeløp(300L)
            .build(feriepenger, andel);

        // Act
        long id = beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        // Assert
        repository.flushAndClear();
        BeregningsresultatFP hentetResultat = repository.hent(BeregningsresultatFP.class, id);
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
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling2 = scenario.lagre(repositoryProvider);
        BeregningsresultatFP BeregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));

        // Act
        beregningsresultatFPRepository.lagre(behandling, BeregningsresultatFP);
        beregningsresultatFPRepository.lagre(behandling2, BeregningsresultatFP);

        // Assert
        Optional<BeregningsresultatFP> beregningsresultatFP1 = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        Optional<BeregningsresultatFP> beregningsresultatFP2 = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling2);
        assertThat(beregningsresultatFP1).isPresent();
        assertThat(beregningsresultatFP2).isPresent();
        assertThat(beregningsresultatFP1).hasValueSatisfying(b -> assertThat(b).isSameAs(beregningsresultatFP2.get())); //NOSONAR
    }

    @Test
    public void slettBeregningsresultatFPOgKobling() {
        // Arrange
        BeregningsresultatFP beregningsresultatFP = buildBeregningsresultatFP(Optional.of(dagensDato));
        Long beregningsresultatFPId = beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);

        Optional<BeregningsresultatFPKobling> koblingOpt = beregningsresultatFPRepository.hentBeregningsresultatFPKobling(behandling);

        // Act
        beregningsresultatFPRepository.deaktiverBeregningsresultatFP(behandling, behandlingRepository.taSkriveLås(behandling));

        //Assert
        BeregningsresultatFP hentetBG = repoRule.getEntityManager().find(BeregningsresultatFP.class, beregningsresultatFPId);
        assertThat(hentetBG).isNotNull();

        BeregningsresultatPeriode beregningsresultatPeriode = beregningsresultatFP.getBeregningsresultatPerioder().get(0);
        BeregningsresultatPeriode hentetBGPeriode = repoRule.getEntityManager().find(BeregningsresultatPeriode.class, beregningsresultatPeriode.getId());
        assertThat(hentetBGPeriode).isNotNull();

        BeregningsresultatAndel beregningsresultatAndel = beregningsresultatPeriode.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel hentetBRAndel = repoRule.getEntityManager().find(BeregningsresultatAndel.class, beregningsresultatAndel.getId());
        assertThat(hentetBRAndel).isNotNull();

        Optional<BeregningsresultatFP> deaktivertBeregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        Optional<BeregningsresultatFPKobling> deaktivertKobling = beregningsresultatFPRepository.hentBeregningsresultatFPKobling(behandling);
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
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP) {
        return brPeriodebuilder
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .build(beregningsresultatFP);
    }

    private BeregningsresultatFP buildBeregningsresultatFP(Optional<LocalDate> endringsdato) {
        BeregningsresultatFP.Builder builder = beregningsresultatFPBuilder
            .medRegelInput("clob1")
            .medRegelSporing("clob2");
        endringsdato.ifPresent(builder::medEndringsdato);
        BeregningsresultatFP beregningsresultatFP = builder.build();
        BeregningsresultatPeriode brPeriode = buildBeregningsresultatPeriode(beregningsresultatFP);
        buildBeregningsresultatAndel(brPeriode);
        return beregningsresultatFP;
    }

}
