package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
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
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class VilkårResultatTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final FagsakRepository fagsakReposiory = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private Repository repository = repoRule.getRepository();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("99"))).build();
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

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling1));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling1));
        Behandlingsresultat behandlingsresultat1 = lagreOgGjenopphenteBehandlingsresultat(behandling1);

        Long id01 = behandlingsresultat1.getBehandling().getId();

        // Act
        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .build();
        Behandlingsresultat behandlingsresultat2 = lagreOgKopierResultat(behandling2);

        // Assert
        assertThat(behandlingsresultat2).isNotSameAs(behandlingsresultat1);
        assertThat(behandlingsresultat2.getVilkårResultat())
            .isSameAs(behandlingsresultat1.getVilkårResultat());
        assertThat(behandlingsresultat2.getVilkårResultat())
            .isEqualTo(behandlingsresultat1.getVilkårResultat());

        Long id02 = behandlingsresultat2.getBehandling().getId();
        assertThat(id02).isNotEqualTo(id01);
    }

    private Behandlingsresultat lagreOgKopierResultat(Behandling behandling2) {
        lagreBehandling(behandling2);
        Optional<Behandlingsresultat> optResultat = behandlingRepository.hentResultatHvisEksisterer(behandling1.getId());
        if (optResultat.isPresent()) {
            behandlingRepository.lagre(Behandlingsresultat.builderFraEksisterende(optResultat.get()).buildFor(behandling2), behandlingRepository.taSkriveLås(behandling2));
        } else {
            behandlingRepository.lagre(Behandlingsresultat.builder().buildFor(behandling2), behandlingRepository.taSkriveLås(behandling2));
        }
        return lagreOgGjenopphenteBehandlingsresultat(behandling2);
    }

    @Test
    public void skal_opprette_nytt_vilkårresultat_i_ny_behandling_når_det_endrer_vilkårresultat() throws Exception {
        // Arrange
        lagreBehandling(behandling1);

        Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        Behandlingsresultat behandlingsresultat1 = lagreOgGjenopphenteBehandlingsresultat(behandling1);

        Long id01 = behandlingsresultat1.getBehandling().getId();

        // Act
        Behandling.Builder builder = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING);
        Behandling behandling2 = builder.build();
        lagreOgKopierResultat(behandling2);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling2.getId());

        // legg til et nytt vilkårsresultat
        VilkårResultat.builderFraEksisterende(behandlingsresultat.getVilkårResultat())
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, VilkårUtfallMerknad.VM_1001, new Properties(), null, false, false, null, null)
            .buildFor(behandlingsresultat);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling2));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling2));

//        Behandlingsresultat behandlingsresultat2 = lagreOgGjenopphenteBehandlingsresultat(behandling2);
        // Assert
        assertThat(behandlingsresultat).isNotSameAs(behandlingsresultat1);
        assertThat(behandlingsresultat.getVilkårResultat()).isNotEqualTo(behandlingsresultat1.getVilkårResultat());

        Long id02 = behandlingsresultat.getBehandling().getId();
        assertThat(id02).isNotEqualTo(id01);
    }

    @Test
    public void skal_legge_til_vilkår() throws Exception {
        lagreBehandling(behandling1);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling1));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling1));
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .buildFor(behandlingsresultat);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, true, false, null, null)
            .buildFor(behandlingsresultat);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(2);

        Vilkår vilkår1 = oppdatertVilkårResultat.getVilkårene().stream().filter(v -> VilkårType.MEDLEMSKAPSVILKÅRET.equals(v.getVilkårType())).findFirst().orElse(null);
        assertThat(vilkår1).isNotNull();
        assertThat(vilkår1.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);

        Vilkår vilkår2 = oppdatertVilkårResultat.getVilkårene().stream().filter(v -> VilkårType.OPPTJENINGSPERIODEVILKÅR.equals(v.getVilkårType())).findFirst().orElse(null);
        assertThat(vilkår2).isNotNull();
        assertThat(vilkår2.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_OPPFYLT);
    }

    @Test
    public void skal_oppdatere_vilkår_med_nytt_utfall() throws Exception {
        lagreBehandling(behandling1);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling1));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling1));
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.SØKER_ER_IKKE_MEDLEM, true, false, null, null)
            .buildFor(behandlingsresultat);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, null, new Properties(), null, true, false, null, null)
            .buildFor(behandlingsresultat);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        Vilkår vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkår.getAvslagsårsak()).isNull();
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.OPPFYLT);
    }

    @Test
    public void skal_overstyre_vilkår() throws Exception {
        lagreBehandling(behandling1);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling1));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling1));
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.INNVILGET)
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, null, new Properties(), null, false, false, null, null)
            .buildFor(behandlingsresultat);

        // Act 1: Ikke oppfylt
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .overstyrVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, Avslagsårsak.SØKER_ER_UTVANDRET)
            .buildFor(behandlingsresultat);

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
            .buildFor(behandlingsresultat);

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
        lagreBehandling(behandling1);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling1);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), behandlingRepository.taSkriveLås(behandling1));
        behandlingRepository.lagre(behandlingsresultat, behandlingRepository.taSkriveLås(behandling1));
        // Arrange
        VilkårResultat opprinneligVilkårResultat = VilkårResultat.builder()
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .leggTilVilkårResultat(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_OPPFYLT, null, new Properties(), Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, true, false, null, null)
            .buildFor(behandlingsresultat);

        // Act
        VilkårResultat oppdatertVilkårResultat = VilkårResultat.builderFraEksisterende(opprinneligVilkårResultat)
            .fjernVilkår(VilkårType.OPPTJENINGSPERIODEVILKÅR)
            .buildFor(behandlingsresultat);

        // Assert
        assertThat(oppdatertVilkårResultat.getVilkårene().size()).isEqualTo(1);
        Vilkår vilkår = oppdatertVilkårResultat.getVilkårene().get(0);
        assertThat(vilkår.getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);
        assertThat(vilkår.getGjeldendeVilkårUtfall()).isEqualTo(VilkårUtfallType.IKKE_VURDERT);
    }

    private Behandlingsresultat lagreOgGjenopphenteBehandlingsresultat(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId()).orElse(Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling));

        assertThat(behandlingsresultat.getBehandling()).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat().getOriginalBehandling()).isNotNull();
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårResultatType()).isEqualTo(VilkårResultatType.IKKE_FASTSATT);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        behandlingRepository.lagre(behandling, lås);

        Long id = behandling.getId();
        assertThat(id).isNotNull();

        Behandling lagretBehandling = repository.hent(Behandling.class, id);
        assertThat(lagretBehandling).isEqualTo(behandling);
        assertThat(behandlingsresultat.getBehandling()).isEqualTo(lagretBehandling);

        return behandlingsresultat;
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

}
