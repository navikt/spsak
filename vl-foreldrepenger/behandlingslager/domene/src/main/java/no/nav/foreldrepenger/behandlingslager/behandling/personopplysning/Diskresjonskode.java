package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "Diskresjonskode")
@DiscriminatorValue(Diskresjonskode.DISCRIMINATOR)
public class Diskresjonskode extends Kodeliste {
    public static final String DISCRIMINATOR = "DISKRESJONSKODE";

    public static final Diskresjonskode KLIENT_ADRESSE  = new Diskresjonskode("KLIE"); //$NON-NLS-1$
    public static final Diskresjonskode MILITÆR         = new Diskresjonskode("MILI"); //$NON-NLS-1$
    public static final Diskresjonskode PENDLER         = new Diskresjonskode("PEND"); //$NON-NLS-1$
    public static final Diskresjonskode KODE7           = new Diskresjonskode("SPFO"); //$NON-NLS-1$
    public static final Diskresjonskode KODE6           = new Diskresjonskode("SPSF"); //$NON-NLS-1$
    public static final Diskresjonskode SVALBARD        = new Diskresjonskode("SVAL"); //$NON-NLS-1$
    public static final Diskresjonskode UTEN_FAST_BO    = new Diskresjonskode("UFB" ); //$NON-NLS-1$
    public static final Diskresjonskode UTENRIKS_TJENST = new Diskresjonskode("URIK"); //$NON-NLS-1$
    public static final Diskresjonskode UDEFINERT       = new Diskresjonskode("UDEF"); //$NON-NLS-1$

    public Diskresjonskode() {
    }

    public Diskresjonskode(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
