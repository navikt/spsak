package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class BeregningsgrunnlagRepositoryImplTest {

    private static final BeregningsgrunnlagTilstand STEG_OPPRETTET = BeregningsgrunnlagTilstand.OPPRETTET;
    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final Repository repository = repoRule.getRepository();
    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository = new BeregningsgrunnlagRepositoryImpl(entityManager, repositoryProvider.getBehandlingLåsRepository());

    private Behandling behandling;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private VirksomhetEntitet beregningVirksomhet;

    @Before
    public void setup() {
        beregningVirksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("55")
            .medNavn("BeregningVirksomheten")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medAntallBarn(1).medFødselsDato(LocalDate.now());
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void lagreOgHentBeregningsgrunnlagGrunnlagEntitet() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);

        // Assert
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(entitetOpt).as("entitetOpt").hasValueSatisfying(entitet -> assertThat(entitet.erAktivt()).isTrue());
    }

    @Test
    public void skalHentSisteBeregningsgrunnlagGrunnlagEntitet() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag1 = buildBeregningsgrunnlag();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag1, STEG_OPPRETTET);

        Beregningsgrunnlag beregningsgrunnlag2 = buildBeregningsgrunnlag();
        long beregningsgrunnlagId2 = beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag2, STEG_OPPRETTET);

        Beregningsgrunnlag beregningsgrunnlag3 = buildBeregningsgrunnlag();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag3, BeregningsgrunnlagTilstand.FORESLÅTT);

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, STEG_OPPRETTET);

        // Assert
        assertThat(entitetOpt).as("entitetOpt").hasValueSatisfying(entitet -> {
            assertThat(entitet.erAktivt()).isFalse();
            assertThat(entitet.getBeregningsgrunnlag().getId()).isEqualTo(beregningsgrunnlagId2);
        });
    }

    @Test
    public void skalReaktiverBeregningsgrunnlagGrunnlagEntitet() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag1 = buildBeregningsgrunnlag();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag1, STEG_OPPRETTET);

        Beregningsgrunnlag beregningsgrunnlag2 = buildBeregningsgrunnlag();
        long beregningsgrunnlagId2 = beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag2, STEG_OPPRETTET);

        Beregningsgrunnlag beregningsgrunnlag3 = buildBeregningsgrunnlag();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag3, BeregningsgrunnlagTilstand.FORESLÅTT);

        // Act
        beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(behandling, STEG_OPPRETTET);
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);

        assertThat(entitetOpt).as("entitetOpt").hasValueSatisfying(entitet -> {
            assertThat(entitet.erAktivt()).as("bg.aktiv").isTrue();
            assertThat(entitet.getBeregningsgrunnlag().getId()).as("bg.id").isEqualTo(beregningsgrunnlagId2);
        });
    }

    @Test
    public void lagreOgHenteBeregningsgrunnlag() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);

        // Assert
        Long id = beregningsgrunnlag.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        Optional<Beregningsgrunnlag> beregningsgrunnlagLest = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(beregningsgrunnlagLest).isEqualTo(Optional.of(beregningsgrunnlag));
    }

    @Test
    public void lagreOgHenteBeregningsgrunnlagMedPrivatpersonSomArbgiver() {
        // Arrange
        String aktørId = "9482747652093";
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlagPrivatpersonArbgiver(aktørId);

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);

        // Assert
        Long id = beregningsgrunnlag.getId();
        assertThat(id).isNotNull();
        BGAndelArbeidsforhold arbFor = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBgAndelArbeidsforhold().get();
        assertThat(arbFor.getArbeidsgiver().get().getIdentifikator()).isEqualTo(aktørId);

        repository.flushAndClear();
        Optional<Beregningsgrunnlag> beregningsgrunnlagLest = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(beregningsgrunnlagLest).isEqualTo(Optional.of(beregningsgrunnlag));
        arbFor = beregningsgrunnlagLest.get().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0).getBgAndelArbeidsforhold().get();
        assertThat(arbFor.getArbeidsgiver().get().getIdentifikator()).isEqualTo(aktørId);

    }

    private Beregningsgrunnlag buildBeregningsgrunnlagPrivatpersonArbgiver(String aktørId) {
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();
        BeregningsgrunnlagPrStatusOgAndel andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BGAndelArbeidsforhold bgArbFor = andel.getBgAndelArbeidsforhold().get();
        BGAndelArbeidsforhold.Builder bgBuilder = BGAndelArbeidsforhold
            .builder(bgArbFor)
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(aktørId)));
        BeregningsgrunnlagPrStatusOgAndel.builder(andel).medBGAndelArbeidsforhold(bgBuilder).build(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0));
        return beregningsgrunnlag;
    }


    @Test
    public void lagreBeregningsgrunnlagOgUnderliggendeTabeller() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);

        // Assert
        Long bgId = beregningsgrunnlag.getId();
        assertThat(bgId).isNotNull();
        Long bgAktivitetStatusId = beregningsgrunnlag.getAktivitetStatuser().get(0).getId();
        assertThat(bgAktivitetStatusId).isNotNull();
        Long sammenlingningsgrId = beregningsgrunnlag.getSammenligningsgrunnlag().getId();
        assertThat(sammenlingningsgrId).isNotNull();
        BeregningsgrunnlagPeriode bgPeriodeLagret = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Long bgPeriodeId = bgPeriodeLagret.getId();
        assertThat(bgPeriodeId).isNotNull();
        Long bgPrStatusOgAndelId = bgPeriodeLagret.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getId();
        assertThat(bgPrStatusOgAndelId).isNotNull();
        Long bgPeriodeÅrsakId = bgPeriodeLagret.getBeregningsgrunnlagPeriodeÅrsaker().get(0).getId();
        assertThat(bgPeriodeÅrsakId).isNotNull();


        repository.flushAndClear();
        Beregningsgrunnlag beregningsgrunnlagLest = repository.hent(Beregningsgrunnlag.class, bgId);
        BeregningsgrunnlagAktivitetStatus bgAktivitetStatusLest = repository.hent(BeregningsgrunnlagAktivitetStatus.class, bgAktivitetStatusId);
        Sammenligningsgrunnlag sammenligningsgrunnlagLest = repository.hent(Sammenligningsgrunnlag.class, sammenlingningsgrId);
        BeregningsgrunnlagPeriode bgPeriodeLest = repository.hent(BeregningsgrunnlagPeriode.class, bgPeriodeId);
        BeregningsgrunnlagPrStatusOgAndel bgPrStatusOgAndelLest = repository.hent(BeregningsgrunnlagPrStatusOgAndel.class, bgPrStatusOgAndelId);
        BeregningsgrunnlagPeriodeÅrsak bgPeriodeÅrsakLest = repository.hent(BeregningsgrunnlagPeriodeÅrsak.class, bgPeriodeÅrsakId);

        assertThat(beregningsgrunnlag.getId()).isNotNull();
        assertThat(beregningsgrunnlagLest.getAktivitetStatuser()).hasSize(1);
        assertThat(bgAktivitetStatusLest).isEqualTo(beregningsgrunnlag.getAktivitetStatuser().get(0));
        assertThat(sammenligningsgrunnlagLest).isEqualTo(beregningsgrunnlag.getSammenligningsgrunnlag());
        assertThat(beregningsgrunnlagLest.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlagLest.getRegelloggSkjæringstidspunkt()).isEqualTo(beregningsgrunnlag.getRegelloggSkjæringstidspunkt());
        assertThat(beregningsgrunnlagLest.getRegelloggBrukersStatus()).isEqualTo(beregningsgrunnlag.getRegelloggBrukersStatus());
        assertThat(bgPeriodeLest).isEqualTo(bgPeriodeLagret);
        assertThat(bgPeriodeLest.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(bgPrStatusOgAndelLest).isEqualTo(bgPeriodeLagret.getBeregningsgrunnlagPrStatusOgAndelList().get(0));
        assertThat(bgPeriodeÅrsakLest).isEqualTo(bgPeriodeLagret.getBeregningsgrunnlagPeriodeÅrsaker().get(0));
        assertThat(bgPeriodeLest.getRegelEvaluering()).isEqualTo(bgPeriodeLagret.getRegelEvaluering());
    }

    @Test
    public void toBehandlingerKanHaSammeBeregningsgrunnlag() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medAntallBarn(1).medFødselsDato(LocalDate.now());
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        Behandling behandling2 = scenario.lagre(repositoryProvider);
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);
        beregningsgrunnlagRepository.lagre(behandling2, beregningsgrunnlag, STEG_OPPRETTET);

        // Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt1 = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt2 = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling2);
        assertThat(beregningsgrunnlagOpt1).hasValueSatisfying(beregningsgrunnlag1 ->
            assertThat(beregningsgrunnlagOpt2).hasValueSatisfying(beregningsgrunnlag2 ->
                assertThat(beregningsgrunnlag1).isSameAs(beregningsgrunnlag2)));
    }


    @Test
    public void skalHenteRiktigBeregningsgrunnlagBasertPåId() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medAntallBarn(1).medFødselsDato(LocalDate.now());
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        Behandling behandling2 = scenario.lagre(repositoryProvider);
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();
        Beregningsgrunnlag beregningsgrunnlag2 = Beregningsgrunnlag.builder(Kopimaskin.deepCopy(beregningsgrunnlag))
            .medSkjæringstidspunkt(beregningsgrunnlag.getSkjæringstidspunkt().plusDays(1))
            .build();

        // Act
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);
        beregningsgrunnlagRepository.lagre(behandling2, beregningsgrunnlag, STEG_OPPRETTET);
        beregningsgrunnlagRepository.lagre(behandling2, beregningsgrunnlag2, STEG_OPPRETTET);

        // Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(beregningsgrunnlag2.getId());
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(bg -> assertThat(bg).isSameAs(beregningsgrunnlag2));
    }

    @Test
    public void settBeregningsgrunnlagEntitetIkkeAktiv() {
        // Arrange
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, STEG_OPPRETTET);

        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        assertThat(entitetOpt).isPresent();

        // Act
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(behandling);

        //Assert
        assertThat(entitetOpt).as("entitetOpt").hasValueSatisfying(entitet ->
            assertThat(entitet.erAktivt()).as("entitet.aktiv").isFalse());
    }

    private BeregningsgrunnlagPrStatusOgAndel buildBgPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(beregningVirksomhet))
            .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(3232.32))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        return BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregningsperiode(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5))
            .medYtelse(RelatertYtelseType.FORELDREPENGER)
            .medOverstyrtPrÅr(BigDecimal.valueOf(4444432.32))
            .medAvkortetPrÅr(BigDecimal.valueOf(423.23))
            .medRedusertPrÅr(BigDecimal.valueOf(52335))
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(Beregningsgrunnlag beregningsgrunnlag) {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .medBruttoPrÅr(BigDecimal.valueOf(534343.55))
            .medAvkortetPrÅr(BigDecimal.valueOf(223421.33))
            .medRedusertPrÅr(BigDecimal.valueOf(23412.32))
            .medRegelEvaluering(true, "input1", "clob1")
            .medRegelEvaluering(false, "input2", "clob2")
            .leggTilPeriodeÅrsak(PeriodeÅrsak.UDEFINERT)
            .build(beregningsgrunnlag);
    }

    private Beregningsgrunnlag buildBeregningsgrunnlag() {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(91425))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425))
            .medRegelloggSkjæringstidspunkt("input1", "clob1")
            .medRegelloggBrukersStatus("input2", "clob2")
            .build();
        buildSammenligningsgrunnlag(beregningsgrunnlag);
        buildBgAktivitetStatus(beregningsgrunnlag);
        BeregningsgrunnlagPeriode bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode);
        return beregningsgrunnlag;
    }

    private BeregningsgrunnlagAktivitetStatus buildBgAktivitetStatus(Beregningsgrunnlag beregningsgrunnlag) {
        return BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(beregningsgrunnlag);
    }

    private Sammenligningsgrunnlag buildSammenligningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        return Sammenligningsgrunnlag.builder()
            .medSammenligningsperiode(LocalDate.now().minusDays(12), LocalDate.now().minusDays(6))
            .medRapportertPrÅr(BigDecimal.valueOf(323212.12))
            .medAvvikPromille(120L)
            .build(beregningsgrunnlag);
    }
}
