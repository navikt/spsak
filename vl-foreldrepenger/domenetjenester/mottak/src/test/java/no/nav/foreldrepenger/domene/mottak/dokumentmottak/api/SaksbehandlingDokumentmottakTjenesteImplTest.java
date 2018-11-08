package no.nav.foreldrepenger.domene.mottak.dokumentmottak.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class SaksbehandlingDokumentmottakTjenesteImplTest {

    private static final Long FAGSAK_ID = 1L;
    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("2");
    private static final DokumentTypeId DOKUMENTTYPE = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
    private static final DokumentKategori DOKUMENTKATEGORI = DokumentKategori.SØKNAD;
    private static final LocalDate FORSENDELSE_MOTTATT = LocalDate.now();
    private static final Boolean ELEKTRONISK_SØKNAD = Boolean.TRUE;
    private static final String PAYLOAD_XML = "<test></test>";

    private BehandlingTema behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private ProsessTaskRepository prosessTaskRepository;
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste;

    @Before
    public void before() {
        prosessTaskRepository = mock(ProsessTaskRepository.class);
        MottatteDokumentTjeneste mottatteDokumentTjeneste = mock(MottatteDokumentTjeneste.class);
        saksbehandlingDokumentmottakTjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository, mottatteDokumentTjeneste);
    }

    @Test
    public void skal_ta_imot_ankommet_saksdokument_og_opprette_prosesstask() {
        // Arrange
        InngåendeSaksdokument saksdokument = InngåendeSaksdokument.builder()
                .medFagsakId(FAGSAK_ID)
                .medJournalpostId(JOURNALPOST_ID)
                .medBehandlingTema(behandlingTema)
                .medDokumentTypeId(DOKUMENTTYPE)
                .medDokumentKategori(DOKUMENTKATEGORI)
                .medForsendelseMottatt(FORSENDELSE_MOTTATT)
                .medElektroniskSøknad(ELEKTRONISK_SØKNAD)
                .medPayloadXml(PAYLOAD_XML)
                .build();
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);

        // Act
        saksbehandlingDokumentmottakTjeneste.dokumentAnkommet(saksdokument);

        // Assert
        verify(prosessTaskRepository).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        assertThat(prosessTaskData.getTaskType()).isEqualTo(HåndterMottattDokumentTaskProperties.TASKTYPE);
        assertThat(prosessTaskData.getFagsakId()).isEqualTo(FAGSAK_ID);
        assertThat(prosessTaskData.getProperties().getProperty(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY)).isEqualTo(behandlingTema.getOffisiellKode());
    }

    @Test
    public void skal_støtte_at_journalpostId_er_null() {
        // Arrange
        InngåendeSaksdokument saksdokument = InngåendeSaksdokument.builder()
                .medFagsakId(FAGSAK_ID)
                .medJournalpostId(null)
                .medBehandlingTema(behandlingTema)
                .medDokumentTypeId(DOKUMENTTYPE)
                .medDokumentKategori(DOKUMENTKATEGORI)
                .medForsendelseMottatt(FORSENDELSE_MOTTATT)
                .medElektroniskSøknad(ELEKTRONISK_SØKNAD)
                .medPayloadXml(PAYLOAD_XML)
                .build();
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);

        // Act
        saksbehandlingDokumentmottakTjeneste.dokumentAnkommet(saksdokument);

        // Assert
        verify(prosessTaskRepository).lagre(captor.capture());
    }
}
