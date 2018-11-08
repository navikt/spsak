package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@Dependent
class StartpunktUtlederAktørArbeid {

    @Inject
    StartpunktUtlederAktørArbeid() {
        // For CDI
    }

    StartpunktType utledStartpunkt() {
        return StartpunktType.OPPTJENING;
    }
}
