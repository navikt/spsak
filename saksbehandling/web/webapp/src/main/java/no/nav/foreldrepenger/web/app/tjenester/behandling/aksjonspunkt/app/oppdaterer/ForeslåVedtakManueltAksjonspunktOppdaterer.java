package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakManueltAksjonspuntDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = ForeslaVedtakManueltAksjonspuntDto.class, adapter=AksjonspunktOppdaterer.class)
class ForeslåVedtakManueltAksjonspunktOppdaterer implements AksjonspunktOppdaterer<ForeslaVedtakManueltAksjonspuntDto> {

    @Override
    public OppdateringResultat oppdater(ForeslaVedtakManueltAksjonspuntDto dto, Behandling behandling) {
        return OppdateringResultat.utenOveropp();
    }
}
