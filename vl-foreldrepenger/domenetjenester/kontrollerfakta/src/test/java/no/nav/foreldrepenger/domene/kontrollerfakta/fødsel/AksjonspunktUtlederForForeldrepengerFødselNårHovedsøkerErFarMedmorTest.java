package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.DUMMY_CONSUMER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT;
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
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;


public class AksjonspunktUtlederForForeldrepengerFødselNårHovedsøkerErFarMedmorTest {

    private static final LocalDate TERMINDAT0_NÅ = LocalDate.now();
    private static final LocalDate FØDSEL_17_SIDEN = LocalDate.now().minusDays(27);

    public static AktørId GITT_AKTØR_ID = new AktørId("666");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktUtlederForForeldrepengerFødsel apUtleder;

    @Before
    public void oppsett() {
        apUtleder = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(repositoryProvider, mock(SkjæringstidspunktTjeneste.class)));
    }

    @Test
    public void ingen_akjsonspunkter_dersom_fødsel_registrert_i_TPS_og_antall_barn_stemmer_med_søknad() {
        // Oppsett
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);
        AksjonspunktUtlederForForeldrepengerFødsel testObjekt = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(behandlingRepositoryMock, mock(SkjæringstidspunktTjeneste.class)));

        Behandling behandling = opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate.now(), 1, 1);
        List<AksjonspunktResultat> utledeteAksjonspunkter = testObjekt.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).isEmpty();
        verify(testObjekt).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_registrert_i_TPS_og_antall_barn_ikke_stemmer_med_søknad() {
        // Oppsett
        BehandlingRepositoryProvider behandlingRepositoryMock = spy(repositoryProvider);
        AksjonspunktUtlederForForeldrepengerFødsel testObjekt = Mockito.spy(new AksjonspunktUtlederForForeldrepengerFødsel(behandlingRepositoryMock, mock(SkjæringstidspunktTjeneste.class)));

        Behandling behandling = opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate.now(), 2, 1);
        List<AksjonspunktResultat> utledeteAksjonspunkter = testObjekt.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(testObjekt).samsvarerAntallBarnISøknadMedAntallBarnITps(any(FamilieHendelse.class), any());
    }

    @Test
    public void sjekk_aksjonspunkter_når_søker_oppgir_termindato() {
        Behandling behandling = opprettBehandlingMedOppgittTerminOgBehandlingType(TERMINDAT0_NÅ);
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter.size()).isEqualTo(2);
        assertThat(utledeteAksjonspunkter.get(0).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_TERMINBEKREFTELSE);
        assertThat(utledeteAksjonspunkter.get(1).getAksjonspunktDefinisjon()).isEqualTo(VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT);
    }

    @Test
    public void sjekk_manglende_fødsel_dersom_fødsel_og_mer_enn_14_dager_siden_fødsel() {
        Behandling behandling = opprettBehandlingMedOppgittFødsel(FØDSEL_17_SIDEN);
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL));
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    @Test
    public void sjekk_autopunkt_vent_på_fødsel_dersom_fødsel_og_mindre_enn_14_dager_siden_fødsel() {
        Behandling behandling = opprettBehandlingMedOppgittFødsel(LocalDate.now());
        List<AksjonspunktResultat> utledeteAksjonspunkter = apUtleder.utledAksjonspunkterFor(behandling);

        assertThat(utledeteAksjonspunkter).containsExactly(AksjonspunktResultat.opprettForAksjonspunkt(AUTO_VENT_PÅ_FØDSELREGISTRERING));
        assertThat(utledeteAksjonspunkter.get(0).getAksjonspunktModifiserer()).isNotEqualTo(DUMMY_CONSUMER);
        verify(apUtleder).erDagensDato14DagerEtterOppgittFødselsdato(any(FamilieHendelse.class));
    }

    private Behandling opprettBehandlingFarSøkerFødselRegistrertITps(LocalDate fødseldato, int antallBarnSøknad, int antallBarnTps) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger
            .forFødsel();
        scenario.medSøknadHendelse()
            .medFødselsDato(fødseldato)
            .medAntallBarn(antallBarnSøknad);
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
        scenario.medBekreftetHendelse().medFødselsDato(fødseldato).medAntallBarn(antallBarnTps);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittTerminOgBehandlingType(LocalDate termindato) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger
            .forFødselMedGittAktørId(GITT_AKTØR_ID);
        scenario.medSøknadHendelse().medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
            .medUtstedtDato(LocalDate.now())
            .medTermindato(termindato)
            .medNavnPå("LEGEN MIN"));
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
        return scenario.lagre(repositoryProvider);
    }

    private Behandling opprettBehandlingMedOppgittFødsel(LocalDate fødseldato) {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødseldato);
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
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
