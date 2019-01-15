package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.VurdereAnnenYteleseFørVedtakDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurdereAnnenYteleseFørVedtakDto.class, adapter=AksjonspunktOppdaterer.class)
class VurderAnnenYtelseFørVedtakOppdaterer implements AksjonspunktOppdaterer<VurdereAnnenYteleseFørVedtakDto> {

    @Override
    public OppdateringResultat oppdater(VurdereAnnenYteleseFørVedtakDto dto, Behandling behandling) {
        return OppdateringResultat.utenOveropp();
    }
}
