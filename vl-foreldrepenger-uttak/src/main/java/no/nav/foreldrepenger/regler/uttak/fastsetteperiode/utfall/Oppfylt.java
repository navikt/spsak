package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;

public class Oppfylt {

    private Oppfylt() {
        // For å hindre instanser
    }

    /**
     * Opprette endenode for oppfylt periode.
     *
     * @param id sluttnode id.
     *
     * @param innvilgetÅrsak
     * @return periode utfall.
     */
    public static FastsettePeriodeUtfall opprett(String id, InnvilgetÅrsak innvilgetÅrsak, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .oppfylt(innvilgetÅrsak)
                .utbetal(utbetal)
                .medId(id)
                .create();
    }


    public static FastsettePeriodeUtfall opprettMedAvslåttGradering(String id, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingAvslagÅrsak, boolean utbetal) {
        return FastsettePeriodeUtfall.builder()
                .oppfylt(innvilgetÅrsak)
                .utbetal(utbetal)
                .medAvslåttGradering(graderingAvslagÅrsak)
                .medId(id)
                .create();
    }



}
