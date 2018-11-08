package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "OpplysningsKilde")
@DiscriminatorValue(OpplysningsKilde.DISCRIMINATOR)
public class OpplysningsKilde extends Kodeliste {
    public static final String DISCRIMINATOR = "OPPLYSNINGSKILDE";

    public static final OpplysningsKilde TPS     = new OpplysningsKilde("TPS");  //$NON-NLS-1$
    public static final OpplysningsKilde SAKSBEH = new OpplysningsKilde("SAKSBEH");  //$NON-NLS-1$
    public static final OpplysningsKilde UDEFINERT = new OpplysningsKilde("UDEFINERT");  //$NON-NLS-1$


    public OpplysningsKilde() {
    }

    public OpplysningsKilde(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
