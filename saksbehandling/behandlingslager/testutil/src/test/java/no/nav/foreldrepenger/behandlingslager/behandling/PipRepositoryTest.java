package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.behandlingslager.pip.PipBehandlingsData;
import no.nav.foreldrepenger.behandlingslager.pip.PipRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class PipRepositoryTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("42");
    private static final Saksnummer SAKSNUMMER = new Saksnummer("100000001");
    private static final Saksnummer SAKSNUMMER2 = new Saksnummer("100000002");
    private static final String ANSVARLIG_SAKSBEHANDLER = "Z123455";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final PipRepository pipRepository = new PipRepository(repoRule.getEntityManager());
    private final FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private Behandling behandling;
    private Map<AktørId, NavBruker> aktørMap = new HashMap<>();

    private static Behandling byggForElektroniskSøknadOmFødsel(Fagsak fagsak, LocalDate mottattDato, String ansvarligSaksbehandler,
                                                               GrunnlagRepositoryProvider repositoryProvider) {
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = behandlingBuilder.build();
        behandling.setAnsvarligSaksbehandler(ansvarligSaksbehandler);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, new SøknadEntitet.Builder()
            .medSøknadReferanse(UUID.randomUUID().toString())
            .medSykemeldinReferanse(UUID.randomUUID().toString())
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(mottattDato)
            .build());

        return behandling;
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

    @Test
    public void skal_finne_behandligstatus_og_sakstatus_for_behandling() throws Exception {
        Fagsak fagsak = byggFagsak(new AktørId("200"), SAKSNUMMER);
        behandling = byggForElektroniskSøknadOmFødsel(fagsak, LocalDate.now(), ANSVARLIG_SAKSBEHANDLER, repositoryProvider);
        lagreBehandling(behandling);

        Optional<PipBehandlingsData> pipBehandlingsData = pipRepository.hentDataForBehandling(behandling.getId());
        assertThat(pipBehandlingsData.get()).isNotNull();
        assertThat(pipBehandlingsData.get().getBehandligStatus()).isEqualTo(behandling.getStatus().getKode());
        assertThat(pipBehandlingsData.get().getAnsvarligSaksbehandler().get()).isEqualTo(ANSVARLIG_SAKSBEHANDLER);
        assertThat(pipBehandlingsData.get().getFagsakStatus()).isEqualTo(behandling.getFagsak().getStatus().getKode());
    }

    @Test
    public void skal_returne_tomt_resultat_når_det_søkes_etter_behandling_id_som_ikke_finnes() throws Exception {
        Optional<PipBehandlingsData> pipBehandlingsData = pipRepository.hentDataForBehandling(1241L);
        assertThat(pipBehandlingsData).isNotPresent();
    }

    @Test
    public void skal_finne_alle_fagsaker_for_en_søker() throws Exception {
        Fagsak fagsak1 = byggFagsak(new AktørId("200"), SAKSNUMMER);
        Fagsak fagsak2 = byggFagsak(new AktørId("200"), SAKSNUMMER2);
        behandling = byggForElektroniskSøknadOmFødsel(fagsak1, LocalDate.now(), ANSVARLIG_SAKSBEHANDLER, repositoryProvider);
        lagreBehandling(behandling);

        Set<Long> resultat = pipRepository.fagsakIderForSøker(Collections.singleton(new AktørId("200")));

        assertThat(resultat).containsOnly(fagsak1.getId(), fagsak2.getId());
    }

    @Test
    public void skal_finne_aktoerId_for_fagsak() throws Exception {
        Fagsak fagsak = byggFagsak(new AktørId("200"), SAKSNUMMER);
        behandling = byggForElektroniskSøknadOmFødsel(fagsak, LocalDate.now(), ANSVARLIG_SAKSBEHANDLER, repositoryProvider);
        lagreBehandling(behandling);

        Set<AktørId> aktørIder = pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(fagsak.getId()));
        assertThat(aktørIder).containsOnly(new AktørId("200"));
    }

    @Test
    public void skal_finne_fagsakId_knyttet_til_journalpostId() throws Exception {
        Fagsak fagsak1 = byggFagsak(new AktørId("200"), SAKSNUMMER);
        @SuppressWarnings("unused")
        Fagsak fagsak2 = byggFagsak(new AktørId("200"), SAKSNUMMER2);
        Journalpost journalpost1 = new Journalpost(JOURNALPOST_ID, fagsak1);
        fagsakRepository.lagre(journalpost1);
        Journalpost journalpost2 = new Journalpost(new JournalpostId("4444"), fagsak1);
        fagsakRepository.lagre(journalpost2);
        repoRule.getRepository().flush();

        Set<Long> fagsakId = pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID));
        assertThat(fagsakId).containsOnly(fagsak1.getId());
    }

    @Test
    public void skal_finne_aksjonspunktTyper_for_aksjonspunktKoder() throws Exception {
        Set<String> resultat1 = pipRepository.hentAksjonspunktTypeForAksjonspunktKoder(Collections.singletonList(AksjonspunktDefinisjon.OVERSTYRING_AV_BEREGNING.getKode()));
        assertThat(resultat1).containsOnly("Overstyring");

        Set<String> resultat2 = pipRepository.hentAksjonspunktTypeForAksjonspunktKoder(Arrays.asList(AksjonspunktDefinisjon.OVERSTYRING_AV_BEREGNING.getKode(), AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD.getKode()));
        assertThat(resultat2).containsOnly("Overstyring", "Manuell");
    }

    private Fagsak byggFagsak(AktørId aktørId, Saksnummer saksnummer) {
        NavBruker navBruker = getNavBruker(aktørId);
        Fagsak fagsak = FagsakBuilder.nyFagsak()
            .medBruker(navBruker)
            .medSaksnummer(saksnummer).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

    private NavBruker getNavBruker(AktørId aktørId) {
        if (aktørMap.containsKey(aktørId)) {
            return aktørMap.get(aktørId);
        }
        final NavBruker bruker = NavBruker.opprettNy(aktørId);
        aktørMap.put(aktørId, bruker);
        return bruker;
    }
}
