package no.nav.foreldrepenger.domene.medlem.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvklaringFaktaMedlemskapTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider provider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;

    @Inject
    private PersonopplysningTjeneste personopplysningTjeneste;

    private AvklaringFaktaMedlemskap tjeneste;

    private static final LocalDate SKJÆRINGSDATO_FØDSEL = LocalDate.now().plusDays(1);

    @Before
    public void setUp() {
        this.tjeneste = new AvklaringFaktaMedlemskap(provider, medlemskapPerioderTjeneste, personopplysningTjeneste);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_ved_gyldig_medlems_periode() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);

        RegistrertMedlemskapPerioder gyldigPeriode = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_7_a) // hjemlet i bokstav a
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();
        scenario.leggTilMedlemskapPeriode(gyldigPeriode);

        leggTilSøker(scenario);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_ved_dekningsgrad_lik_ikke_medlem() {
        // Arrange
        RegistrertMedlemskapPerioder gyldigPeriode = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.FTL_2_9_1_b)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilMedlemskapPeriode(gyldigPeriode);
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_få_aksjonspunkt_når_dekningsrad_er_av_type_uavklart() {
        // Arrange
        RegistrertMedlemskapPerioder gyldigPeriode = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.OPPHOR)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilMedlemskapPeriode(gyldigPeriode);
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);

        leggTilSøker(scenario);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).contains(MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_dersom_dekningsgrad_unntatt_og_person_bosatt_og_statsborgerskap_ulik_usa() {
        // Arrange
        RegistrertMedlemskapPerioder medlemskapPeriodeForUnntak = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.UNNTATT) // unntak FT §2-13
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilMedlemskapPeriode(medlemskapPeriodeForUnntak);
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_dersom_dekningsgrad_unntatt_og_person_utvandret() {
        // Arrange
        RegistrertMedlemskapPerioder medlemskapPeriodeForUnntak = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.UNNTATT) // unntak FT §2-13
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilMedlemskapPeriode(medlemskapPeriodeForUnntak);
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.USA, Region.UDEFINERT, PersonstatusType.UTVA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_dersom_dekningsgrad_unntatt_og_person_bosatt_og_statsborgerskap_lik_usa() {
        // Arrange
        RegistrertMedlemskapPerioder medlemskapPeriodeForUnntak = new MedlemskapPerioderBuilder()
            .medDekningType(MedlemskapDekningType.UNNTATT)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medErMedlem(true)
            .medPeriode(SKJÆRINGSDATO_FØDSEL, SKJÆRINGSDATO_FØDSEL)
            .build();

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.leggTilMedlemskapPeriode(medlemskapPeriodeForUnntak);
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.USA, Region.UDEFINERT, PersonstatusType.BOSA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).contains(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_ved_ikke_gyldig_periode_og_status_utvandret() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.NOR, Region.NORDEN, PersonstatusType.UTVA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_ved_ikke_gyldig_periode_og_ikke_utvandret_og_region_nordisk() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.SWE, Region.UDEFINERT, PersonstatusType.BOSA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_ved_ikke_gyldig_periode_og_ikke_utvandret_og_region_eøs_og_inntekt_siste_3mnd() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        final SøknadRepository søknadRepository = scenario.mockBehandlingRepositoryProvider().getElement1().getSøknadRepository();
        leggTilSøker(scenario, Landkoder.BEL, Region.UDEFINERT, PersonstatusType.BOSA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);
        søknadRepository.hentSøknad(behandling);

        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medInntektsKilde(InntektsKilde.INNTEKT_OPPTJENING);
        builder.buildInntektGrunnlag();

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).isEmpty();
    }

    @Test
    public void skal_opprette_aksjonspunkt_ved_ikke_gyldig_periode_og_ikke_utvandret_og_region_eøs_og_ikke_inntekt_siste_3mnd() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.BEL, Region.UDEFINERT, PersonstatusType.BOSA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).contains(MedlemResultat.AVKLAR_OPPHOLDSRETT);
    }

    @Test
    public void skal_opprette_aksjonspunkt_ved_ikke_gyldig_periode_og_ikke_utvandret_og_region_annen() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.medSøknad().medMottattDato(SKJÆRINGSDATO_FØDSEL);
        leggTilSøker(scenario, Landkoder.UDEFINERT, Region.UDEFINERT, PersonstatusType.BOSA);
        Behandling behandling = scenario.lagre(provider, resultatRepositoryProvider);

        // Act
        Optional<MedlemResultat> resultat = tjeneste.utled(behandling, SKJÆRINGSDATO_FØDSEL);

        // Assert
        assertThat(resultat).contains(MedlemResultat.AVKLAR_LOVLIG_OPPHOLD);
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario) {
        leggTilSøker(scenario, Landkoder.NOR, Region.NORDEN, PersonstatusType.BOSA);
    }

    private void leggTilSøker(AbstractTestScenario<?> scenario, Landkoder statsborgerskap, Region region, PersonstatusType personstatus) {
        PersonInformasjon.Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.UOPPGITT, region)
            .personstatus(personstatus)
            .statsborgerskap(statsborgerskap)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }

}
