package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

// TODO: Er et tynt subsett avRelasjonRolleType, men kodene er i bruk i SøknadXML så kan ikke bare slettes.
@Entity(name = "ForeldreType")
@DiscriminatorValue(ForeldreType.DISCRIMINATOR)
public class ForeldreType extends Kodeliste {

    public static final String DISCRIMINATOR = "FORELDRE_TYPE";
    public static final ForeldreType MOR = new ForeldreType("MOR"); //$NON-NLS-1$
    public static final ForeldreType FAR = new ForeldreType("FAR"); //$NON-NLS-1$
    public static final ForeldreType MEDMOR = new ForeldreType("MEDMOR"); //$NON-NLS-1$

    public static final ForeldreType UDEFINERT = new ForeldreType("-"); //$NON-NLS-1$

    ForeldreType() {
        // Hibernate trenger en
    }

    private ForeldreType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
