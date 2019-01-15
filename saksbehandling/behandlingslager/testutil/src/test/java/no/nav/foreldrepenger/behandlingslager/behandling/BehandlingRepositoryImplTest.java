package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingKandidaterRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class BehandlingRepositoryImplTest {

    private static final String ANSVARLIG_SAKSBEHANDLER = "Ansvarlig Saksbehandler";
    private final static int REVURDERING_DAGER_TILBAKE = 60;
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final Repository repository = repoRule.getRepository();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(entityManager);
    private final ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final BehandlingKandidaterRepository behandlingKandidaterRepository = new BehandlingKandidaterRepositoryImpl(entityManager);
    private final BehandlingVedtakRepository behandlingVedtakRepository = resultatRepositoryProvider.getVedtakRepository();
    private final FagsakRepository fagsakRepository = new FagsakRepositoryImpl(entityManager);
    private final AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(entityManager);
    private final Saksnummer saksnummer = new Saksnummer("2");
    private final Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).medSaksnummer(saksnummer).build();
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
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_ENDRING_BEREGNINGSGRUNNLAG)).build();

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
    public void skal_hente_liste_over_revurderingsaarsaker() {
        List<VurderÅrsak> aarsaksListe = repository.hentAlle(VurderÅrsak.class);
        assertThat(aarsaksListe.size()).isEqualTo(5);
        assertThat(aarsaksListe.stream().map(aarsak -> aarsak.getKode()).collect(Collectors.toList())).contains("FEIL_FAKTA");
    }

    // FIXME SP: Hvorfor slettes vilkårene? De burde kun vært logisk slettet.
    @Test
    public void skal_slette_vilkår_som_blir_fjernet_til_tross_for_at_Hibernate_har_problemer_med_orphan_removal() {
        // Arrange
        Fagsak fagsak = byggFagsak(new AktørId("199"));
        behandling = byggBehandlingForElektroniskSøknadOmFødsel(fagsak, LocalDate.now());

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().buildFor(behandling);

        VilkårResultat.builder()
            .leggTilVilkår(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT)
            .buildFor(behandlingsresultat);

        // Act
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        // Assert
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårene().size()).isEqualTo(1);
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType()).isEqualTo(VilkårType.MEDLEMSKAPSVILKÅRET);

        // Arrange
        VilkårResultat.builderFraEksisterende(behandlingsresultat.getVilkårResultat())
            .leggTilVilkår(VilkårType.OPPTJENINGSPERIODEVILKÅR, VilkårUtfallType.IKKE_VURDERT)
            .fjernVilkår(VilkårType.MEDLEMSKAPSVILKÅRET)
            .buildFor(behandlingsresultat);

        // Act
        behandlingRepository.lagre(behandling, lås);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        repository.flushAndClear();

        // Assert
        Behandling opphentetBehandling = repository.hent(Behandling.class, behandling.getId());
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårene().size()).isEqualTo(1);
        assertThat(behandlingsresultat.getVilkårResultat().getVilkårene().iterator().next().getVilkårType())
            .isEqualTo(VilkårType.OPPTJENINGSPERIODEVILKÅR);
        List<Vilkår> alleVilkår = repository.hentAlle(Vilkår.class);
        assertThat(alleVilkår.size()).isEqualTo(1);
        assertThat(alleVilkår.get(0)).isEqualTo(behandlingsresultat.getVilkårResultat().getVilkårene().iterator().next());
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
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør()
            .medBehandlingstidFrist(tidsfrist);
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);

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
        AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        scenario.lagre(repositoryProvider, resultatRepositoryProvider);
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

        LocalDate.now().plusDays(5);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        return behandling;
    }

    private Aksjonspunkt opprettAksjonspunkt(Behandling behandling,
                                             AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             LocalDateTime frist) {

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setFrist(aksjonspunkt, frist, Venteårsak.UDEFINERT);
        return aksjonspunkt;
    }

    private Fagsak byggFagsak(AktørId aktørId) {
        NavBruker navBruker = NavBruker.opprettNy(aktørId);
        Fagsak fagsak = FagsakBuilder.nyFagsak()
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private Behandling byggBehandlingForElektroniskSøknadOmFødsel(Fagsak fagsak, LocalDate mottattDato) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHANDLER);
        lagreBehandling(behandling);

        final Søknad søknad = new SøknadEntitet.Builder()
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(mottattDato)
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);

        return behandling;

    }

    private BehandlingVedtak.Builder opprettBuilderForVedtak() {
        behandling = opprettBehandlingMedTermindato();
        oppdaterMedBehandlingsresultatOgLagre(behandling, true, false);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());

        return BehandlingVedtak.builder().medVedtaksdato(LocalDate.now())
            .medAnsvarligSaksbehandler("Janne Hansen")
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medIverksettingStatus(IverksettingStatus.IKKE_IVERKSATT)
            .medBehandlingsresultat(behandlingsresultat);
    }

    private Behandling opprettBehandlingMedTermindato() {

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        return behandling;
    }

    private Behandling opprettRevurderingsKandidat(int dagerTilbake) {

        LocalDate.now().minusDays(dagerTilbake);
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();

        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderEndreEksisterende(behandlingRepository.hentResultat(behandling.getId()))
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        final BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder().medVedtaksdato(LocalDate.now()).medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultatType(VedtakResultatType.INNVILGET).medAnsvarligSaksbehandler("asdf").build();
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        scenario.avsluttBehandling(repositoryProvider, behandling);

        behandlingVedtakRepository.lagre(behandlingVedtak, lås);

        return behandling;
    }

    private Behandlingsresultat oppdaterMedBehandlingsresultatOgLagre(Behandling behandling, boolean innvilget, boolean henlegg) {
        Optional<Behandlingsresultat> behandlingsresultat1 = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        Behandlingsresultat behandlingsresultat = behandlingsresultat1.orElse(Behandlingsresultat.builder().buildFor(behandling));
        VilkårResultat.builder()
            .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, innvilget ? VilkårUtfallType.OPPFYLT : VilkårUtfallType.IKKE_OPPFYLT,
                null, new Properties(), null, false, false, null, null)
            .medVilkårResultatType(innvilget ? VilkårResultatType.INNVILGET : VilkårResultatType.AVSLÅTT)
            .buildFor(behandlingsresultat);

        if (henlegg) {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET);
        }

        repository.lagre(behandling);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        return behandlingsresultat;
    }

    private Behandling.Builder opprettBuilderForBehandling() {
        fagsakRepository.opprettNy(fagsak);
        return Behandling.forFørstegangssøknad(fagsak);

    }
}
