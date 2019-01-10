package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface AksjonspunktKode {

    @JsonIgnore
    String getKode();
}
