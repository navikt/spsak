package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import no.nav.foreldrepenger.behandlingslager.behandling.Innsendingsvalg;

public interface SøknadVedlegg {

    String getSkjemanummer();

    String getTilleggsinfo();

    boolean isErPåkrevdISøknadsdialog();

    Innsendingsvalg getInnsendingsvalg();

}
