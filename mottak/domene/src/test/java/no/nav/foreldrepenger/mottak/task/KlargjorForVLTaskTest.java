package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class KlargjorForVLTaskTest {


    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "234567";

    private KlargjorForVLTask task;

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    @Mock
    private KlargjørForVLTjeneste klargjørForVLTjeneste;

    @Mock
    private DokumentRepository dokumentRepository;

    @Mock
    private KodeverkRepository kodeverkRepository;

    private UUID forsendelseId;

    @Before
    public void setup() {
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        klargjørForVLTjeneste = mock(KlargjørForVLTjeneste.class);
        dokumentRepository = mock(DokumentRepository.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        forsendelseId = UUID.randomUUID();
        task = new KlargjorForVLTask(prosessTaskRepositoryMock, kodeverkRepository, klargjørForVLTjeneste, dokumentRepository);
    }

    @Test
    public void test_utfør_mangler_precondition() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(KlargjorForVLTask.TASKNAME));
        Exception fangetFeil = null;
        try {
            toTaskWithPrecondition(data);
        } catch (Exception ex) {
            fangetFeil = ex;
        }
        assertThat(fangetFeil).isInstanceOf(TekniskException.class);
        assertThat(((TekniskException) fangetFeil).getFeil().getKode()).isEqualTo("FP-941984");
    }

    @Test
    public void test_utfor_klargjor_uten_xml_i_payload() {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(KlargjorForVLTask.TASKNAME));
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("pay the load");
        data.setForsendelseId(UUID.randomUUID());

        toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void test_utfor_klargjor_med_alle_nodvendige_data() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(KlargjorForVLTask.TASKNAME));
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(UUID.randomUUID());

        toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void test_oppdater_metadata_hvis_forsendelseId_er_satt() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(KlargjorForVLTask.TASKNAME));
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(forsendelseId);

        toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(),any(), any());
        verify(dokumentRepository).oppdaterForsendelseMetadata(forsendelseId, ARKIV_ID, SAKSNUMMER, ForsendelseStatus.FPSAK);
    }

    private MottakMeldingDataWrapper toTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }
}
