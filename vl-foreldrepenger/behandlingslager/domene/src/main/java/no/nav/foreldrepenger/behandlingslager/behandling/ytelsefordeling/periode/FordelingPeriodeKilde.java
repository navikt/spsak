package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FordelingPeriodeKilde")
@DiscriminatorValue(FordelingPeriodeKilde.DISCRIMINATOR)
public class FordelingPeriodeKilde extends Kodeliste {

    public static final String DISCRIMINATOR = "FORDELING_PERIODE_KILDE";

    public static final FordelingPeriodeKilde SØKNAD = new FordelingPeriodeKilde("SØKNAD"); //$NON-NLS-1$
    public static final FordelingPeriodeKilde TIDLIGERE_VEDTAK = new FordelingPeriodeKilde("TIDLIGERE_VEDTAK"); //$NON-NLS-1$

    FordelingPeriodeKilde() {
        // For hibernate
    }

    public FordelingPeriodeKilde(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
