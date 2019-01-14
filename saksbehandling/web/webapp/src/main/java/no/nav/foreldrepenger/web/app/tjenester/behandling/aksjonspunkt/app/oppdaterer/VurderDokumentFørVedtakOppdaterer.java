package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereDokumentFørVedtakDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurdereDokumentFørVedtakDto.class, adapter=AksjonspunktOppdaterer.class)
class VurderDokumentFørVedtakOppdaterer implements AksjonspunktOppdaterer<VurdereDokumentFørVedtakDto> {

    @Override
    public OppdateringResultat oppdater(VurdereDokumentFørVedtakDto dto, Behandling behandling) {
        return OppdateringResultat.utenOveropp();
    }
}
