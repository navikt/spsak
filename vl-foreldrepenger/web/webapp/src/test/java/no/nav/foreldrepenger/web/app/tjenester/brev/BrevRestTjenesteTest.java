package no.nav.foreldrepenger.web.app.tjenester.brev;

import static no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType.INNHENT_DOK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BrevmalDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;

public class BrevRestTjenesteTest {

    private BrevRestTjeneste brevRestTjeneste;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjenesteMock;

    @Before
    public void setUp() {
        dokumentBestillerApplikasjonTjenesteMock = mock(DokumentBestillerApplikasjonTjeneste.class);
        brevRestTjeneste = new BrevRestTjeneste(dokumentBestillerApplikasjonTjenesteMock);
    }

    @Test
    public void returnererForhandsvisning() {
        long behandlingId = 1L;
        when(dokumentBestillerApplikasjonTjenesteMock.hentForhåndsvisningDokument(any())).thenReturn("Brev".getBytes());

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandlingId, INNHENT_DOK, "Fritekst");
        Response response = brevRestTjeneste.hentForhåndsvisningDokument(bestillBrevDto);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);

        verify(dokumentBestillerApplikasjonTjenesteMock).hentForhåndsvisningDokument(bestillBrevDto);
    }

    @Test
    public void bestillerDokument() {
        long behandlingId = 2L;

        BestillBrevDto bestillBrevDto = new BestillBrevDto(behandlingId, INNHENT_DOK, "Dette er en fritekst");
        brevRestTjeneste.bestillDokument(bestillBrevDto);

        verify(dokumentBestillerApplikasjonTjenesteMock).bestillDokument(eq(bestillBrevDto), eq(HistorikkAktør.SAKSBEHANDLER));
    }

    @Test
    public void henterMottakere() {
        long behandlingId = 1L;
        when(dokumentBestillerApplikasjonTjenesteMock.hentMottakere(anyLong())).thenReturn(Collections.singletonList("Søker"));

        List<String> mottakere = brevRestTjeneste.hentMottakere(new BehandlingIdDto(behandlingId));

        verify(dokumentBestillerApplikasjonTjenesteMock).hentMottakere(anyLong());

        assertThat(mottakere.size()).isEqualTo(1);
        assertThat(mottakere.get(0)).isEqualTo("Søker");
    }

    @Test
    public void henterBrevmaler() {
        long behandlingId = 1L;
        when(dokumentBestillerApplikasjonTjenesteMock.hentBrevmalerFor(behandlingId))
            .thenReturn(Collections.singletonList(new BrevmalDto("INNHEN", "Innhent dokumentasjon", DokumentMalRestriksjon.INGEN, true)));

        List<BrevmalDto> brevmaler = brevRestTjeneste.hentMaler(new BehandlingIdDto(behandlingId));

        verify(dokumentBestillerApplikasjonTjenesteMock).hentBrevmalerFor(behandlingId);

        assertThat(brevmaler.size()).isEqualTo(1);
        assertThat(brevmaler.get(0).getNavn()).isEqualTo("Innhent dokumentasjon");
    }

    @Test
    public void harSendtVarselOmRevurdering() {
        when(dokumentBestillerApplikasjonTjenesteMock.erDokumentProdusert(any(), any())).thenReturn(true);

        Boolean harSendt = brevRestTjeneste.harSendtVarselOmRevurdering(new BehandlingIdDto(1L));

        verify(dokumentBestillerApplikasjonTjenesteMock).erDokumentProdusert(any(), any());

        assertThat(harSendt).isEqualTo(true);
    }
}
