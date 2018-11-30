package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarTilleggsopplysningerDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarTilleggsopplysningerDto.class, adapter=AksjonspunktOppdaterer.class)
class AvklarTilleggsopplysningerOppdaterer implements AksjonspunktOppdaterer<AvklarTilleggsopplysningerDto> {

    @Override
    public OppdateringResultat oppdater(AvklarTilleggsopplysningerDto dto, Behandling behandling) {
        // skal ikke oppdater noe her
        return OppdateringResultat.utenOveropp();
    }
}
