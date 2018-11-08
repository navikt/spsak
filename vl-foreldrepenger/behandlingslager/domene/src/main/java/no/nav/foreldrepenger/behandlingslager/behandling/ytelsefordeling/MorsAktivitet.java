package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MorsAktivitet")
@DiscriminatorValue(MorsAktivitet.DISCRIMINATOR)
public class MorsAktivitet extends Kodeliste {

    public static final String DISCRIMINATOR = "MORS_AKTIVITET";

    public static final MorsAktivitet UDEFINERT = new MorsAktivitet("-"); //$NON-NLS-1$

    public static final MorsAktivitet ARBEID = new MorsAktivitet("ARBEID"); //$NON-NLS-1$
    public static final MorsAktivitet UTDANNING = new MorsAktivitet("UTDANNING"); //$NON-NLS-1$
    public static final MorsAktivitet SAMTIDIGUTTAK = new MorsAktivitet("SAMTIDIGUTTAK"); //$NON-NLS-1$
    public static final MorsAktivitet KVALPROG = new MorsAktivitet("KVALPROG"); //$NON-NLS-1$
    public static final MorsAktivitet INTROPROG = new MorsAktivitet("INTROPROG"); //$NON-NLS-1$
    public static final MorsAktivitet TRENGER_HJELP = new MorsAktivitet("TRENGER_HJELP"); //$NON-NLS-1$
    public static final MorsAktivitet INNLAGT = new MorsAktivitet("INNLAGT"); //$NON-NLS-1$
    public static final MorsAktivitet ARBEID_OG_UTDANNING = new MorsAktivitet("ARBEID_OG_UTDANNING"); //$NON-NLS-1$
    public static final MorsAktivitet UFØRE = new MorsAktivitet("UFØRE"); //$NON-NLS-1$

    MorsAktivitet() {
        // Hibernate trenger en
    }

    private MorsAktivitet(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
