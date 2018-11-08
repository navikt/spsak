package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OMSORGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;

public class EngangsstønadVilkårUtlederTest {

    @Test
    public void skal_opprette_vilkår_for_mor_som_søker_stønad_fødsel() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        Behandling behandling = scenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        // Act
        final Optional<FamilieHendelseType> familieHendelseType = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon).map(FamilieHendelse::getType);
        UtledeteVilkår utledeteVilkår = new EngangsstønadVilkårUtleder().utledVilkår(behandling, familieHendelseType);

        // Assert
        assertThat(utledeteVilkår.getPotensielleBetingedeVilkårtyper()).containsExactly(FØDSELSVILKÅRET_MOR);
        assertThat(utledeteVilkår.getBetinget()).isEqualTo(Optional.of(FØDSELSVILKÅRET_MOR));
        assertThat(utledeteVilkår.getAlleAvklarte()).containsExactly(FØDSELSVILKÅRET_MOR, MEDLEMSKAPSVILKÅRET, SØKNADSFRISTVILKÅRET, SØKERSOPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_opprette_vilkår_for_mor_som_søker_stønad_adopsjon() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        Behandling behandling = scenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        // Act
        final Optional<FamilieHendelseType> familieHendelseType = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon).map(FamilieHendelse::getType);
        UtledeteVilkår utledeteVilkår = new EngangsstønadVilkårUtleder().utledVilkår(behandling, familieHendelseType);

        // Assert
        assertThat(utledeteVilkår.getPotensielleBetingedeVilkårtyper()).containsExactly(ADOPSJONSVILKÅRET_ENGANGSSTØNAD);
        assertThat(utledeteVilkår.getBetinget()).isEqualTo(Optional.of(ADOPSJONSVILKÅRET_ENGANGSSTØNAD));
        assertThat(utledeteVilkår.getAlleAvklarte()).containsExactly(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, MEDLEMSKAPSVILKÅRET, SØKNADSFRISTVILKÅRET, SØKERSOPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_opprette_vilkår_for_far_som_søker_stønad_adopsjon() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medAdoptererAlene(true)
            .medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.medSøknad().medFarSøkerType(FarSøkerType.ADOPTERER_ALENE);
        Behandling behandling = scenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        // Act
        final Optional<FamilieHendelseType> familieHendelseType = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon).map(FamilieHendelse::getType);
        UtledeteVilkår utledeteVilkår = new EngangsstønadVilkårUtleder().utledVilkår(behandling, familieHendelseType);

        // Assert
        assertThat(utledeteVilkår.getPotensielleBetingedeVilkårtyper()).containsExactly(ADOPSJONSVILKÅRET_ENGANGSSTØNAD);
        assertThat(utledeteVilkår.getBetinget()).isEqualTo(Optional.of(ADOPSJONSVILKÅRET_ENGANGSSTØNAD));
        assertThat(utledeteVilkår.getAlleAvklarte()).containsExactly(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, MEDLEMSKAPSVILKÅRET, SØKNADSFRISTVILKÅRET, SØKERSOPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_opprette_vilkår_for_far_som_søker_stønad_adopsjon_ikke_alene() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now())
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET));
        scenario.medSøknad().medFarSøkerType(FarSøkerType.ANDRE_FORELDER_DØD);
        Behandling behandling = scenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        // Act
        final Optional<FamilieHendelseType> familieHendelseType = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon).map(FamilieHendelse::getType);
        UtledeteVilkår utledeteVilkår = new EngangsstønadVilkårUtleder().utledVilkår(behandling, familieHendelseType);

        // Assert
        assertThat(utledeteVilkår.getPotensielleBetingedeVilkårtyper())
            .containsExactlyInAnyOrder(OMSORGSVILKÅRET, FORELDREANSVARSVILKÅRET_2_LEDD, FORELDREANSVARSVILKÅRET_4_LEDD);
        assertThat(utledeteVilkår.getBetinget()).isEqualTo(Optional.empty());
        assertThat(utledeteVilkår.getAlleAvklarte()).containsExactly(MEDLEMSKAPSVILKÅRET, SØKNADSFRISTVILKÅRET, SØKERSOPPLYSNINGSPLIKT);
    }

    @Test
    public void skal_opprette_vilkår_når_far_søker_om_omsorgsovertakelse_ved_fødsel() {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET).medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.medSøknad().medFarSøkerType(FarSøkerType.OVERTATT_OMSORG_F);
        Behandling behandling = scenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();

        // Act
        final Optional<FamilieHendelseType> familieHendelseType = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon).map(FamilieHendelse::getType);
        UtledeteVilkår utledeteVilkår = new EngangsstønadVilkårUtleder().utledVilkår(behandling, familieHendelseType);

        // Assert
        assertThat(utledeteVilkår.getPotensielleBetingedeVilkårtyper())
            .containsExactlyInAnyOrder(OMSORGSVILKÅRET, FORELDREANSVARSVILKÅRET_2_LEDD, FORELDREANSVARSVILKÅRET_4_LEDD);
        assertThat(utledeteVilkår.getBetinget()).isEqualTo(Optional.empty());
        assertThat(utledeteVilkår.getAlleAvklarte()).containsExactly(MEDLEMSKAPSVILKÅRET, SØKNADSFRISTVILKÅRET, SØKERSOPPLYSNINGSPLIKT);
    }
}
