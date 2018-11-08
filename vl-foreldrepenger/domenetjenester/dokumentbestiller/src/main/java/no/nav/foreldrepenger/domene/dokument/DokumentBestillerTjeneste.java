package no.nav.foreldrepenger.domene.dokument;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface DokumentBestillerTjeneste {

    void aksjonspunktVarselRevurdering(Behandling behandling, VarselRevurderingAksjonspunktDto adapter);

    void aksjonspunktKlageVurdering(Behandling behandling, KlageVurderingAksjonspunktDto adapter);
}
