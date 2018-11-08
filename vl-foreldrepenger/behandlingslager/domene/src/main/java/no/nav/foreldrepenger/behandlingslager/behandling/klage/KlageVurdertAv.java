package no.nav.foreldrepenger.behandlingslager.behandling.klage;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "KlageVurdertAv")
@DiscriminatorValue(KlageVurdertAv.DISCRIMINATOR)
public class KlageVurdertAv extends Kodeliste {

    public static final String DISCRIMINATOR = "KLAGE_VURDERT_AV";

    public static final KlageVurdertAv NFP = new KlageVurdertAv("NFP"); //$NON-NLS-1$
    public static final KlageVurdertAv NK = new KlageVurdertAv("NK"); //$NON-NLS-1$

    private KlageVurdertAv() {
        // for hibernate
    }

    private KlageVurdertAv(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
