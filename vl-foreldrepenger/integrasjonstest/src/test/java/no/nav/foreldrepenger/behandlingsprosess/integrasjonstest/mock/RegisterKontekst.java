package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MedlTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MeldekortTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;

/**
 * Midlertidig løsning for å utfase gammelt mock-paradigme som ikke gir testene mulighet
 * til å spesifirere ønskede testsett i registre
 */
@ApplicationScoped
public class RegisterKontekst {

    private boolean initalisert;

    @Inject
    private RegisterKontekst() {
        // For CDI
    }

    public boolean erInitalisert() {
        return initalisert;
    }

    public void intialiser() {
        initalisert = true;
    }

    public void nullstill() {
        initalisert = false;
        TpsTestSett.nullstill();
        ArbeidsforholdTestSett.nullstill();
        InntektTestSett.nullstill();
        MedlTestSett.nullstill();
        MeldekortTestSett.nullstill();
        InfotrygdVedtakTestSett.nullstill();
    }
}
