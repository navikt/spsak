package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.DUMMY_CONSUMER;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.FØRSTEGANGSSØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class AksjonspunktUtlederForEngangsstønadFødselTest {

    private static final LocalDate FØDSELSDATO_NÅ = LocalDate.now();
    private static final LocalDate TERMINDATO_NÅ = LocalDate.now();
    private static final LocalDate FØDSELSDATO_16_SIDEN = LocalDate.now().minusDays(16);
    private static final LocalDate TERMINDATO_27_SIDEN = LocalDate.now().minusDays(27);

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private AksjonspunktUtlederForEngangsstønadFødsel apUtleder;

    @Before
    public void oppsett() {
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);
        apUtleder = Mockito.spy(new AksjonspunktUtlederForEngangsstønadFødsel(behandlingRepositoryMock, mock(SkjæringstidspunktTjeneste.class)));
    }

    @Test
    public void avklar_terminbekreftelse_dersom_termindato_nå() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittTermin(TERMINDATO_NÅ, FØRSTEGANGSSØKNAD);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AVKLAR_TERMINBEKREFTELSE));
        verify(apUtleder).gjelderSøknadenForeldrepenger();
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_termindato_mer_enn_25_dager_siden() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittTermin(TERMINDATO_27_SIDEN, FØRSTEGANGSSØKNAD);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(apUtleder).erDagensDato25DagerEtterOppgittTerminsdato(any(FamilieHendelseGrunnlag.class));
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_og_mindre_enn_14_dager_siden_fødsel() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittFødsel(FØDSELSDATO_NÅ);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AUTO_VENT_PÅ_FØDSELREGISTRERING));
        assertThat(utledeteAksjonspunkter.get(0).getAksjonspunktModifiserer()).isNotEqualTo(DUMMY_CONSUMER);
        //Usikker her
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    @Test
    public void autopunkt_vent_på_fødsel_dersom_fødsel_og_mer_enn_14_dager_siden_fødsel() {
        //Arrange
        Behandling behandling = opprettBehandlingMedOppgittFødsel(FØDSELSDATO_16_SIDEN);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        //Usikker her
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    @Test
    public void ingen_akjsonspunkter_dersom_fødsel_registrert_i_TPS_og_antall_barn_stemmer_med_søknad() {
        //Arrange
        Behandling behandling = opprettBehandlingForFødselRegistrertITps(FØDSELSDATO_NÅ,1, 1);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).isEmpty();
        verify(apUtleder).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_registrert_i_TPS_og_antall_barn_ikke_stemmer_med_søknad() {
        //Arrange
        Behandling behandling = opprettBehandlingForFødselRegistrertITps(FØDSELSDATO_NÅ, 2, 1);
        //Act
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);
        //Assert
        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(apUtleder).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    private Behandling opprettBehandlingMedOppgittTermin(LocalDate termindato, BehandlingType behandlingType) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medBehandlingType(behandlingType);
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);

        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));
        return scenario.lagre(repositoryProvider);
    }


    private Behandling opprettBehandlingMedOppgittFødsel(LocalDate fødseldato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødseldato);

        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingForFødselRegistrertITps(LocalDate fødseldato, int antallBarnSøknad, int antallBarnTps) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel();
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        scenario.medSøknadHendelse()
            .medFødselsDato(fødseldato)
            .medAntallBarn(antallBarnSøknad);
        scenario.medBekreftetHendelse().medFødselsDato(fødseldato).medAntallBarn(antallBarnTps);
        return scenario.lagre(repositoryProvider);
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
