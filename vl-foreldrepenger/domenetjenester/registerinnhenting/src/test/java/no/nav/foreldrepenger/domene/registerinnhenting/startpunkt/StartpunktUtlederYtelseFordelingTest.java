package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.BEREGNING;
import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType.UTTAKSVILKÅR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;

public class StartpunktUtlederYtelseFordelingTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private StartpunktUtlederYtelseFordeling utleder;
    private YtelsesFordelingRepository ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
    private BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

    private static final BigDecimal ARBEIDSPROSENT_30 = new BigDecimal(30);

    @Mock
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));

    @Before
    public void oppsett() {
        initMocks(this);
        utleder = new StartpunktUtlederYtelseFordeling(skjæringstidspunktTjeneste, ytelsesFordelingRepository,  beregningsresultatFPRepository);
    }

    @Test
    public void skal_returnere_inngangsvilkår_dersom_skjæringstidspunkt_er_endret() {
        // Arrange
        Behandling originalBehandling = lagFørstegangsBehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        beregningsresultatFPRepository.lagre(originalBehandling,beregningsresultatFP);

        Behandling revurdering = lagRevurdering(originalBehandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);

        opprettYtelsesFordeling(revurdering);

        LocalDate førsteuttaksdato = LocalDate.now();
        LocalDate endretUttaksdato = førsteuttaksdato.plusDays(1);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(originalBehandling)).thenReturn(førsteuttaksdato);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(revurdering)).thenReturn(endretUttaksdato);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_returnere_beregning_dersom_søker_gradering_på_andel_uten_dagsats() {
        // Arrange
        Behandling originalBehandling = lagFørstegangsBehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        buildBeregningsresultatAndel(brPeriode,lagVirksomhet("123"), 1000);
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("345"), 0);
        beregningsresultatFPRepository.lagre(originalBehandling,beregningsresultatFP);

        Behandling revurdering = lagRevurdering(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        opprettYtelsesFordelingMedGradering(revurdering, ARBEIDSPROSENT_30);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(BEREGNING);
    }

    @Test
    public void startpunkt_uttak_dersom_søknad_gradering_og_orig_behandling_har_ingen_aktiviter_lik_null_dagsats() {
        // Arrange
        Behandling originalBehandling = lagFørstegangsBehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        // Samme aktivitet er delt i 2 andeler, men minst én har dagsats > 0
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("123"), 1000);
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("123"), 0);
        beregningsresultatFPRepository.lagre(originalBehandling,beregningsresultatFP);

        Behandling revurdering = lagRevurdering(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        opprettYtelsesFordelingMedGradering(revurdering, ARBEIDSPROSENT_30);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(UTTAKSVILKÅR);
    }

    @Test
    public void startpunkt_beregning_dersom_søknad_gradering_og_orig_behandling_har_en_aktivitet_lik_null_dagsats() {
        // Arrange
        Behandling originalBehandling = lagFørstegangsBehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        // Aktivitet 1 er delt i 2 andeler, men minst én har dagsats > 0
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("123"), 1000);
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("123"), 0);
        // Aktivitet 2 har andel 0 => startpunkt Beregning
        buildBeregningsresultatAndel(brPeriode, lagVirksomhet("456"), 0);
        beregningsresultatFPRepository.lagre(originalBehandling,beregningsresultatFP);

        Behandling revurdering = lagRevurdering(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        opprettYtelsesFordelingMedGradering(revurdering, ARBEIDSPROSENT_30);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(BEREGNING);
    }

    @Test
    public void skal_returnere_uttak_dersom_søker_gradering_og_kunn_ett_arbeidsforhold_i_orginalbehandling() {
        // Arrange
        Behandling originalBehandling = lagFørstegangsBehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        buildBeregningsresultatAndel(brPeriode , lagVirksomhet("123"), 1000);
        beregningsresultatFPRepository.lagre(originalBehandling,beregningsresultatFP);

        Behandling revurdering = lagRevurdering(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        opprettYtelsesFordelingMedGradering(revurdering, ARBEIDSPROSENT_30);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, 1L, 2L)).isEqualTo(UTTAKSVILKÅR);
    }

    private Behandling lagFørstegangsBehandling(){
        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD);
        return førstegangScenario.lagre(repositoryProvider);
    }

    private Behandling lagRevurdering(Behandling originalBehandling, BehandlingÅrsakType behandlingÅrsakType){
        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING);
        revurderingScenario.medOriginalBehandling(originalBehandling, behandlingÅrsakType);
        return revurderingScenario.lagre(repositoryProvider);
    }

    private void opprettYtelsesFordeling(Behandling revurdering) {
            opprettYtelsesFordelingMedGradering(revurdering,BigDecimal.ZERO);
    }
    private void opprettYtelsesFordelingMedGradering(Behandling behandling, BigDecimal arbeidsProsent){
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now(), LocalDate.now().plusDays(7))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medArbeidsprosent(arbeidsProsent)
            .build();
        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(Collections.singletonList(periode), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
    }

    private void buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, VirksomhetEntitet virksomhetEntitet, int dagsats) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(true)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medDagsats(dagsats)
            .medDagsatsFraBg(2160)
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medVirksomhet(virksomhetEntitet)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private BeregningsresultatPeriode lagBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .build(beregningsresultatFP);
    }

    private BeregningsresultatFP lagBeregningsresultatFP() {
        return BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
    }

    private VirksomhetEntitet lagVirksomhet(String orgnr) {
        Optional<Virksomhet> virksomhetOpt = virksomhetRepository.hent(orgnr);
        if (!virksomhetOpt.isPresent()) {
            VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(orgnr)
                .medNavn("BeregningVirksomhet for " + orgnr)
                .oppdatertOpplysningerNå()
                .build();
            virksomhetRepository.lagre(virksomhet);
            return virksomhet;
        } else {
            return (VirksomhetEntitet) virksomhetOpt.get();
        }
    }
}
