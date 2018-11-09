package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Rule;
import org.junit.Test;

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
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
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

public class FødselsvilkårFarTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)));

    @Test // FP_VK 11.2 Vilkårsutfall oppfylt
    public void skal_vurdere_vilkår_som_oppfylt_når_søker_er_far_og_fødsel_bekreftet() throws IOException {
        // Arrange
        Behandling behandling = lagBehandlingMedFarEllerMedmor(RelasjonsRolleType.FARA, NavBrukerKjønn.MANN, true, false, true);

        // Act
        VilkårData data = new InngangsvilkårFødselFar(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String soekersKjonn = jsonNode.get("soekersKjonn").asText();

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(soekersKjonn).isEqualTo("MANN");
    }

    @Test // FP_VK 11.2 Vilkårsutfall oppfylt
    public void skal_vurdere_vilkår_som_oppfylt_når_søker_er_medmor_og_fødsel_bekreftet() throws IOException {
        // Arrange
        Behandling behandling = lagBehandlingMedFarEllerMedmor(RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE, true, false, true);

        // Act
        VilkårData data = new InngangsvilkårFødselFar(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String soekersKjonn = jsonNode.get("soekersKjonn").asText();

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(soekersKjonn).isEqualTo("KVINNE");
    }

    @Test  // FP_VK 11.4 Vilkårsutfall ikke oppfylt
    public void skal_vurdere_vilkår_som_ikke_oppfylt_når_søker_er_medmor_og_fødsel_ikke_bekreftet_og_søkt_om_termin_og_mor_frisk() {
        // Arrange
        Behandling behandling = lagBehandlingMedFarEllerMedmor(RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE, false, false, false);

        // Act
        VilkårData data = new InngangsvilkårFødselFar(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(data.getVilkårUtfallMerknad()).isEqualTo(VilkårUtfallMerknad.VM_1028);
    }

    @Test // FP_VK 11.4 Vilkårsutfall oppfylt
    public void skal_vurdere_vilkår_som_oppfylt_når_søker_er_far_og_fødsel_ikke_bekreftet_og_søkt_om_termin_og_mor_syk() {
        // Arrange
        Behandling behandling = lagBehandlingMedFarEllerMedmor(RelasjonsRolleType.FARA, NavBrukerKjønn.MANN, false, true, false);

        // Act
        VilkårData data = new InngangsvilkårFødselFar(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    private Behandling lagBehandlingMedFarEllerMedmor(RelasjonsRolleType rolle, NavBrukerKjønn kjønn, boolean fødselErBekreftet,
                                                      boolean morErSykVedFødsel, boolean erFødsel) {
        // Setup basis scenario
        LocalDate fødselsdato = LocalDate.now();
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødsel();
        if (erFødsel) {
            scenario.medSøknadHendelse()
                .medFødselsDato(fødselsdato)
                .medAntallBarn(1)
                .medErMorForSykVedFødsel(morErSykVedFødsel);
        } else {
            scenario.medSøknadHendelse()
                .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                    .medTermindato(LocalDate.now())
                    .medUtstedtDato(LocalDate.now())
                    .medNavnPå("LEGEN min"))
                .medAntallBarn(1)
                .medErMorForSykVedFødsel(morErSykVedFødsel);
        }
        scenario.medBrukerKjønn(NavBrukerKjønn.MANN);

        // Legg til om fødsel er bekreftet eller om mor er syk ved fødsel
        if (fødselErBekreftet) {
            scenario.medBekreftetHendelse().medFødselsDato(fødselsdato).medAntallBarn(1);
        }
        if (morErSykVedFødsel) {
            scenario.medBekreftetHendelse().medErMorForSykVedFødsel(true);
        }

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();
        AktørId barnAktørId = new AktørId("123");
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(barnAktørId, fødselsdato)
            .relasjonTil(søkerAktørId, rolle, null)
            .build();

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .voksenPerson(søkerAktørId, SivilstandType.GIFT, kjønn, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(barnAktørId, RelasjonsRolleType.BARN, null)
            .build();

        scenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(fødselsdato, null));
        scenario.medRegisterOpplysninger(søker);
        scenario.medRegisterOpplysninger(fødtBarn);

        return scenario.lagre(repositoryProvider);
    }
}
