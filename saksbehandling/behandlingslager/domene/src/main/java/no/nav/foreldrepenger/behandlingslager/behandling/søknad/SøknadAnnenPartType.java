
package no.nav.foreldrepenger.behandlingslager.behandling.søknad;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "SøknadAnnenPartType")
@DiscriminatorValue(SøknadAnnenPartType.DISCRIMINATOR)
public class SøknadAnnenPartType extends Kodeliste {

    public static final String DISCRIMINATOR = "SOEKNAD_ANNEN_PART";
    // TODO (MAUR): disse kodeverdier er midlertidig satt inn for testformål. Ekte kodeverdier ikke kjent enda.
    public static final SøknadAnnenPartType MOR = new SøknadAnnenPartType("MOR"); //$NON-NLS-1$
    public static final SøknadAnnenPartType MEDMOR = new SøknadAnnenPartType("MEDMOR"); //$NON-NLS-1$
    public static final SøknadAnnenPartType FAR = new SøknadAnnenPartType("FAR"); //$NON-NLS-1$
    public static final SøknadAnnenPartType MEDFAR = new SøknadAnnenPartType("MEDFAR"); //$NON-NLS-1$

    public static final SøknadAnnenPartType UDEFINERT = new SøknadAnnenPartType("-"); //$NON-NLS-1$

    SøknadAnnenPartType() {
        // Hibernate trenger en
    }

    private SøknadAnnenPartType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
