package no.nav.foreldrepenger.domene.mottak.søknad;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.InntektArbeidYtelseScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTidligereMottattForeldrepenger;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class AksjonspunktUtlederForTidligereMottattForeldrepengerTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Test
    public void skal_ikke_opprette_aksjonspunkt_om_søker_ikke_har_mottatt_foreldrepenger_før() {
        // Arrange
        AktørId aktørId = new AktørId("123");
        AktørId annenAktørId = new AktørId("456");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling behandling = byggBehandling(scenario, aktørId, annenAktørId, NavBrukerKjønn.KVINNE);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = lagUtleder().utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater.isEmpty()).isTrue();
    }

    @Test
    public void skal_opprette_aksjonspunkt_om_soker_har_mottatt_foreldrepenger_før() {
        // Arrange
        AktørId aktørId = new AktørId("123");
        AktørId annenAktørId = new AktørId("456");

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        leggTilYtelseForAktør(scenario, RelatertYtelseType.FORELDREPENGER, aktørId);
        Behandling behandling = byggBehandling(scenario, aktørId, annenAktørId, NavBrukerKjønn.KVINNE);

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = lagUtleder().utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(1);
        assertThat(aksjonspunktResultater.get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE);
    }

    @Test
    public void skal_ikke_opprette_aksjonspunkt_hvis_behandling_har_type_REVURDERING() {
        // Arrange
        AktørId aktørId = new AktørId("123");
        AktørId annenAktørId = new AktørId("456");
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        Behandling behandling = byggBehandling(scenario, aktørId, annenAktørId, NavBrukerKjønn.KVINNE);

        Behandling.Builder builder = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING);
        Behandling revurdering = builder.build();

        // Act
        List<AksjonspunktResultat> aksjonspunktResultater = lagUtleder().utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunktResultater).hasSize(0);
    }

    private AksjonspunktUtlederForTidligereMottattForeldrepenger lagUtleder() {
        return new AksjonspunktUtlederForTidligereMottattForeldrepenger(repositoryProvider);
    }

    private Behandling byggBehandling(AbstractTestScenario<?> scenario, AktørId aktørId, AktørId annenAktørId, NavBrukerKjønn kjønn) {
        scenario.medBekreftetHendelse().medFødselsDato(LocalDate.now());
        scenario.medBruker(aktørId, kjønn)
            .medSøknad().medMottattDato(LocalDate.now());
        leggTilSøker(scenario, kjønn);
        scenario.medSøknadAnnenPart().medAktørId(annenAktørId);
        return scenario.lagre(repositoryProvider);
    }

    private void leggTilYtelseForAktør(AbstractTestScenario<?> scenario, RelatertYtelseType relatertYtelseType, AktørId aktørId) {
        InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder builder = scenario.getInntektArbeidYtelseScenarioTestBuilder();
        builder.medAktørId(aktørId);
        builder.medYtelseType(relatertYtelseType);
        builder.build();
    }
    
    private void leggTilSøker(AbstractTestScenario<?> scenario, NavBrukerKjønn kjønn) {
        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();
        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.UOPPGITT, kjønn, Region.UDEFINERT)
            .build();
        scenario.medRegisterOpplysninger(søker);
    }
}
