package no.nav.foreldrepenger.dokumentbestiller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.w3c.dom.Node;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.AvslagForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;

public class AvslagsbrevFPTest extends DokumentBestillerApplikasjonTjenesteImplFPOppsett {
    @Mock
    private Sats mockSats;

    @Test
    public void forhåndsvisAvslagsbrevFPTest() {
        // arrange
        when(beregningsRepository.finnEksaktSats(any(), any())).thenReturn(mockSats);
        when(mockSats.getVerdi()).thenReturn(100000L);
        Avslagsårsak avslagsårsak = mock(Avslagsårsak.class);
        when(avslagsårsak.getKode()).thenReturn(Avslagsårsak.SØKER_ER_IKKE_BOSATT.getKode());
        when(avslagsårsak.getLovReferanse(any())).thenReturn("66");
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT)
            .medAvslagsårsak(avslagsårsak)
            .buildFor(behandling);

        mockHentDokumentData(new AvslagForeldrepengerDokument(brevParametere));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast(new byte[]{1, 1});
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor.forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture())).thenReturn(produserDokumentutkastResponse);

        // act
        BestillVedtakBrevDto dto = new BestillVedtakBrevDto(behandling.getId(), null);
        dto.setSkalBrukeOverstyrendeFritekstBrev(false);
        byte[] forhandsvisAvslagsbrev = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(dto, (b -> false));

        // assert
        assertAvslagsbrev(captor, forhandsvisAvslagsbrev);
    }

    private void assertAvslagsbrev(ArgumentCaptor<ProduserDokumentutkastRequest> captor, byte[] forhandsvisAvslagsbrev) {
        ProduserDokumentutkastRequest value = captor.getValue();
        Assert.assertNotNull(forhandsvisAvslagsbrev);
        Assert.assertNotNull(value.getBrevdata());
        document = ((Node) value.getBrevdata()).getOwnerDocument();
        assertDokumentFelles();
        checkDokumentValue("behandlingsType", "FOERSTEGANGSBEHANDLING");
        checkDokumentValue("klageFristUker", "6");
    }
}
