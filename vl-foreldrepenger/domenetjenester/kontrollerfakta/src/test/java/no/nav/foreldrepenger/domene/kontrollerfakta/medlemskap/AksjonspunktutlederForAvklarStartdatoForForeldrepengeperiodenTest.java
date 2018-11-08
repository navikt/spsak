package no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Spy;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.konfig.Tid;

public class AksjonspunktutlederForAvklarStartdatoForForeldrepengeperiodenTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private YtelsesFordelingRepository ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();

    private MottatteDokumentRepository mottatteDokumentRepository = new MottatteDokumentRepositoryImpl(repoRule.getEntityManager());

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();

    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));

    @Spy
    private AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden utleder = new AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden(repositoryProvider, skjæringstidspunktTjeneste);

    @Test
    public void skal_ikke_opprette_aksjonspunkt_fordi_startdatoer_samsvarer() {
        // Arrange
        AktørId aktørId  = new AktørId("123");
        LocalDate fødselsdato = LocalDate.now().minusDays(1);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        opprettArbeidsforhold(scenario, aktørId, fødselsdato, Tid.TIDENES_ENDE);

        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettOppgittFordeling(fødselsdato, behandling);
        opprettInntektsmelding(fødselsdato, behandling);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_fordi_inget_arbeidsforhold_er_løpende_selvom_startdatoene_ikke_samsvarer() {
        // Arrange
        AktørId aktørId  = new AktørId("123");
        LocalDate fødselsdato = LocalDate.now();

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        opprettArbeidsforhold(scenario, aktørId, fødselsdato, fødselsdato.plusMonths(3L));

        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettOppgittFordeling(fødselsdato, behandling);
        opprettInntektsmelding(fødselsdato.plusDays(2L), behandling);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_fordi_startdatoene_på_alle_løpende_arbeidsforhold_samsvarer_med_oppgitt_av_bruker() {
        // Arrange
        AktørId aktørId  = new AktørId("123");
        LocalDate fødselsdato = LocalDate.now();

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        opprettArbeidsforhold(scenario, aktørId, fødselsdato, Tid.TIDENES_ENDE);

        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettOppgittFordeling(fødselsdato, behandling);
        opprettInntektsmelding(fødselsdato.plusDays(2L), behandling);
        opprettInntektsmelding(fødselsdato, behandling);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_fordi_startdatoene_skal_tolkes_som_påfølgende_mandag() {
        // Arrange
        AktørId aktørId  = new AktørId("123");
        LocalDate fødselsdato = endreDatoTilLørdag(LocalDate.now());

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        opprettArbeidsforhold(scenario, aktørId, fødselsdato, Tid.TIDENES_ENDE);

        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettOppgittFordeling(fødselsdato, behandling);
        opprettInntektsmelding(fødselsdato.plusDays(1L), behandling);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_fordi_startdatoene_på_alle_løpende_arbeidsforhold_ikke_samsvarer_med_oppgitt_av_bruker() {
        // Arrange
        AktørId aktørId  = new AktørId("123");
        LocalDate fødselsdato = endreDatoHvisLørdagEllerSøndag(LocalDate.now());

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(aktørId);
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        scenario.removeDodgyDefaultInntektArbeidYTelse();
        opprettArbeidsforhold(scenario, aktørId, fødselsdato.minusDays(2), Tid.TIDENES_ENDE);

        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettOppgittFordeling(fødselsdato, behandling);
        opprettInntektsmelding(fødselsdato.plusDays(2L), behandling);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.AVKLAR_STARTDATO_FOR_FORELDREPENGEPERIODEN);
    }

    LocalDate endreDatoHvisLørdagEllerSøndag(LocalDate dato) {
        if (dato.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            return dato.plusDays(2L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            return dato.plusDays(1L);
        }
        return dato;
    }

    LocalDate endreDatoTilLørdag(LocalDate dato) {
        if (dato.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
            return dato.plusDays(5L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
            return dato.plusDays(4L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)) {
            return dato.plusDays(3L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
            return dato.plusDays(2L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.FRIDAY)) {
            return dato.plusDays(1L);
        }
        return dato;
    }


    void opprettArbeidsforhold(ScenarioMorSøkerForeldrepenger scenario, AktørId aktørId, LocalDate fødselsdato, LocalDate tom) {
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medAktørId(aktørId);
        builder.medAktivitetsAvtaleFom(fødselsdato);
        builder.medAktivitetsAvtaleTom(tom);
        builder.medOrgNr("45345");
        builder.medOrgNavn("Virksomhet");
        builder.build();
    }

    void opprettOppgittFordeling(LocalDate fødselsdato, Behandling behandling) {
        OppgittPeriode periode1 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(fødselsdato, fødselsdato.plusWeeks(6))
            .build();

        OppgittPeriode periode2 = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .medPeriode(fødselsdato.plusWeeks(6).plusDays(1), fødselsdato.plusWeeks(10))
            .build();

        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Arrays.asList(periode1, periode2), true);
        ytelsesFordelingRepository.lagre(behandling, fordeling);
    }

    void opprettInntektsmelding(LocalDate fødselsdato, Behandling behandling) {
        final MottattDokument build = new MottattDokument.Builder().medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medFagsakId(behandling.getFagsakId())
            .medMottattDato(LocalDate.now())
            .medBehandlingId(behandling.getId())
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId("123123123"))
            .medDokumentId("123123")
            .build();
        mottatteDokumentRepository.lagre(build);

        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(fødselsdato);
        inntektsmeldingBuilder.medBeløp(BigDecimal.TEN);
        inntektsmeldingBuilder.medMottattDokument(build);
        inntektsmeldingBuilder.medInnsendingstidspunkt(LocalDateTime.now());

        Optional<Virksomhet> hent = virksomhetRepository.hent("45345");
        if (hent.isPresent()) {
            Virksomhet virksomhet = hent.get();
            inntektsmeldingBuilder.medVirksomhet(virksomhet);
        }

        inntektArbeidYtelseRepository.lagre(behandling, inntektsmeldingBuilder.build());
    }
}
