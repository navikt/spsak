package no.nav.foreldrepenger.dokumentbestiller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Node;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FritekstVedtakDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;

public class FritekstBrevTest extends DokumentBestillerApplikasjonTjenesteImplFPOppsett {

    @Test
    public void forhåndsvisFritekstbrev() {
        // arrange
        Behandlingsresultat.builder()
            .medBehandlingResultatType(BehandlingResultatType.INNVILGET)
            .medVedtaksbrev(Vedtaksbrev.FRITEKST)
            .buildFor(behandling);

        BestillVedtakBrevDto bestillVedtakBrev = new BestillVedtakBrevDto(behandling.getId(), null);
        bestillVedtakBrev.setSkalBrukeOverstyrendeFritekstBrev(true);
        bestillVedtakBrev.setOverskrift("Overskrift");
        bestillVedtakBrev.setFritekstBrev("Fritekstbrev");

        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.FRITEKST_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.FRITEKST_DOK)).thenReturn(dokumentMalMock);
        mockHentDokumentData(new FritekstVedtakDokument("overskrift", "brødtekst"));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast(new byte[]{1, 1});
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor.forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture())).thenReturn(produserDokumentutkastResponse);

        // act
        byte[] forhandsvisFritekstbrev = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrev, (b -> false));

        // assert
        ProduserDokumentutkastRequest value = captor.getValue();
        Assert.assertNotNull(forhandsvisFritekstbrev);
        Assert.assertNotNull(value.getBrevdata());

        String brevXml = ((Node) value.getBrevdata()).getOwnerDocument().getFirstChild().getTextContent();
        Assert.assertTrue(brevXml.contains("Overskrift") && brevXml.contains("Fritekstbrev"));
    }
}
