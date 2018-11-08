package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.task;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class DokumentBestillerTaskTest {

    @Mock
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    private ProsessTaskData prosessTaskData;

    private DokumentBestillerTask dokumentBestillerTask;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        dokumentBestillerTask  = new DokumentBestillerTask(dokumentBestillerApplikasjonTjeneste);
        prosessTaskData = new ProsessTaskData(DokumentBestillerTaskProperties.TASKTYPE);
    }

    @Test
    public void testDokumentBestillerTask() throws Exception {
        final Long dokumentDataId = 10L;
        final HistorikkAktør aktor = HistorikkAktør.SAKSBEHANDLER;
        final String dokumentBegrunnelse = null;

        // Arrange
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.DOKUMENT_DATA_ID_KEY, String.valueOf(dokumentDataId));
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.HISTORIKK_AKTØR_KEY, aktor.getKode());
        prosessTaskData.setProperty(DokumentBestillerTaskProperties.DOKUMENT_BEGRUNNELSE_ID_KEY, dokumentBegrunnelse);

        // Act
        dokumentBestillerTask.doTask(prosessTaskData);

        // Assert
        verify(dokumentBestillerApplikasjonTjeneste).produserDokument(eq(dokumentDataId), eq(aktor), eq(dokumentBegrunnelse));
    }
}
