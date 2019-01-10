package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktKode;

/**
 * Aksjonspunkt Dto som lar seg avslå (der vilkår kan settes Ok/Ikke OK)
 *
 */
public interface AvslagbartAksjonspunktDto extends AksjonspunktKode {

    Boolean getErVilkarOk();

    String getAvslagskode();

    String getBegrunnelse();
}