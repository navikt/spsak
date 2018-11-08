package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

class RevurderingFremoverhoppTransisjon extends FremoverhoppTransisjon {


    RevurderingFremoverhoppTransisjon(BehandlingStegType målsteg) {
        super("revurdering-fremoverhopp-til-" + målsteg.getKode(), målsteg);
    }

}
