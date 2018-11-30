package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

class FellesStartpunktUtlederLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(FellesStartpunktUtlederLogger.class);

    FellesStartpunktUtlederLogger() {
        // For CDI
    }

    static void loggEndringSomFørteTilStartpunkt(String klasseNavn, StartpunktType startpunkt, String endring, Long id1, Long id2) {
        LOGGER.info("{}: Setter startpunkt til {}. Og har endring i {}. GrunnlagId1: {}, grunnlagId2: {}", klasseNavn, startpunkt.getKode(), endring, id1, id2);// NOSONAR //$NON-NLS-1$
    }
}
