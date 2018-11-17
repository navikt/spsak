package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class VilkårResultatTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private final FagsakRepository fagsakReposiory = new FagsakRepositoryImpl(repoRule.getEntityManager());

    private Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().build();
    private Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
    private Behandling behandling1;

    @Before
    public void setup() {
        fagsakReposiory.opprettNy(fagsak);
        behandling1 = behandlingBuilder.build();
    }

    @Test
    public void skal_gjenbruke_vilkårresultat_i_ny_behandling_når_det_ikke_er_endret() throws Exception {
        // Arrange
        lagreBehandling(behandling1);

        Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        Behandlingsresultat behandlingsresultat1 = lagreOgGjenopphenteBehandlingsresultat(behandling1);

        Long id01 = behandlingsresultat1.getBehandling().getId();

        // Act
        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .medKopiAvForrigeBehandlingsresultat()
            .build();
        lagreBehandling(behandling2);
        Behandlingsresultat behandlingsresultat2 = lagreOgGjenopphenteBehandlingsresultat(behandling2);

        // Assert
        assertThat(behandling2.getBehandlingsresultat()).isNotSameAs(behandling1.getBehandlingsresultat());
        assertThat(behandling2.getBehandlingsresultat().getVilkårResultat())
                .isSameAs(behandling1.getBehandlingsresultat().getVilkårResultat());
        assertThat(behandling2.getBehandlingsresultat().getVilkårResultat())
                .isEqualTo(behandling1.getBehandlingsresultat().getVilkårResultat());

        Long id02 = behandlingsresultat2.getBehandling().getId();
        assertThat(id02).isNotEqualTo(id01);
    }

    @Test
    public void skal_opprette_nytt_vilkårresultat_i_ny_behandling_når_det_endrer_vilkårresultat() throws Exception {
        // Arrange
        lagreBehandling(behandling1);

        Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        Behandlingsresultat behandlingsresultat1 = lagreOgGjenopphenteBehandlingsresultat(behandling1);

        Long id01 = behandlingsresultat1.getBehandling().getId();

        // Act
        Behandling.Builder builder = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .medKopiAvForrigeBehandlingsresultat();
        Behandling behandling2 = builder.build();
        lagreBehandling(behandling2);

        // legg til et nytt vilkårsresultat
        VilkårResultat.builderFraEksisterende(behandling2.getBehandlingsresultat().getVilkårResultat())
                .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, VilkårUtfallMerknad.VM_1001, new Properties(), null, false, false, null, null)
                .buildFor(behandling2);

        Behandlingsresultat behandlingsresultat2 = lagreOgGjenopphenteBehandlingsresultat(behandling2);
        // Assert
        assertThat(behandling2.getBehandlingsresultat()).isNotSameAs(behandling1.getBehandlingsresultat());
        assertThat(behandling2.getBehandlingsresultat().getVilkårResultat())
                .isNotEqualTo(behandling1.getBehandlingsresultat().getVilkårResultat());

        Long id02 = behandlingsresultat2.getBehandling().getId();
        assertThat(id02).isNotEqualTo(id01);
    }

    @Test
    public void skal_legge_til_vilkår() throws Exception {
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .buildFor(behandling1);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_F, true, false, null, null)
            .buildFor(behandling1);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(2);

        Vilkår vilkår1 = oppdatertVilkårResultat.getVilkårene().stream().filter(v -> VilkårType.MEDLEMSKAPSVILKÅRET.equals(v.getVilkårType())).findFirst().orElse(null);
        assertThat(vilkår1).isNotNull();
        assertThat(vilkår1.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);

        Vilkår vilkår2 = oppdatertVilkårResultat.getVilkårene().stream().filter(v -> VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD.equals(v.getVilkårType())).findFirst().orElse(null);
        assertThat(vilkår2).isNotNull();
        assertThat(vilkår2.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
    }

    @Test
    public void skal_oppdatere_vilkår_med_nytt_utfall() throws Exception {
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkårResultat(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.SØKER_HAR_IKKE_FORELDREANSVAR, true, false, null, null)
            .buildFor(behandling1);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, VilkårUtfallType.OPPFYLT, null, new Properties(), null, true, false, null, null)
            .buildFor(behandling1);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        Vilkår vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD);
        assertThat(vilkår.getAvslagsårsak()).isNull();
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    @Test
    public void skal_overstyre_vilkår() throws Exception {
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, null, new Properties(), null, false, false, null, null)
            .buildFor(behandling1);

        // Act 1: Ikke oppfylt
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .overstyrVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, Avslagsårsak.SØKER_ER_UTVANDRET)
            .buildFor(behandling1);

        // Assert
        assertThat(oppdatertVilkårResultat.erOverstyrt()).isTrue();
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        Vilkår vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkår.getAvslagsårsak()).isEqualTo(Avslagsårsak.SØKER_ER_UTVANDRET);
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
        assertThat(vilkår.erOverstyrt()).isTrue();
        assertThat(vilkår.erManueltVurdert()).isTrue();

        // Act 2: Oppfylt
        oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(oppdatertVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .overstyrVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, null)
            .buildFor(behandling1);

        // Assert
        assertThat(oppdatertVilkårResultat.erOverstyrt()).isTrue();
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkår.getAvslagsårsak()).isEqualTo(null);
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.OPPFYLT);
        assertThat(vilkår.erOverstyrt()).isTrue();
        assertThat(vilkår.erManueltVurdert()).isTrue();
    }

    @Test
    public void skal_fjerne_vilkår() throws Exception {
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .leggTilVilkårResultat(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_F, true, false, null, null)
            .buildFor(behandling1);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .fjernVilkår(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD)
            .buildFor(behandling1);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        Vilkår vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);
    }

    private Behandlingsresultat lagreOgGjenopphenteBehandlingsresultat(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();

        assertThat(behandlingsresultat.getBehandling()).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat().getOriginalBehandling()).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        lagreBehandling(behandling);

        Long id = behandling.getId();
        assertThat(id).isNotNull();

        Behandling lagretBehandling = repository.hent(Behandling.class, id);
        assertThat(lagretBehandling).isEqualTo(behandling);
        assertThat(lagretBehandling.getBehandlingsresultat()).isEqualTo(behandlingsresultat);

        return behandlingsresultat;
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

}
