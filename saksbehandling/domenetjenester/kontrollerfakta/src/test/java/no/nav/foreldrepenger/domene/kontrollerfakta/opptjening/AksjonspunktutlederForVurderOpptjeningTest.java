package no.nav.foreldrepenger.domene.kontrollerfakta.opptjening;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Spy;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.UtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class AksjonspunktutlederForVurderOpptjeningTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private OpptjeningRepository opptjeningRepository;
    @Spy
    private AksjonspunktutlederForVurderOpptjening utleder = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock(SkjæringstidspunktTjeneste.class));

    @Before
    public void oppsett() {
        initMocks(this);
        opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunktet_5051() {
        // Arrange
        AktørId aktørId1 = new AktørId("123");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_hatt_vartpenger() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.VENTELØNN_VARTPENGER);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_oppgitt_frilansperiode() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.FRILANSER);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_dersom_bekreftet_frilansoppdrag() {
        AktørId aktørId1 = new AktørId("1");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder()
            .medAktørId(aktørId1);
        builder.medYrkesaktivitetArbeidType(ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER)
            .medYrkesaktivitetArbeidsforholdId("123")
            .medAktivitetsAvtaleProsentsats(BigDecimal.ONE)
            .medAktivitetsAvtaleFom(fraOgMed.plusDays(5)).medAktivitetsAvtaleTom(fraOgMed.plusDays(6))
            .medOrgNr("777777777")
            .medInntektspostType(InntektspostType.LØNN)
            .medInntektspostFom(fraOgMed.plusDays(5)).medInntektspostTom(fraOgMed.plusDays(6))
            .medInntektspostBeløp(BigDecimal.TEN);
        builder.build();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        lagreOpptjeningsPeriode(tilOgMed, repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId()));

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_hatt_ventelønn() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.VENTELØNN_VARTPENGER);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_hatt_militær_eller_siviltjeneste() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.MILITÆR_ELLER_SIVILTJENESTE);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_hatt_etterlønn_sluttvederlag() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.ETTERLØNN_SLUTTPAKKE);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_hatt_videre_og_etterutdanning() {
        // Arrange
        Behandling behandling = opprettBehandling(ArbeidType.LØNN_UNDER_UTDANNING);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_er_selvstendig_næringsdrivende_og_ikke_hatt_næringsinntekt_eller_registrert_næringen_senere() {
        // Arrange
        AktørId aktørId = new AktørId("23");
        Behandling behandling = opprettOppgittOpptjening(aktørId, false);
        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }


    @Test
    public void skal_opprette_aksjonspunkt_om_bruker_har_utenlandsforhold() {
        // Arrange
        AktørId aktørId = new AktørId("77");
        Behandling behandling = opprettUtenlandskArbeidsforhold(aktørId);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_bruker_er_selvstendig_næringsdrivende_og_ikke_hatt_næringsinntekt_og_registrert_næringen_senere() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr("45345")
            .medNavn("Virksomhet")
            .medRegistrert(LocalDate.now())
            .medOppstart(LocalDate.now())
            .oppdatertOpplysningerNå()
            .build();

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();
        UtenlandskVirksomhetEntitet svenska_stat = new UtenlandskVirksomhetEntitet(Landkoder.SWE, "Svenska Stat");
        egenNæringBuilder
            .medPeriode(periode)
            .medUtenlandskVirksomhet(svenska_stat)
            .medBegrunnelse("Vet ikke")
            .medBruttoInntekt(BigDecimal.valueOf(100000))
            .medRegnskapsførerNavn("Jacob")
            .medRegnskapsførerTlf("+46678456345")
            .medVirksomhetType(VirksomhetType.FISKE)
            .medVirksomhet(virksomhet);
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        oppgittOpptjeningBuilder
            .leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);

        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medVirksomhetRegistrert(LocalDate.now());
        builder.build();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_bruker_er_selvstendig_næringsdrivende_og_hatt_næringsinntekt() {
        // Arrange
        AktørId aktørId = new AktørId("23");
        Behandling behandling = opprettOppgittOpptjening(aktørId, true);

        //Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_når_ingen_arbeidsavtaler_har_0_stillingsprosent() {
        // Arrange
        AktørId aktørId1 = new AktørId("1");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_når_en_arbeidsavtale_har_0_stillingsprosent() {
        // Arrange
        AktørId aktørId1 = new AktørId("1");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder()
            .medAktørId(aktørId1);
        builder.medAktivitetsAvtaleProsentsats(BigDecimal.ZERO);
        builder.build();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        lagreOpptjeningsPeriode(tilOgMed, repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId()));

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = utleder.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon()).isEqualTo(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
    }

    private Behandling opprettUtenlandskArbeidsforhold(AktørId aktørId) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId, NavBrukerKjønn.KVINNE);

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        UtenlandskVirksomhetEntitet svenska_stat = new UtenlandskVirksomhetEntitet(Landkoder.SWE, "Svenska Stat");
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        oppgittOpptjeningBuilder
            .leggTilOppgittArbeidsforhold(OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder.ny().medUtenlandskVirksomhet(svenska_stat).medPeriode(periode).medErUtenlandskInntekt(true).medArbeidType(ArbeidType.UTENLANDSK_ARBEIDSFORHOLD));
        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        lagreOpptjeningsPeriode(tilOgMed, repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId()));
        return behandling;
    }

    private Behandling opprettOppgittOpptjening(AktørId aktørId, boolean medNæring) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør().medBruker(aktørId, NavBrukerKjønn.KVINNE);

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();
        UtenlandskVirksomhetEntitet svenska_stat = new UtenlandskVirksomhetEntitet(Landkoder.SWE, "Svenska Stat");
        egenNæringBuilder
            .medPeriode(periode)
            .medUtenlandskVirksomhet(svenska_stat)
            .medBegrunnelse("Vet ikke")
            .medBruttoInntekt(BigDecimal.valueOf(100000))
            .medRegnskapsførerNavn("Jacob")
            .medRegnskapsførerTlf("+46678456345")
            .medVirksomhetType(VirksomhetType.FISKE);
        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        oppgittOpptjeningBuilder
            .leggTilEgneNæringer(Collections.singletonList(egenNæringBuilder));
        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);

        if (medNæring) {
            InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
            builder.medAktørId(aktørId)
                .medInntektsKilde(InntektsKilde.SIGRUN)
                .medInntektspostType(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medInntektspostFom(LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1))
                .medInntektspostTom(LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31))
                .medInntektspostBeløp(BigDecimal.TEN)
                .build();
        }

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        lagreOpptjeningsPeriode(tilOgMed, repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId()));
        return behandling;
    }

    private Behandling opprettBehandling(ArbeidType annenOpptjeningType) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        LocalDate fraOgMed = LocalDate.now().minusMonths(1);
        LocalDate tilOgMed = LocalDate.now().plusMonths(1);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);

        OppgittOpptjeningBuilder oppgittOpptjeningBuilder = OppgittOpptjeningBuilder.ny();
        oppgittOpptjeningBuilder
            .leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode, annenOpptjeningType));

        scenario.medOppgittOpptjening(oppgittOpptjeningBuilder);

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        lagreOpptjeningsPeriode(tilOgMed, repositoryProvider.getBehandlingRepository().hentResultat(behandling.getId()));
        return behandling;
    }

    private void lagreOpptjeningsPeriode(LocalDate opptjeningTom, Behandlingsresultat behandlingsresultat) {
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, opptjeningTom.minusMonths(10), opptjeningTom);
    }
}
