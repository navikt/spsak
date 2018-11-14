package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;

public class FødselsvilkårMorTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)));

    @Test
    public void skal_vurdere_vilkår_som_ikke_oppfylt_når_søker_ikke_er_kvinne() throws JsonProcessingException, IOException {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        
        leggTilSøker(scenario, NavBrukerKjønn.MANN);
        
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(LocalDate.now(), null));

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String soekersKjonn = jsonNode.get("soekersKjonn").asText();

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1003);
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(soekersKjonn).isEqualTo("MANN");
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

    @Test
    public void skal_vurdere_vilkår_som_oppfylt_når_søker_er_mor_og_fødsel_bekreftet() {
        // Arrange
        Behandling behandling = lagBehandlingMedMorEllerMedmor(RelasjonsRolleType.MORA);

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_oppfylt_når_søker_ikke_er_mor_og_fødsel_bekreftet() {
        // Arrange
        Behandling behandling = lagBehandlingMedMorEllerMedmor(RelasjonsRolleType.FARA);

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1002);
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_oppfylt_når_fødsel_ikke_bekreftet_termindato_ikke_passert_26_uker() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(24))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()));
        scenario.medSøknadDato(LocalDate.now().minusMonths(5))
            .medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(24))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()));
        
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(LocalDate.now().minusDays(24), null));

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1001);
    }

    @Test
    public void skal_vurdere_vilkår_som_oppfylt_når_fødsel_ikke_bekreftet_termindato_passert_26_uker() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(24))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()));
        scenario.medSøknadDato(LocalDate.now().minusMonths(4))
            .medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(24))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()));
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(LocalDate.now().minusDays(24), null));

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_oppfylt_dersom_fødsel_burde_vært_inntruffet() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(26))
                .medUtstedtDato(LocalDate.now())
                .medNavnPå("LEGE LEGESEN"));
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().minusDays(26))
                .medUtstedtDato(LocalDate.now())
                .medNavnPå("LEGE LEGESEN"));
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(LocalDate.now().minusDays(26), null));

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1026);
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_oppfylt_dersom_fødsel_med_0_barn() {
        // Arrange
        LocalDate fødselsdato = LocalDate.now();
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato).medAntallBarn(0);
        scenario.medBekreftetHendelse().medFødselsDato(fødselsdato).medAntallBarn(0);
        scenario.medBrukerKjønn(NavBrukerKjønn.KVINNE);
        leggTilSøker(scenario, NavBrukerKjønn.KVINNE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(fødselsdato, null));

        // Act
        VilkårData data = new InngangsvilkårFødselMor(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_MOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1026);
    }

    private Behandling lagBehandlingMedMorEllerMedmor(RelasjonsRolleType rolle) {
        // Setup basis scenario
        LocalDate fødselsdato = LocalDate.now();
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(fødselsdato).medAntallBarn(1);
        scenario.medBekreftetHendelse().medFødselsDato(fødselsdato).medAntallBarn(1);
        scenario.medBrukerKjønn(NavBrukerKjønn.KVINNE);

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId barnAktørId = new AktørId("123");
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(barnAktørId, fødselsdato)
            .relasjonTil(søkerAktørId, rolle, true)
            .build();

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(barnAktørId, RelasjonsRolleType.BARN, true)
            .build();
        scenario.medRegisterOpplysninger(søker);
        scenario.medRegisterOpplysninger(fødtBarn);

        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(fødselsdato, null));
        return behandling;
    }

}
