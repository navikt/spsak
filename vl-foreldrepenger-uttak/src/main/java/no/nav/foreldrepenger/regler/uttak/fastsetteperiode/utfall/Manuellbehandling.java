package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;

public class Manuellbehandling {

    private Manuellbehandling() {
        // For å hindre instanser
    }

    public static FastsettePeriodeUtfall opprett(String id, IkkeOppfyltÅrsak ikkeOppfyltÅrsak, Manuellbehandlingårsak manuellbehandlingårsak, boolean trekkDagerFraSaldo, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .manuellBehandling(ikkeOppfyltÅrsak, manuellbehandlingårsak)
                .utbetal(utbetal)
                .medTrekkDagerFraSaldo(trekkDagerFraSaldo)
                .medId(id)
                .create();
    }

}
