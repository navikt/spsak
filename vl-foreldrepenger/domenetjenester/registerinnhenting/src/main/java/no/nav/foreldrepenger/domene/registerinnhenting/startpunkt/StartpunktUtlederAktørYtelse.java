package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@Dependent
class StartpunktUtlederAktørYtelse {

    @Inject
    StartpunktUtlederAktørYtelse() {
        // For CDI
    }

    StartpunktType utledStartpunkt() {
        return StartpunktType.SØKERS_RELASJON_TIL_BARNET;
    }
}
