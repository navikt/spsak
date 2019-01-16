package no.nav.foreldrepenger.behandling.revurdering;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandling.revurdering.FagsakRevurdering;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class FagsakRevurderingTest {
    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());

    private BehandlingRepository behandlingRepository;
    private Behandling behandling;
    private Behandling nyesteBehandling;
    private Behandling eldreBehandling;
    private Fagsak fagsak;
    private Saksnummer fagsakSaksnummer = new Saksnummer("1");

    private Fagsak fagsakMedFlereBehandlinger;
    private Saksnummer fagsakMedFlereBehSaksnr = new Saksnummer("2");
    private FagsakRevurdering tjeneste;

    @Before
    public void setup() {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Before
    public void opprettBehandlinger() {
        fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("1234"))).medSaksnummer(fagsakSaksnummer).build();
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.opprettNy(fagsak);
        behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        fagsakMedFlereBehandlinger = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("12356"))).medSaksnummer(fagsakMedFlereBehSaksnr).build();
        fagsakRepository.opprettNy(fagsakMedFlereBehandlinger);
        nyesteBehandling = Behandling.forFørstegangssøknad(fagsakMedFlereBehandlinger)
            .medAvsluttetDato(LocalDateTime.now())
            .build();
        behandlingRepository.lagre(nyesteBehandling, behandlingRepository.taSkriveLås(nyesteBehandling));
        eldreBehandling = Behandling.forFørstegangssøknad(fagsakMedFlereBehandlinger)
            .medAvsluttetDato(LocalDateTime.now().minusDays(1))
            .build();
        behandlingRepository.lagre(eldreBehandling, behandlingRepository.taSkriveLås(eldreBehandling));

        tjeneste = new FagsakRevurdering(behandlingRepository);
    }

    @Test
    public void kanIkkeOppretteRevurderingNårÅpenBehandling() throws Exception {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsak);
        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void kanIkkeOppretteRevurderingNårBehandlingErHenlagt() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET).buildFor(behandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsak);

        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void kanOppretteRevurderingNårEnBehandlingErVedtattMenSisteBehandlingErHenlagt() {
        avsluttBehandling(eldreBehandling);
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET).buildFor(nyesteBehandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(nyesteBehandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        avsluttBehandling(nyesteBehandling);
        behandlingRepository.lagre(nyesteBehandling, lås);
        Behandlingsresultat behandlingsresultat1 = Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(eldreBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(behandlingsresultat1);
        BehandlingLås lås1 = behandlingRepository.taSkriveLås(eldreBehandling);
        behandlingRepository.lagre(behandlingsresultat1.getVilkårResultat(), lås1);
        behandlingRepository.lagre(behandlingsresultat1, lås1);
        behandlingRepository.lagre(eldreBehandling, lås1);

        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isTrue();
    }

    private void avsluttBehandling(Behandling behandling) {
        Whitebox.setInternalState(behandling, "status", BehandlingStatus.AVSLUTTET);
    }

    @Test
    public void kanOppretteRevurderingNårFlereBehandlingerErVedtattOgSisteKanRevurderes() {
        avsluttBehandling(eldreBehandling);
        avsluttBehandling(nyesteBehandling);
        Behandlingsresultat behandlingsresultat1 = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(eldreBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET,
            VilkårUtfallType.IKKE_OPPFYLT, null, null,
            null, false, false, null, null).buildFor(behandlingsresultat1);
        BehandlingLås lås1 = behandlingRepository.taSkriveLås(nyesteBehandling);
        behandlingRepository.lagre(behandlingsresultat1.getVilkårResultat(), lås1);
        behandlingRepository.lagre(behandlingsresultat1, lås1);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(nyesteBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(behandlingsresultat);
        BehandlingLås lås = behandlingRepository.taSkriveLås(nyesteBehandling);
        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isTrue();
    }

    @Test
    public void kanIkkeOppretteRevurderingNårFlereBehandlingerErVedtattOgSisteIkkeKanRevurderes() {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(nyesteBehandling);
        VilkårResultat vilkårResultat = VilkårResultat.builder().leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET,
            VilkårUtfallType.IKKE_OPPFYLT, null, null,
            null, false, false, null, null).buildFor(behandlingsresultat);
        BehandlingLås lås = behandlingRepository.taSkriveLås(nyesteBehandling);
        behandlingRepository.lagre(vilkårResultat, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        Behandlingsresultat behandlingsresultat1 = Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(eldreBehandling);
        VilkårResultat vilkårResultat1 = VilkårResultat.builder().leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(behandlingsresultat1);
        BehandlingLås lås1 = behandlingRepository.taSkriveLås(nyesteBehandling);
        behandlingRepository.lagre(vilkårResultat1, lås1);
        behandlingRepository.lagre(behandlingsresultat1, lås1);

        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void behandlingerSkalSorteresSynkendePåAvsluttetDato() {
        Fagsak fagsak = FagsakBuilder.nyFagsak().build();
        LocalDateTime now = LocalDateTime.now();
        Behandling nyBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now).build();
        Behandling gammelBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now.minusDays(1)).build();

        FagsakRevurdering.BehandlingAvsluttetDatoComparator behandlingAvsluttetDatoComparator = new FagsakRevurdering.BehandlingAvsluttetDatoComparator();

        List<Behandling> behandlinger = asList(nyBehandling, gammelBehandling);
        List<Behandling> sorterteBehandlinger = behandlinger.stream().sorted(behandlingAvsluttetDatoComparator).collect(Collectors.toList());

        assertThat(sorterteBehandlinger.get(0).getAvsluttetDato()).isEqualTo(now);
    }

    @Test
    public void behandlingerSkalSorteresSynkendePåOpprettetDatoNårAvsluttetDatoErNull() {
        Fagsak fagsak = FagsakBuilder.nyFagsak().build();
        LocalDateTime now = LocalDateTime.now();
        Behandling nyBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(null).medOpprettetDato(now).build();
        Behandling gammelBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now).medOpprettetDato(now.minusDays(1)).build();

        FagsakRevurdering.BehandlingAvsluttetDatoComparator behandlingAvsluttetDatoComparator = new FagsakRevurdering.BehandlingAvsluttetDatoComparator();

        List<Behandling> behandlinger = asList(nyBehandling, gammelBehandling);
        List<Behandling> sorterteBehandlinger = behandlinger.stream().sorted(behandlingAvsluttetDatoComparator).collect(Collectors.toList());

        assertThat(sorterteBehandlinger.get(0).getAvsluttetDato()).isNull();
        assertThat(sorterteBehandlinger.get(0).getOpprettetDato()).isEqualTo(now);
    }
}
