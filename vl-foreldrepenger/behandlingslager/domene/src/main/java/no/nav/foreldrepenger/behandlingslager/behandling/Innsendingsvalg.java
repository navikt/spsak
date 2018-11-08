package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Innsendingsvalg")
@DiscriminatorValue(Innsendingsvalg.DISCRIMINATOR)
public class Innsendingsvalg extends Kodeliste {

    public static final String DISCRIMINATOR = "INNSENDINGSVALG";
    public static final Innsendingsvalg LASTET_OPP = new Innsendingsvalg("LASTET_OPP"); //$NON-NLS-1$
    public static final Innsendingsvalg SEND_SENERE = new Innsendingsvalg("SEND_SENERE"); //$NON-NLS-1$
    public static final Innsendingsvalg SENDES_IKKE = new Innsendingsvalg("SENDES_IKKE"); //$NON-NLS-1$
    public static final Innsendingsvalg VEDLEGG_SENDES_AV_ANDRE = new Innsendingsvalg("VEDLEGG_SENDES_AV_ANDRE"); //$NON-NLS-1$
    public static final Innsendingsvalg IKKE_VALGT = new Innsendingsvalg("IKKE_VALGT"); //$NON-NLS-1$
    public static final Innsendingsvalg VEDLEGG_ALLEREDE_SENDT = new Innsendingsvalg("VEDLEGG_ALLEREDE_SENDT"); //$NON-NLS-1$

    Innsendingsvalg() {
        // Hibernate trenger en
    }

    private Innsendingsvalg(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
