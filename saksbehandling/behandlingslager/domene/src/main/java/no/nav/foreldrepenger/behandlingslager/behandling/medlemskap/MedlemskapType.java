package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapType")
@DiscriminatorValue(MedlemskapType.DISCRIMINATOR)
public class MedlemskapType extends Kodeliste {
    public static final String DISCRIMINATOR = "MEDLEMSKAP_TYPE";
    public static final MedlemskapType ENDELIG = new MedlemskapType("ENDELIG"); //$NON-NLS-1$
    public static final MedlemskapType FORELOPIG = new MedlemskapType("FORELOPIG"); //$NON-NLS-1$
    public static final MedlemskapType UNDER_AVKLARING = new MedlemskapType("AVKLARES"); //$NON-NLS-1$
    
    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final MedlemskapType UDEFINERT = new MedlemskapType("-"); //$NON-NLS-1$

    MedlemskapType() {
        // Hibernate trenger en
    }

    private MedlemskapType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
