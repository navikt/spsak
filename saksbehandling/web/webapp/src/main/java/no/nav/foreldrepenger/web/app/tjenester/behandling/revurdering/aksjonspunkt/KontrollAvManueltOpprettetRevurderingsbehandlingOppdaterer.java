package no.nav.foreldrepenger.web.app.tjenester.behandling.revurdering.aksjonspunkt;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;


@ApplicationScoped
@DtoTilServiceAdapter(dto = KontrollAvManueltOpprettetRevurderingsbehandlingDto.class, adapter = AksjonspunktOppdaterer.class)
class KontrollAvManueltOpprettetRevurderingsbehandlingOppdaterer implements AksjonspunktOppdaterer<KontrollAvManueltOpprettetRevurderingsbehandlingDto> {

    @Override
    public OppdateringResultat oppdater(KontrollAvManueltOpprettetRevurderingsbehandlingDto dto, Behandling behandling) {
        return OppdateringResultat.utenOveropp();
    }

}


