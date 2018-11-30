package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.vedtak.util.Objects;

/**
 * Brukes for intern håndtering av flyt på et steg. Inneholder kode for stegets nye status. Hvis status er fremoverføring,
 * er også steget det skal fremoverføres til inkludert.
 */
public class BehandlingStegProsesseringResultat {
    private final BehandlingStegStatus nyStegStatus;
    private final TransisjonIdentifikator transisjon;

    private BehandlingStegProsesseringResultat(BehandlingStegStatus nyStegStatus, TransisjonIdentifikator transisjon) {
        this.nyStegStatus = nyStegStatus;
        this.transisjon = transisjon;
    }

    static BehandlingStegProsesseringResultat medMuligTransisjon(BehandlingStegStatus nyStegStatus, TransisjonIdentifikator transisjon) {
        return new BehandlingStegProsesseringResultat(nyStegStatus, transisjon);
    }

    static BehandlingStegProsesseringResultat utenOverhopp(BehandlingStegStatus nyStegStatus) {
        return new BehandlingStegProsesseringResultat(nyStegStatus, FellesTransisjoner.UTFØRT);
    }

    public TransisjonIdentifikator getTransisjon() {
        return transisjon;
    }

    public BehandlingStegStatus getNyStegStatus() {
        return nyStegStatus;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<nyStegStatus=" + nyStegStatus + ", transisjon=" + transisjon + ">"; // NOSONAR //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    static void validerKombinasjon(BehandlingStegStatus nyStegStatus, BehandlingStegType målsteg) {
        Objects.check(nyStegStatus != null, "resultat må være satt"); //$NON-NLS-1$
        if (BehandlingStegStatus.FREMOVERFØRT.equals(nyStegStatus)) {
            Objects.check(målsteg != null, "målsteg må være satt ved fremoverføring"); //$NON-NLS-1$
        } else {
            Objects.check(målsteg == null, "målsteg skal ikke være satt ved resultat " + nyStegStatus.getKode()); //$NON-NLS-1$
        }
    }
}
