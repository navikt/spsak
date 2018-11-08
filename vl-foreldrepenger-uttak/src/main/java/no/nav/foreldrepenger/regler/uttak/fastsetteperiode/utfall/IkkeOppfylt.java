package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;

public class IkkeOppfylt {

    private IkkeOppfylt() {
        // For å hindre instanser
    }

    /**
     * Opprette endenode for ikke oppfylt periode.
     *
     * @param id sluttnode id.
     * @param årsak årsak til at periode ble ikke oppfylt.
     *
     * @return periode utfall.
     */
    public static FastsettePeriodeUtfall opprett(String id, IkkeOppfyltÅrsak årsak, boolean trekkDagerFraSaldo, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .ikkeOppfylt(årsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDagerFraSaldo)
                .medId(id)
                .create();
    }

}
