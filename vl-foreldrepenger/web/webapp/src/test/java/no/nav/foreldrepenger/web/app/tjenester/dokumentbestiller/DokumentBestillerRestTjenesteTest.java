package no.nav.foreldrepenger.web.app.tjenester.dokumentbestiller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;

public class DokumentBestillerRestTjenesteTest {
    private static final String BEHANDLING_ID = "129";
    private static final String FRITEKST = "Fritekst";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjenesteMock;

    private DokumentBestillerRestTjeneste dokumentBestillerRestTjeneste;

    @Before
    public void setUp() {
        byte[] pdf = new byte[]{0};
        when(dokumentBestillerApplikasjonTjenesteMock.forhandsvisDokument(Mockito.anyLong())).thenReturn(pdf);
        dokumentBestillerRestTjeneste = new DokumentBestillerRestTjeneste(dokumentBestillerApplikasjonTjenesteMock, null);
    }

    @Test
    public void forhandsvisVedtaksbrevTest() {
        // act
        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(Long.valueOf(BEHANDLING_ID), FRITEKST);
        bestillVedtakBrevDto.setSkalBrukeOverstyrendeFritekstBrev(false);
        Response response = dokumentBestillerRestTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrevDto);

        // verify
        verify(dokumentBestillerApplikasjonTjenesteMock).forhandsvisVedtaksbrev(Mockito.any(), Mockito.any());
        verify(dokumentBestillerApplikasjonTjenesteMock, never()).forhandsvisDokument(Mockito.anyLong());
        assertThat(response).isNotNull();
    }

}
