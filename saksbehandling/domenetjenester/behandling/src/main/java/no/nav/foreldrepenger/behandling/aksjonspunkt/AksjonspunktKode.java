package no.nav.foreldrepenger.behandling.aksjonspunkt;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface AksjonspunktKode {

    @JsonIgnore
    String getKode();
}
