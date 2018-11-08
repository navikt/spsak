package no.nav.foreldrepenger.domene.registrerer;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface DokumentRegistrererTjeneste {

    void aksjonspunktManuellRegistrering(Behandling behandling, ManuellRegistreringAksjonspunktDto adapter);


}
