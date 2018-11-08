package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingRepositoryImplTest {

    private static final String ANSVARLIG_SAKSBEHANDLER = "Ansvarlig Saksbehandler";
    private final static int REVURDERING_DAGER_TILBAKE = 60;
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BehandlingKandidaterRepository behandlingKandidaterRepository = new BehandlingKandidaterRepositoryImpl(entityManager);
    private final BehandlingVedtakRepository behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    private final FagsakRepository fagsakRepository = new FagsakRepositoryImpl(entityManager);
    private final AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(entityManager);
    private final Saksnummer saksnummer = new Saksnummer("2");
    private final Fagsak fagsak = FagsakBuilder.nyEngangstønadForMor().medSaksnummer(saksnummer).build();
    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(entityManager);
    private Behandling behandling;

    private LocalDateTime imorgen = LocalDateTime.now().plusDays(1);
    private LocalDateTime igår = LocalDateTime.now().minusDays(1);

    @Test
    public void skal_finne_behandling_gitt_id() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling().build();
        lagreBehandling(behandling);

        // Act
        Behandling resultat = behandlingRepository.hentBehandling(behandling.getId());

        // Assert
        assertThat(resultat).isNotNull();
    }

    private void lagreBehandling(Behandling... behandlinger) {
        for (Behandling behandling : behandlinger) {
            BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
            behandlingRepository.lagre(behandling, lås);
        }
    }

    @Test
    public void skal_hente_alle_behandlinger_fra_fagsak() {

        Behandling.Builder builder = opprettBuilderForBehandling();
        lagreBehandling(builder);

        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(saksnummer);

        assertThat(behandlinger).hasSize(1);

    }

    private void lagreBehandling(Behandling.Builder builder) {
        Behandling behandling = builder.build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

    @Test
    public void skal_finne_behandling_med_årsak() {
        Behandling behandling = opprettRevurderingsKandidat(REVURDERING_DAGER_TILBAKE + 2);

        Behandling revurderingsBehandling = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_AVVIK_ANTALL_BARN)).build();

        behandlingRepository.lagre(revurderingsBehandling, behandlingRepository.taSkriveLås(revurderingsBehandling));

        List<Behandling> result = behandlingRepository.hentBehandlingerMedÅrsakerForFagsakId(behandling.getFagsakId(),
            BehandlingÅrsakType.årsakerForAutomatiskRevurdering());
        assertThat(result).isNotEmpty();
    }

    @Test
    public void skal_hente_siste_behandling_basert_på_fagsakId() {

        Behandling.Builder builder = opprettBuilderForBehandling();

        lagreBehandling(builder);

        Optional<Behandling> sisteBehandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId());

        assertThat(sisteBehandling).isPresent();
        assertThat(sisteBehandling.get().getFagsakId()).isEqualTo(fagsak.getId());

    }

    @Test
    public void skal_hente_siste_behandling_ekskluder_basert_på_fagsakId() {
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forUtenVurderingResultat(ScenarioMorSøkerEngangsstønad.forAdopsjon());
        Behandling klageBehandling = scenario.lagre(repositoryProvider);

        List<Behandling> alleBehandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(klageBehandling.getFagsak().getSaksnummer());
        assertThat(alleBehandlinger).as("Forventer at alle behandlinger opprettet skal eksistere").hasSize(2);

        Optional<Behandling> sisteBehandling = behandlingRepository
            .hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(klageBehandling.getFagsak().getId(), Arrays.asList(BehandlingType.KLAGE));

        assertThat(sisteBehandling).isPresent();
        assertThat(sisteBehandling.get().getFagsakId()).isEqualTo(klageBehandling.getFagsak().getId());
        assertThat(sisteBehandling.get().getType()).isNotEqualTo(BehandlingType.KLAGE);
    }

    @Test
    public void skal_kunne_lagre_vedtak() {
        BehandlingVedtak vedtak = opprettBuilderForVedtak().build();

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        behandlingRepository.lagre(behandling, lås);
        behandlingVedtakRepository.lagre(vedtak, lås);

        Long id = vedtak.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        BehandlingVedtak vedtakLest = repository.hent(BehandlingVedtak.class, id);
        assertThat(vedtakLest).isNotNull();

    }

    @Test
    public void skal_kunne_lagre_konsekvens_for_ytelsen() {
        behandling = opprettBehandlingMedTermindato();
        Behandlingsresultat behandlingsresultat = oppdaterMedBehandlingsresultatOgLagre(behandling, true, false);

        setKonsekvensForYtelsen(behandlingsresultat, Arrays.asList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK));
        List<BehandlingsresultatKonsekvensForYtelsen> brKonsekvenser = repository.hentAlle(BehandlingsresultatKonsekvensForYtelsen.class);
        assertThat(brKonsekvenser).hasSize(2);
        brKonsekvenser.forEach(brk -> assertThat(brk.getBehandlingsresultat()).isNotNull());
        List<KonsekvensForYtelsen> konsekvenser = brKonsekvenser.stream().map(BehandlingsresultatKonsekvensForYtelsen::getKonsekvensForYtelsen).collect(Collectors.toList());
        assertThat(konsekvenser).containsExactlyInAnyOrder(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK);
    }

    @Test
    public void dersom_man_lagrer_konsekvens_for_ytelsen_flere_ganger_skal_kun_den_siste_lagringen_gjelde() {
        behandling = opprettBehandlingMedTermindato();
        Behandlingsresultat behandlingsresultat = oppdaterMedBehandlingsresultatOgLagre(behandling, true, false);

        setKonsekvensForYtelsen(behandlingsresultat, Arrays.asList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.ENDRING_I_UTTAK));
        behandling = behandlingRepository.hentBehandling(behandling.getId());
        Behandlingsresultat.builderEndreEksisterende(behandling.getBehandlingsresultat()).fjernKonsekvenserForYtelsen();
        setKonsekvensForYtelsen(behandling.getBehandlingsresultat(), Arrays.asList(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN));

        List<BehandlingsresultatKonsekvensForYtelsen> brKonsekvenser = repository.hentAlle(BehandlingsresultatKonsekvensForYtelsen.class);
        assertThat(brKonsekvenser).hasSize(1);
        brKonsekvenser.forEach(brk -> assertThat(brk.getBehandlingsresultat()).isNotNull());
        List<KonsekvensForYtelsen> konsekvenser = brKonsekvenser.stream().map(BehandlingsresultatKonsekvensForYtelsen::getKonsekvensForYtelsen).collect(Collectors.toList());
        assertThat(konsekvenser).containsExactlyInAnyOrder(KonsekvensForYtelsen.ENDRING_I_FORDELING_AV_YTELSEN);
    }

    private void setKonsekvensForYtelsen(Behandlingsresultat behandlingsresultat, List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        Behandlingsresultat.Builder builder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        konsekvenserForYtelsen.forEach(builder::leggTilKonsekvensForYtelsen);
        builder.buildFor(behandling);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        repository.flushAndClear();
    }

    @Test
    public void skal_kunne_lagre_klageVurderingResultat() {
        // Arrange
        KlageVurderingResultat klageVurderingResultat = opprettBuilderForKlageVurderingResultat().build();

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        behandlingRepository.lagre(behandling, lås);

        // Act
        behandlingRepository.lagre(klageVurderingResultat, lås);

        // Assert
        Long id = klageVurderingResultat.getId();
        assertThat(id).isNotNull();

        repository.flushAndClear();
        KlageVurderingResultat klageVurderingResultatLest = repository.hent(KlageVurderingResultat.class, id);
        assertThat(klageVurderingResultatLest).isNotNull();
    }

    @Test
    public void skal_hente_liste_over_revurderingsaarsaker() {
        List<VurderÅrsak> aarsaksListe = repository.hentAlle(VurderÅrsak.class);
        assertThat(aarsaksListe.size()).isEqualTo(5);
        assertThat(aarsaksListe.get(0).getKode()).isEqualTo("FEIL_FAKTA");
    }

    @Test
    public void skal_slette_vilkår_som_blir_fjernet_til_tross_for_at_Hibernate_har_problemer_med_orphan_removal() {
        // Arrange
        Fagsak fagsak = byggFagsak(new AktørId("199"), RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, LocalDate.now(), LocalDate.now());

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.OMSORGSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .buildFor(behandling);

        // Act
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), lås);

        // Assert
        assertThat(behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().size()).isEqualTo(1);
        assertThat(behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().iterator().next().getVilkårType()).isEqualTo(VilkårType.OMSORGSVILKÅRET);

        // Arrange
        VilkårResultat.builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            .leggTilVilkår(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD, VilkårUtfallType.IKKE_VURDERT)
            .fjernVilkår(VilkårType.OMSORGSVILKÅRET)
            .buildFor(behandling);

        // Act
        behandlingRepository.lagre(behandling, lås);
        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), lås);
        repository.flushAndClear();

        // Assert
        Behandling opphentetBehandling = repository.hent(Behandling.class, behandling.getId());
        assertThat(opphentetBehandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().size()).isEqualTo(1);
        assertThat(opphentetBehandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
            .isEqualTo(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);
        List<Vilkår> alleVilkår = repository.hentAlle(Vilkår.class);
        assertThat(alleVilkår.size()).isEqualTo(1);
        assertThat(alleVilkår.get(0)).isEqualTo(opphentetBehandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().iterator().next());
    }

    @Test
    public void skal_slette_klage_resultat() {
        // Arrange
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forAvvistNK(ScenarioFarSøkerEngangsstønad.forAdopsjon());
        behandling = scenario.lagre(repositoryProvider);
        entityManager.flush();

        // Asserting arrangement
        Behandling behandlingMedKlageVR = behandlingRepository.hentBehandling(behandling.getId());

        assertThat(behandlingMedKlageVR.hentKlageVurderingResultat(KlageVurdertAv.NFP))
            .as("Mangler KlageVurderingResultat gitt av NFP").isPresent();
        assertThat(behandlingMedKlageVR.hentKlageVurderingResultat(KlageVurdertAv.NK))
            .as("Mangler KlageVurderingResultat gitt av NK").isPresent();

        // Act
        behandlingRepository.slettKlageVurderingResultat(behandlingMedKlageVR, behandlingRepository.taSkriveLås(behandlingMedKlageVR), KlageVurdertAv.NK);
        behandlingRepository.slettKlageVurderingResultat(behandlingMedKlageVR, behandlingRepository.taSkriveLås(behandlingMedKlageVR), KlageVurdertAv.NFP);
        repository.flushAndClear();

        // Assert
        Behandling behandlingEtterSletting = behandlingRepository.hentBehandling(behandling.getId());
        assertThat(behandlingEtterSletting.hentKlageVurderingResultat(KlageVurdertAv.NFP))
            .as("KlageVurderingResultat gitt av NFP ikke fjernet.").isNotPresent();

        assertThat(behandlingEtterSletting.hentKlageVurderingResultat(KlageVurdertAv.NK))
            .as("KlageVurderingResultat gitt av NK ikke fjernet.").isNotPresent();
    }

    @Test
    public void skal_finne_for_automatisk_gjenopptagelse_naar_alle_kriterier_oppfylt() {

        // Arrange
        Behandling behandling1 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, igår);
        opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, igår);

        Behandling behandling2 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling2, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, igår);

        Behandling behandling3 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling3, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, igår);
        lagreBehandling(behandling1, behandling2, behandling3);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).hasSize(3);
        assertThat(liste).contains(behandling1);
        assertThat(liste).contains(behandling2);
        assertThat(liste).contains(behandling3);
    }

    @Test
    public void skal_ikke_finne_for_automatisk_gjenopptagelse_naar_naar_manuelt_aksjonspunkt() {

        // Arrange
        Behandling behandling1 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.MANUELL_VURDERING_AV_MEDLEMSKAP, igår);
        lagreBehandling(behandling1);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_ikke_finne_for_automatisk_gjenopptagelse_naar_naar_lukket_aksjonspunkt() {

        LocalDateTime.now().minusDays(1);
        Behandling behandling1 = opprettBehandlingForAutomatiskGjenopptagelse();
        Aksjonspunkt aksjonspunkt = opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, igår);
        aksjonspunktRepository.setTilUtført(aksjonspunkt, "ferdig");
        lagreBehandling(behandling1);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_ikke_finne_for_automatisk_gjenopptagelse_naar_aksjonspunkt_frist_ikke_utgaatt() {

        // Arrange
        Behandling behandling1 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, imorgen);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_ikke_finne_for_automatisk_gjenopptagelse_naar_aksjonspunkt_er_køet() {

        // Arrange
        Behandling behandling1 = opprettBehandlingForAutomatiskGjenopptagelse();
        opprettAksjonspunkt(behandling1, AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING, imorgen);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_ikek_finne_for_automatisk_gjenopptagelse_når_aksjonspunt_er_inaktivt() throws Exception {
        // Arrange
        Behandling behandling = opprettBehandlingForAutomatiskGjenopptagelse();
        Aksjonspunkt aksjonspunkt = opprettAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT, igår);
        aksjonspunktRepository.deaktiver(aksjonspunkt);
        lagreBehandling(behandling);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        // Assert
        assertThat(liste).isEmpty();
    }

    @Test
    public void skal_finne_førstegangsbehandling_naar_frist_er_utgatt() {
        // Arrange
        LocalDate tidsfrist = LocalDate.now().minusDays(1);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBehandlingstidFrist(tidsfrist);
        final FamilieHendelseBuilder familieHendelseBuilder = scenario.medSøknadHendelse();
        familieHendelseBuilder.medAntallBarn(1)
            .medFødselsDato(LocalDate.now());
        scenario.lagre(repositoryProvider);

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerMedUtløptBehandlingsfrist();

        // Assert
        assertThat(liste).hasSize(1);
    }

    @Test
    public void skal_ikke_finne_revurderingsbehandling() {
        // Arrange
        Behandling behandling = opprettRevurderingsKandidat(REVURDERING_DAGER_TILBAKE + 2);

        LocalDate tidsfrist = LocalDate.now().minusDays(1);
        Behandling revurderingsBehandling = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingstidFrist(tidsfrist).build();
        behandlingRepository.lagre(revurderingsBehandling, behandlingRepository.taSkriveLås(revurderingsBehandling));

        // Act
        List<Behandling> liste = behandlingKandidaterRepository.finnBehandlingerMedUtløptBehandlingsfrist();

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_opprettholde_id_etter_endringer() {

        // Lagre Personopplysning
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medAntallBarn(1).medFødselsDato(LocalDate.now());
        scenario.lagre(repositoryProvider);
    }

    @Test
    public void skal_finne_årsaker_for_behandling() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling()
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)
                .medManueltOpprettet(false))
            .build();
        lagreBehandling(behandling);

        // Act
        List<BehandlingÅrsak> liste = behandlingRepository.finnÅrsakerForBehandling(behandling);

        // Assert
        assertThat(liste).hasSize(1);
        assertThat(liste.get(0).getBehandlingÅrsakType()).isEqualTo(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
    }

    @Test
    public void skal_finne_årsakstyper_for_behandling() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling()
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ANNET)
                .medManueltOpprettet(false))
            .build();
        lagreBehandling(behandling);

        // Act
        List<BehandlingÅrsakType> liste = behandlingRepository.finnÅrsakTyperForBehandling(behandling);

        // Assert
        assertThat(liste).hasSize(1);
        assertThat(liste.get(0)).isEqualTo(BehandlingÅrsakType.RE_ANNET);
    }

    @Test
    public void skal_ikke_finne_noen_årsakstyper_hvis_ingen() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling()
            .build();
        lagreBehandling(behandling);

        // Act
        List<BehandlingÅrsakType> liste = behandlingRepository.finnÅrsakTyperForBehandling(behandling);

        // Assert
        assertThat(liste).hasSize(0);
    }

    @Test
    public void skal_ikke_finne_noen_årsaker_hvis_ingen() {

        // Arrange
        Behandling behandling = opprettBuilderForBehandling()
            .build();
        lagreBehandling(behandling);

        // Act
        List<BehandlingÅrsak> liste = behandlingRepository.finnÅrsakerForBehandling(behandling);

        // Assert
        assertThat(liste).hasSize(0);
    }

    private Behandling opprettBehandlingForAutomatiskGjenopptagelse() {

        LocalDate terminDato = LocalDate.now().plusDays(5);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(terminDato)
                .medUtstedtDato(LocalDate.now())
                .medNavnPå("Lege Legesen"))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(terminDato)
                .medNavnPå("NAVNSENASDA ")
                .medUtstedtDato(terminDato.minusDays(40)))
            .medAntallBarn(1));

        Behandling behandling = scenario.lagre(repositoryProvider);
        return behandling;
    }

    private Aksjonspunkt opprettAksjonspunkt(Behandling behandling,
                                             AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             LocalDateTime frist) {

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setFrist(aksjonspunkt, frist, Venteårsak.UDEFINERT);
        return aksjonspunkt;
    }

    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private Behandling byggBehandlingForElektroniskSøknadOmFødsel(Fagsak fagsak, LocalDate fødselsdato, LocalDate mottattDato) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        lagreBehandling(behandling);
        final FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(fødselsdato);
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(mottattDato)
            .medElektroniskRegistrert(true)
            .medFamilieHendelse(repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon()).build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;

    }

    private BehandlingVedtak.Builder opprettBuilderForVedtak() {
        behandling = opprettBehandlingMedTermindato();
        oppdaterMedBehandlingsresultatOgLagre(behandling, true, false);

        return BehandlingVedtak.builder().medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("Janne Hansen")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .medBehandlingsresultat(behandling.getBehandlingsresultat());
    }

    private KlageVurderingResultat.Builder opprettBuilderForKlageVurderingResultat() {
        behandling = opprettBehandlingMedTermindato();
        oppdaterMedBehandlingsresultatOgLagre(behandling, true, false);

        return KlageVurderingResultat.builder().medBehandling(behandling)
            .medKlageVurdertAv(KlageVurdertAv.NK)
            .medKlageVurdering(KlageVurdering.AVVIS_KLAGE)
            .medKlageAvvistÅrsak(KlageAvvistÅrsak.KLAGE_UGYLDIG)
            .medVedtaksdatoPåklagdBehandling(LocalDate.now())
            .medBegrunnelse("begrunnelse klage avvist");
    }

    private Behandling opprettBehandlingMedTermindato() {

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("ASDASD ASD ASD")
                .medUtstedtDato(LocalDate.now())
                .medTermindato(LocalDate.now().plusDays(40)))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medUtstedtDato(LocalDate.now().minusDays(7))
                .medNavnPå("NAVN"))
            .medAntallBarn(1));

        behandling = scenario.lagre(repositoryProvider);
        return behandling;
    }

    private Behandling opprettRevurderingsKandidat(int dagerTilbake) {

        LocalDate terminDato = LocalDate.now().minusDays(dagerTilbake);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medNavnPå("ASDASD ASD ASD")
                .medUtstedtDato(LocalDate.now().minusDays(40))
                .medTermindato(terminDato))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse(scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(terminDato)
                .medNavnPå("LEGESEN")
                .medUtstedtDato(terminDato.minusDays(40)))
            .medAntallBarn(1));

        behandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now()).medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultatType(VedtakResultatType.INNVILGET).medAnsvarligSaksbehandler("asdf").build();
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        behandlingVedtakRepository.lagre(behandlingVedtak, behandlingRepository.taSkriveLås(behandling));

        return behandling;
    }

    private Behandlingsresultat oppdaterMedBehandlingsresultatOgLagre(Behandling behandling, boolean innvilget, boolean henlegg) {
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.FØDSELSVILKÅRET_MOR, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT,
                null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandling);
        if (innvilget) {
            BeregningResultat.builder()
                .medBeregning(new Beregning(48500L, 1L, 48500L, LocalDateTime.now()))
                .buildFor(behandling);
        }

        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (henlegg) {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        }

        repository.lagre(behandling);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        if (innvilget) {
            beregningRepository.lagre(behandlingsresultat.getBeregningResultat(), lås);
        }
        return behandlingsresultat;
    }

    private Behandling.Builder opprettBuilderForBehandling() {
        fagsakRepository.opprettNy(fagsak);
        return Behandling.forFørstegangssøknad(fagsak);

    }
}
