package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;


public class StartpunktUtlederInntektsmeldingTest {

    private static final BigDecimal INNTEKTBELØP_DEFAULT = new BigDecimal(30000);
    private static final String ARBEIDSID_DEFAULT = "Arbeidid_123";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private BeregningsresultatFPRepository beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();

    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    @Mock
    InntektsmeldingAggregat førstegangsbehandlingIMAggregat;

    @Mock
    InntektArbeidYtelseGrunnlag førstegangsbehandlingGrunnlagIAY;

    @Mock
    InntektArbeidYtelseGrunnlag revurderingGrunnlagIAY;

    @Mock
    private FørstePermisjonsdagTjeneste førstePermisjonsdagTjeneste;

    private StartpunktUtlederInntektsmelding utleder;
    private static final BigDecimal ARBEIDSPROSENT_30 = new BigDecimal(30);

    @Before
    public void oppsett() {
        initMocks(this);
        utleder = new StartpunktUtlederInntektsmelding(inntektArbeidYtelseTjeneste, førstePermisjonsdagTjeneste, beregningsresultatFPRepository);
    }

    @Test
    public void skal_returnere_inngangsvilkår_dersom_endring_på_første_permisjonsdag_mellom_ny_IM_og_vedtaksgrunnlaget() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling førstegangsbehandling = opprettFørstegangsbehandling();

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(førstegangsbehandling)).thenReturn(Optional.of(førsteUttaksdato));

        // Arrange - opprette revurderingsbehandling
        Behandling revurdering = opprettRevurdering(førstegangsbehandling);

        LocalDate endretUttaksdato = førsteUttaksdato.plusWeeks(1);
        List<Inntektsmelding> revurderingIM =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, endretUttaksdato, ARBEIDSID_DEFAULT);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurdering)).thenReturn(Optional.of(revurderingGrunnlagIAY));
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurdering)).thenReturn(revurderingIM);

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurdering, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP);
    }

    @Test
    public void skal_returnere_beregning_dersom_innsendingsårsak_er_ny() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling førstegangsbehandling = opprettFørstegangsbehandling();

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(førstegangsbehandling)).thenReturn(Optional.of(førsteUttaksdato));

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(førstegangsbehandling);

        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.BEREGNING);
    }

    @Test
    public void skal_returnere_beregning_dersom_endring_på_inntekt_mellom_ny_IM_og_grunnlag_IM() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling behandling = opprettFørstegangsbehandling();

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(behandling)).thenReturn(Optional.of(førsteUttaksdato));

        BigDecimal førstegangsbehandlingInntekt = new BigDecimal(30000);
        List<Inntektsmelding> førstegangsbehandlingIM =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, førstegangsbehandlingInntekt, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(førstegangsbehandlingGrunnlagIAY.getInntektsmeldinger()).thenReturn(Optional.of(førstegangsbehandlingIMAggregat));
        when(førstegangsbehandlingIMAggregat.getInntektsmeldinger()).thenReturn(førstegangsbehandlingIM);

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(behandling);

        BigDecimal revurderingInntekt = førstegangsbehandlingInntekt.add(new BigDecimal(1000));
        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.ENDRING, revurderingInntekt, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.BEREGNING);
    }

    @Test
    public void skal_returnere_beregning_dersom_endring_på_natural_ytelser_mellom_ny_IM_og_grunnlag_IM() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling behandling = opprettFørstegangsbehandling();

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(behandling)).thenReturn(Optional.of(førsteUttaksdato));

        List<Inntektsmelding> førstegangsbehandlingIM = lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(førstegangsbehandlingGrunnlagIAY.getInntektsmeldinger()).thenReturn(Optional.of(førstegangsbehandlingIMAggregat));
        when(førstegangsbehandlingIMAggregat.getInntektsmeldinger()).thenReturn(førstegangsbehandlingIM);

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(behandling);

        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak = lagInntektsmeldingMedNaturalytelse(InntektsmeldingInnsendingsårsak.ENDRING, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT, new NaturalYtelseEntitet(LocalDate.now(), LocalDate.now().plusDays(1), new BigDecimal(30), null));
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.BEREGNING);
    }

    @Test
    public void skal_returnere_uttak_dersom_ingen_endring_i_permisjonsdag_inntekt_naturytelser_eller_refusjon() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling behandling = opprettFørstegangsbehandling();

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(behandling)).thenReturn(Optional.of(førsteUttaksdato));

        List<Inntektsmelding> førstegangsbehandlingIM =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(førstegangsbehandlingGrunnlagIAY.getInntektsmeldinger()).thenReturn(Optional.of(førstegangsbehandlingIMAggregat));
        when(førstegangsbehandlingIMAggregat.getInntektsmeldinger()).thenReturn(førstegangsbehandlingIM);

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(behandling);

        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.ENDRING, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.UTTAKSVILKÅR);
    }

    @Test
    public void skal_returnere_beregning_dersom_ny_IM_inneholder_gradering_og_andel_i_orginalBahandling_har_dagsats_lik_0() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling behandling = opprettFørstegangsbehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        buildBeregningsresultatAndel(brPeriode,lagVirksomhet("123")  ,1000);
        buildBeregningsresultatAndel(brPeriode,lagVirksomhet("345"),  0);
        beregningsresultatFPRepository.lagre(behandling,beregningsresultatFP);

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(behandling)).thenReturn(Optional.of(førsteUttaksdato));

        List<Inntektsmelding> førstegangsbehandlingIM =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(førstegangsbehandlingGrunnlagIAY.getInntektsmeldinger()).thenReturn(Optional.of(førstegangsbehandlingIMAggregat));
        when(førstegangsbehandlingIMAggregat.getInntektsmeldinger()).thenReturn(førstegangsbehandlingIM);

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(behandling);
        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak =
            lagInntektsmeldingMedGradering(InntektsmeldingInnsendingsårsak.ENDRING, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT,ARBEIDSPROSENT_30);

        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.BEREGNING);
    }

    @Test
    public void skal_returnere_uttak_dersom_ny_IM_inneholder_gradering_og_kunn_ett_arbeidsforhold_i_orginalbehandling() {
        // Arrange - opprette avsluttet førstegangsbehandling
        Behandling behandling = opprettFørstegangsbehandling();

        BeregningsresultatFP beregningsresultatFP = lagBeregningsresultatFP();
        BeregningsresultatPeriode brPeriode = lagBeregningsresultatPeriode(beregningsresultatFP);
        buildBeregningsresultatAndel(brPeriode,lagVirksomhet("123")  ,1000);
        beregningsresultatFPRepository.lagre(behandling,beregningsresultatFP);

        LocalDate førsteUttaksdato = LocalDate.now();
        when(førstePermisjonsdagTjeneste.henteFørstePermisjonsdag(behandling)).thenReturn(Optional.of(førsteUttaksdato));

        List<Inntektsmelding> førstegangsbehandlingIM =
            lagInntektsmelding(InntektsmeldingInnsendingsårsak.NY, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT);
        when(førstegangsbehandlingGrunnlagIAY.getInntektsmeldinger()).thenReturn(Optional.of(førstegangsbehandlingIMAggregat));
        when(førstegangsbehandlingIMAggregat.getInntektsmeldinger()).thenReturn(førstegangsbehandlingIM);

        // Arrange - opprette revurderingsbehandling
        Behandling revurderingsBehandling = opprettRevurdering(behandling);
        List<Inntektsmelding> inntektsmeldingerMottattEtterVedtak =
            lagInntektsmeldingMedGradering(InntektsmeldingInnsendingsårsak.ENDRING, INNTEKTBELØP_DEFAULT, førsteUttaksdato, ARBEIDSID_DEFAULT,ARBEIDSPROSENT_30);

        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurderingsBehandling)).thenReturn(inntektsmeldingerMottattEtterVedtak);
        when(inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(revurderingsBehandling)).thenReturn(Optional.of(revurderingGrunnlagIAY));

        // Act/Assert
        assertThat(utleder.utledStartpunkt(revurderingsBehandling, førstegangsbehandlingGrunnlagIAY, revurderingGrunnlagIAY)).isEqualTo(StartpunktType.UTTAKSVILKÅR);
    }

    private Behandling opprettRevurdering(Behandling førstegangsbehandling) {
        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.UDEFINERT);
        return revurderingScenario.lagre(repositoryProvider);
    }

    private Behandling opprettFørstegangsbehandling() {
        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling førstegangsbehandling = førstegangScenario.lagre(repositoryProvider);
        førstegangsbehandling.avsluttBehandling();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(førstegangsbehandling);
        behandlingRepository.lagre(førstegangsbehandling, behandlingLås);
        return førstegangsbehandling;
    }

    private List<Inntektsmelding> lagInntektsmelding(InntektsmeldingInnsendingsårsak innsendingsårsak, BigDecimal beløp, LocalDate førsteUttaksdato, String arbeidID) {
        List<Inntektsmelding> inntektsmeldingerGrunnlag = new ArrayList<>();
        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medBeløp(beløp)
            .medArbeidsforholdId(arbeidID)
            .medStartDatoPermisjon(førsteUttaksdato)
            .medVirksomhet(new VirksomhetEntitet.Builder().medOrgnr("123").build())
            .medInntektsmeldingaarsak(innsendingsårsak)
            .build();

        inntektsmeldingerGrunnlag.add(inntektsmelding);
        return inntektsmeldingerGrunnlag;
    }

    private InntektsmeldingBuilder getInntektsmeldingBuilder() {
        return InntektsmeldingBuilder.builder().medInnsendingstidspunkt(LocalDateTime.now());
    }

    private List<Inntektsmelding> lagInntektsmeldingMedGradering(InntektsmeldingInnsendingsårsak innsendingsårsak, BigDecimal beløp, LocalDate førsteUttaksdato, String arbeidID, BigDecimal arbeidsProsent) {
        List<Inntektsmelding> inntektsmeldingerGrunnlag = new ArrayList<>();
        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medBeløp(beløp)
            .medArbeidsforholdId(arbeidID)
            .medStartDatoPermisjon(førsteUttaksdato)
            .medVirksomhet(new VirksomhetEntitet.Builder().medOrgnr("123").build())
            .medInntektsmeldingaarsak(innsendingsårsak)
            .leggTil(new GraderingEntitet(LocalDate.now(),LocalDate.now().plusWeeks(1),arbeidsProsent))
            .build();

        inntektsmeldingerGrunnlag.add(inntektsmelding);
        return inntektsmeldingerGrunnlag;
    }

    private List<Inntektsmelding> lagInntektsmeldingMedNaturalytelse(InntektsmeldingInnsendingsårsak innsendingsårsak, BigDecimal beløp, LocalDate førsteUttaksdato, String arbeidID, NaturalYtelseEntitet naturalYtelseEntitet) {
        List<Inntektsmelding> inntektsmeldingerGrunnlag = new ArrayList<>();
        Inntektsmelding inntektsmelding = getInntektsmeldingBuilder()
            .medBeløp(beløp)
            .medArbeidsforholdId(arbeidID)
            .medVirksomhet(new VirksomhetEntitet.Builder().medOrgnr("123").build())
            .medStartDatoPermisjon(førsteUttaksdato)
            .medInntektsmeldingaarsak(innsendingsårsak)
            .leggTil(naturalYtelseEntitet)
            .build();

        inntektsmeldingerGrunnlag.add(inntektsmelding);
        return inntektsmeldingerGrunnlag;
    }
    private BeregningsresultatAndel buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode,VirksomhetEntitet virksomhetEntitet ,int dagsats) {
        return BeregningsresultatAndel.builder()
            .medBrukerErMottaker(true)
            .medVirksomhet(virksomhetEntitet)
            .medArbforholdType(OpptjeningAktivitetType.ARBEID)
            .medDagsats(dagsats)
            .medDagsatsFraBg(2160)
            .medUtbetalingsgrad(BigDecimal.valueOf(100))
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private BeregningsresultatPeriode lagBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().minusDays(20), LocalDate.now().minusDays(15))
            .build(beregningsresultatFP);
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
    private BeregningsresultatFP lagBeregningsresultatFP() {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        return beregningsresultatFP;
    }
}
