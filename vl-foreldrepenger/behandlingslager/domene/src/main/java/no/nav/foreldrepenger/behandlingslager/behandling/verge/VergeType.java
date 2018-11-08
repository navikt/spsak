package no.nav.foreldrepenger.behandlingslager.behandling.verge;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "VergeType")
@DiscriminatorValue(VergeType.DISCRIMINATOR)
public class VergeType extends Kodeliste {

    public static final String DISCRIMINATOR = "VERGE_TYPE";

    public static final VergeType BARN      = new VergeType("BARN"); //$NON-NLS-1$   Verge for barn under 18 år
    public static final VergeType FBARN     = new VergeType("FBARN"); //$NON-NLS-1$  Verge for foreldreløst barn under 18 år
    public static final VergeType VOKSEN    = new VergeType("VOKSEN"); //$NON-NLS-1$ Verge for voksen
    public static final VergeType UDEFINERT = new VergeType("-"); //$NON-NLS-1$

    public VergeType() {
    }

    public VergeType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
