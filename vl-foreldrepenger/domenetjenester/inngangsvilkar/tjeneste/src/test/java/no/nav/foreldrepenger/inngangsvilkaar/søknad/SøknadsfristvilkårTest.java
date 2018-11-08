package no.nav.foreldrepenger.inngangsvilkaar.søknad;

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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.medlem.impl.MedlemskapPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.inngangsvilkaar.impl.InngangsvilkårOversetter;

public class SøknadsfristvilkårTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private BasisPersonopplysningTjeneste personopplysningTjeneste = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
    private InngangsvilkårOversetter oversetter = new InngangsvilkårOversetter(repositoryProvider,
        new MedlemskapPerioderTjenesteImpl(12, 6, skjæringstidspunktTjeneste), skjæringstidspunktTjeneste, personopplysningTjeneste,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)));

    @Test
    public void skal_vurdere_vilkår_som_oppfylt_når_elektronisk_søknad_og_søknad_mottat_innen_6_mnd_fra_skjæringstidspunkt() throws JsonProcessingException, IOException {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknad().medElektroniskRegistrert(true);
        scenario.medSøknad().medMottattDato(LocalDate.now().plusMonths(6));
        scenario.medBekreftetHendelse()
            .medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now()));
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Act
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);

        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(data.getRegelInput());
        String elektroniskSoeknad = jsonNode.get("elektroniskSoeknad").asText();

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getMerknadParametere()).isEmpty();
        assertThat(data.getRegelInput()).isNotEmpty();
        assertThat(elektroniskSoeknad).isEqualTo("true");
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_vurdert_når_elektronisk_søknad_og_søknad_ikke_mottat_innen_6_mnd_fra_skjæringstidspunkt() {
        final int ANTALL_DAGER_SOKNAD_LEVERT_FOR_SENT = 100;

        // Arrange
        Behandling behandling = mockBehandling(true, LocalDate.now().plusMonths(6).plusDays(ANTALL_DAGER_SOKNAD_LEVERT_FOR_SENT),
            LocalDate.now());

        // Act
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);

        assertThat(data.getApDefinisjoner()).contains(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
        assertThat(data.getMerknadParametere())
            .containsOnlyKeys("antallDagerSoeknadLevertForSent")
            .containsEntry("antallDagerSoeknadLevertForSent", String.valueOf(ANTALL_DAGER_SOKNAD_LEVERT_FOR_SENT));

    }

    @Test
    public void skal_vurdere_vilkår_som_oppfylt_når_papirsøknad_og_søknad_mottat_innen_6_mnd_og_2_dager_fra_skjæringstidspunkt() {
        // Arrange
        Behandling behandling = mockBehandling(false, LocalDate.now().minusMonths(6), LocalDate.now());

        // Act
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(data.getMerknadParametere()).isEmpty();
    }

    @Test
    public void skal_vurdere_vilkår_som_ikke_vurdert_når_papirsøknad_og_søknad_ikke_mottat_innen_6_mnd_og_2_dager_fra_skjæringstidspunkt() {
        // Arrange
        Behandling behandling = mockBehandling(false, LocalDate.now(), LocalDate.now().minusMonths(6).minusDays(6));

        // Act
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);

        // Assert
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);

        assertThat(data.getApDefinisjoner()).contains(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
        assertThat(data.getMerknadParametere())
            .containsOnlyKeys("antallDagerSoeknadLevertForSent");
    }

    private Behandling mockBehandling(boolean elektronisk, LocalDate mottakDato, LocalDate omsorgsovertakelsesDato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknad().medElektroniskRegistrert(elektronisk);
        scenario.medSøknad().medMottattDato(mottakDato);
        scenario.medBekreftetHendelse()
            .medAdopsjon(scenario.medBekreftetHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelsesDato));
        return scenario.lagre(repositoryProvider);
    }

    @Test
    public void skal_vurdere_vilkår_for_papirsøknad_med_original_frist_lørdag_pluss_2_virkedager() {

        LocalDate mottattMandag = LocalDate.of(2017, 9, 4);
        LocalDate mottattTirsdag = mottattMandag.plusDays(1);
        LocalDate mottattOnsdag = mottattMandag.plusDays(2);
        LocalDate mottattTorsdag = mottattMandag.plusDays(3);
        LocalDate mottattFredag = mottattMandag.plusDays(4);
        LocalDate mottattLørdag = mottattMandag.plusDays(5);
        LocalDate mottattSøndag = mottattMandag.plusDays(6);

        LocalDate skjæringstidspunktMedOrginalFristLørdag = mottattMandag.minusDays(2).minusMonths(6);

        // Act + assert
        assertOppfylt(mockPapirSøknad(mottattMandag, skjæringstidspunktMedOrginalFristLørdag));
        assertOppfylt(mockPapirSøknad(mottattTirsdag, skjæringstidspunktMedOrginalFristLørdag));
        assertIkkeVurdertForSent(mockPapirSøknad(mottattOnsdag, skjæringstidspunktMedOrginalFristLørdag), 1);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattTorsdag, skjæringstidspunktMedOrginalFristLørdag), 2);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattFredag, skjæringstidspunktMedOrginalFristLørdag), 3);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattLørdag, skjæringstidspunktMedOrginalFristLørdag), 4);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattSøndag, skjæringstidspunktMedOrginalFristLørdag), 5);

    }

    @Test
    public void skal_vurdere_vilkår_for_papirsøknad_med_original_frist_søndag_pluss_2_virkedager() {

        LocalDate mottattMandag = LocalDate.of(2017, 9, 4);
        LocalDate mottattTirsdag = mottattMandag.plusDays(1);
        LocalDate mottattOnsdag = mottattMandag.plusDays(2);
        LocalDate mottattTorsdag = mottattMandag.plusDays(3);
        LocalDate mottattFredag = mottattMandag.plusDays(4);
        LocalDate mottattLørdag = mottattMandag.plusDays(5);
        LocalDate mottattSøndag = mottattMandag.plusDays(6);

        LocalDate skjæringstidspunktMedOrginalFristSøndag = mottattMandag.minusDays(1).minusMonths(6);

        // Act + assert
        assertOppfylt(mockPapirSøknad(mottattMandag, skjæringstidspunktMedOrginalFristSøndag));
        assertOppfylt(mockPapirSøknad(mottattTirsdag, skjæringstidspunktMedOrginalFristSøndag));
        assertIkkeVurdertForSent(mockPapirSøknad(mottattOnsdag, skjæringstidspunktMedOrginalFristSøndag), 1);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattTorsdag, skjæringstidspunktMedOrginalFristSøndag), 2);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattFredag, skjæringstidspunktMedOrginalFristSøndag), 3);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattLørdag, skjæringstidspunktMedOrginalFristSøndag), 4);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattSøndag, skjæringstidspunktMedOrginalFristSøndag), 5);

    }

    @Test
    public void skal_vurdere_vilkår_for_papirsøknad_med_original_frist_fredag_pluss_2_virkedager() {

        LocalDate mottattMandag = LocalDate.of(2017, 9, 4);
        LocalDate mottattTirsdag = mottattMandag.plusDays(1);
        LocalDate mottattOnsdag = mottattMandag.plusDays(2);
        LocalDate mottattTorsdag = mottattMandag.plusDays(3);
        LocalDate mottattFredag = mottattMandag.plusDays(4);
        LocalDate mottattLørdag = mottattMandag.plusDays(5);
        LocalDate mottattSøndag = mottattMandag.plusDays(6);

        LocalDate skjæringstidspunktMedOrginalFristFredag = mottattMandag.minusDays(3).minusMonths(6);

        // Act + assert
        assertOppfylt(mockPapirSøknad(mottattMandag, skjæringstidspunktMedOrginalFristFredag));
        assertOppfylt(mockPapirSøknad(mottattTirsdag, skjæringstidspunktMedOrginalFristFredag));
        assertIkkeVurdertForSent(mockPapirSøknad(mottattOnsdag, skjæringstidspunktMedOrginalFristFredag), 1);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattTorsdag, skjæringstidspunktMedOrginalFristFredag), 2);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattFredag, skjæringstidspunktMedOrginalFristFredag), 3);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattLørdag, skjæringstidspunktMedOrginalFristFredag), 4);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattSøndag, skjæringstidspunktMedOrginalFristFredag), 5);

    }

    @Test
    public void skal_vurdere_vilkår_for_papirsøknad_med_original_frist_torsdag_pluss_2_virkedager_og_her_treffer_månedsslutt() {

        LocalDate mottattMandag = LocalDate.of(2017, 9, 11);
        LocalDate mottattTirsdag = mottattMandag.plusDays(1);
        LocalDate mottattOnsdag = mottattMandag.plusDays(2);
        LocalDate mottattTorsdag = mottattMandag.plusDays(3);
        LocalDate mottattFredag = mottattMandag.plusDays(4);
        LocalDate mottattLørdag = mottattMandag.plusDays(5);
        LocalDate mottattSøndag = mottattMandag.plusDays(6);

        LocalDate skjæringstidspunktMedOrginalFristTorsdag = mottattMandag.minusDays(4).minusMonths(6);

        // Act + assert
        assertOppfylt(mockPapirSøknad(mottattMandag, skjæringstidspunktMedOrginalFristTorsdag));
        assertIkkeVurdertForSent(mockPapirSøknad(mottattTirsdag, skjæringstidspunktMedOrginalFristTorsdag), 1);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattOnsdag, skjæringstidspunktMedOrginalFristTorsdag), 2);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattTorsdag, skjæringstidspunktMedOrginalFristTorsdag), 3);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattFredag, skjæringstidspunktMedOrginalFristTorsdag), 4);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattLørdag, skjæringstidspunktMedOrginalFristTorsdag), 5);
        assertIkkeVurdertForSent(mockPapirSøknad(mottattSøndag, skjæringstidspunktMedOrginalFristTorsdag), 6);

    }

    private Behandling mockPapirSøknad(LocalDate mottattDag, LocalDate omsorgDato) {
        Behandling behandling = mockBehandling(false, mottattDag, omsorgDato);
        return behandling;
    }

    private void assertOppfylt(Behandling behandling) {
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.OPPFYLT);

        assertThat(data.getApDefinisjoner()).isEmpty();
        ;
        assertThat(data.getMerknadParametere()).isEmpty();
    }

    private void assertIkkeVurdertForSent(Behandling behandling, int dagerForSent) {
        VilkårData data = new InngangsvilkårEngangsstønadSøknadsfrist(oversetter).vurderVilkår(behandling);
        assertThat(data.getVilkårType()).isEqualTo(VilkårType.SØKNADSFRISTVILKÅRET);
        assertThat(data.getUtfallType()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);

        assertThat(data.getApDefinisjoner()).contains(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);
        assertThat(data.getMerknadParametere())
            .containsOnlyKeys("antallDagerSoeknadLevertForSent")
            .containsEntry("antallDagerSoeknadLevertForSent", String.valueOf(dagerForSent));
    }

}
