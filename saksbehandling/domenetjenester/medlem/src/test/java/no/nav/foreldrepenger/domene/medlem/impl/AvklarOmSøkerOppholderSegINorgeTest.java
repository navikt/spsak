package no.nav.foreldrepenger.domene.medlem.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personas;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvklarOmSøkerOppholderSegINorgeTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider provider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private PersonopplysningTjeneste personopplysningTjeneste;

    private AvklarOmSøkerOppholderSegINorge tjeneste;

    @Before
    public void setUp() {
        this.tjeneste = new AvklarOmSøkerOppholderSegINorge(provider, personopplysningTjeneste);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_soker_har_fodt() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(fødselsdato, fødselsdato.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        leggTilSøker(scenario, AdresseType.POSTADRESSE_UTLAND, Landkoder.FIN);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, fødselsdato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_soker_er_nordisk() {
        // Arrange
        LocalDate termindato = LocalDate.now();
        AktørId aktørId1 = new AktørId("1");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        scenario.medSøknad()
            .medMottattDato(LocalDate.now());
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(termindato, termindato.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        leggTilSøker(scenario, AdresseType.POSTADRESSE_UTLAND, Landkoder.FIN);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, termindato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_soker_har_annet_statsborgerskap() {
        // Arrange
        LocalDate termindato = LocalDate.now();
        AktørId aktørId1 = new AktørId("1");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        scenario.medSøknad()
            .medMottattDato(LocalDate.now());
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(termindato, termindato.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        leggTilSøker(scenario, AdresseType.POSTADRESSE_UTLAND, Landkoder.CAN);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, termindato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_soker_har_hatt_inntekt_i_Norge_de_siste_tre_mnd() {
        // Arrange
        AktørId aktørId1 = new AktørId("1");
        LocalDate fom = LocalDate.now().minusWeeks(3L);
        LocalDate tom = LocalDate.now().minusWeeks(1L);
        LocalDate termindato = LocalDate.now().plusDays(40);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(termindato, termindato.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        scenario.medSøknad().medMottattDato(fom);
        leggTilSøker(scenario, AdresseType.POSTADRESSE_UTLAND, Landkoder.ESP);

        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medAktørId(aktørId1);
        builder.medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);
        builder.medInntektspostBeløp(BigDecimal.TEN);
        builder.medInntektspostFom(fom);
        builder.medInntektspostTom(tom);
        builder.build();
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);
        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, termindato);

        //Assert
        assertThat(medlemResultat).isEmpty();
    }

    @Test
    public void skal_oprette_aksjonspunkt_ved_uavklart_oppholdsrett() {
        // Arrange
        LocalDate termindato = LocalDate.now().minusDays(15L);
        AktørId aktørId1 = new AktørId("1");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør().medBruker(aktørId1, NavBrukerKjønn.KVINNE);
        scenario.medSøknad()
            .medMottattDato(LocalDate.now());
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(termindato, termindato.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        leggTilSøker(scenario, AdresseType.POSTADRESSE_UTLAND, Landkoder.ESP);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);
        // Act
        Optional<MedlemResultat> medlemResultat = tjeneste.utled(behandling, termindato);

        //Assert
        assertThat(medlemResultat).contains(MedlemResultat.AVKLAR_OPPHOLDSRETT);
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, AdresseType adresseType, Landkoder adresseLand) {
        PersonInformasjon.Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        Personas persona = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.UOPPGITT, Region.UDEFINERT)
            .personstatus(PersonstatusType.UDEFINERT)
            .statsborgerskap(adresseLand);

        PersonAdresse.Builder adresseBuilder = PersonAdresse.builder().adresselinje1("Portveien 2").land(adresseLand);
        persona.adresse(adresseType, adresseBuilder);
        PersonInformasjon søker = persona.build();
        scenario.medRegisterOpplysninger(søker);
    }

}
