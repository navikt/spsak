package no.nav.foreldrepenger.dokumentbestiller;

import static org.mockito.ArgumentMatchers.any;
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
import no.nav.foreldrepenger.dokumentbestiller.api.mal.OpphørDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;

public class OpphørbrevTest extends DokumentBestillerApplikasjonTjenesteImplFPOppsett {
    @Mock
    private Sats mockSats;
    @Mock
    private Avslagsårsak avslagsårsak;

    @Test
    public void forhåndsvisAvslagsbrevFPTest() {
        when(beregningsRepository.finnEksaktSats(any(), any())).thenReturn(mockSats);
        when(mockSats.getVerdi()).thenReturn(100000L);
        when(avslagsårsak.getKode()).thenReturn(Avslagsårsak.SØKER_ER_IKKE_BOSATT.getKode());
        when(avslagsårsak.getLovReferanse(any())).thenReturn("66");
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.OPPHØR)
            .medAvslagsårsak(avslagsårsak)
            .buildFor(behandling);

        mockHentDokumentData(new OpphørDokument(brevParametere));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        produserDokumentutkastResponse.setDokumentutkast(new byte[]{1, 1});
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor.forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture())).thenReturn(produserDokumentutkastResponse);

        // act
        BestillVedtakBrevDto dto = new BestillVedtakBrevDto(behandling.getId(), null);
        dto.setSkalBrukeOverstyrendeFritekstBrev(false);
        byte[] forhandsvisAvslagsbrev = dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(dto, (b -> false));

        // assert
        assertOpphørsbrev(captor, forhandsvisAvslagsbrev);
    }

    private void assertOpphørsbrev(ArgumentCaptor<ProduserDokumentutkastRequest> captor, byte[] forhandsvisBrev) {
        ProduserDokumentutkastRequest value = captor.getValue();
        Assert.assertNotNull(forhandsvisBrev);
        Assert.assertNotNull(value.getBrevdata());
        document = ((Node) value.getBrevdata()).getOwnerDocument();
        assertDokumentFelles();
        checkDokumentValue("behandlingsType", "FOERSTEGANGSBEHANDLING");
        checkDokumentValue("klageFristUker", "6");
    }
}
