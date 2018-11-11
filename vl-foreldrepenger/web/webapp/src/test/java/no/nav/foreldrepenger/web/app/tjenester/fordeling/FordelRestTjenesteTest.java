package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.BehandlendeFagsystem;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.impl.FagsakTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.vurderfagsystem.VurderFagsystem;
import no.nav.foreldrepenger.domene.vurderfagsystem.VurderFagsystemTjeneste;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakOrchestrator;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakTjeneste;

public class FordelRestTjenesteTest {

    private static final AktørId AKTØR_ID_MOR = new AktørId("1");

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjenesteMock;
    private FagsakTjeneste fagsakTjenesteMock;
    private OpprettSakOrchestrator opprettSakOrchestratorMock;
    private OpprettSakTjeneste opprettSakTjenesteMock;

    private VurderFagsystemTjeneste vurderFagsystemTjenesteMock;
    private DokumentArkivTjeneste dokumentArkivTjeneste;

    private BehandlingRepositoryProvider kodeverkRepository = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private FordelRestTjeneste fordelRestTjeneste;

    private KodeverkRepository kverkRepo = KodeverkTestHelper.getKodeverkRepository();

    @Before
    public void setup() {
        dokumentmottakTjenesteMock = mock(SaksbehandlingDokumentmottakTjeneste.class);
        dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
        fagsakTjenesteMock = new FagsakTjenesteImpl(kodeverkRepository, null);
        opprettSakOrchestratorMock = mock(OpprettSakOrchestrator.class);
        opprettSakTjenesteMock = mock(OpprettSakTjeneste.class);
        vurderFagsystemTjenesteMock = mock(VurderFagsystemTjeneste.class);

        fordelRestTjeneste = new FordelRestTjeneste(dokumentmottakTjenesteMock,  dokumentArkivTjeneste,
            fagsakTjenesteMock, opprettSakOrchestratorMock, opprettSakTjenesteMock,kodeverkRepository,vurderFagsystemTjenesteMock);
    }

    @Test
    public void skalReturnereFagsystemVedtaksløsning() {
        Saksnummer saksnummer  = new Saksnummer("12345");
        VurderFagsystemDto innDto = new VurderFagsystemDto("1234", true, "1", "ab0047");
        BehandlendeFagsystem behandlendeFagsystem = new BehandlendeFagsystem(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        behandlendeFagsystem = behandlendeFagsystem.medSaksnummer(saksnummer);

        when(vurderFagsystemTjenesteMock.vurderFagsystem(any(VurderFagsystem.class))).thenReturn(behandlendeFagsystem);

        BehandlendeFagsystemDto result = fordelRestTjeneste.vurderFagsystem(innDto);

        assertThat(result).isNotNull();
        assertThat(String.valueOf(result.getSaksnummer().get())).isEqualTo(saksnummer.getVerdi());
        assertThat(result.isBehandlesIVedtaksløsningen()).isTrue();
    }

    @Test
    public void skalReturnereFagsystemManuell() {
        Saksnummer saksnummer  = new Saksnummer("12345");
        JournalpostId journalpostId = new JournalpostId("1234");
        VurderFagsystemDto innDto = new VurderFagsystemDto(journalpostId.getVerdi(), false, "1", "ab0047");
        innDto.setDokumentTypeIdOffisiellKode(kverkRepo.finn(DokumentTypeId.class, DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL).getOffisiellKode());
        BehandlendeFagsystem behandlendeFagsystem = new BehandlendeFagsystem(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        behandlendeFagsystem = behandlendeFagsystem.medSaksnummer(saksnummer);

        when(vurderFagsystemTjenesteMock.vurderFagsystem(any(VurderFagsystem.class))).thenReturn(behandlendeFagsystem);

        BehandlendeFagsystemDto result = fordelRestTjeneste.vurderFagsystem(innDto);

        assertThat(result).isNotNull();
        assertThat(result.isManuellVurdering()).isTrue();
    }

    @Test
    public void skalReturnereFagsakinformasjonMedBehandlingTemaOgAktørId() {
        final ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID_MOR);
        scenario.medSaksnummer(new Saksnummer("1")).medSøknadHendelse().medFødselsDato(LocalDate.now());
        scenario.lagre(kodeverkRepository);
        FagsakInfomasjonDto result = fordelRestTjeneste.fagsak(new SaksnummerDto("1"));

        assertThat(result).isNotNull();
        assertThat(new AktørId(result.getAktørId())).isEqualTo(AKTØR_ID_MOR);
        assertThat(result.getBehandlingstemaOffisiellKode()).isEqualTo(kodeverkRepository.getKodeverkRepository().finn(BehandlingTema.class, BehandlingTema.FORELDREPENGER_FØDSEL).getOffisiellKode());
    }

}
