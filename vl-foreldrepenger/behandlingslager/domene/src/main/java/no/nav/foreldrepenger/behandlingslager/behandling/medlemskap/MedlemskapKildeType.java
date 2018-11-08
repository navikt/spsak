package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapKildeType")
@DiscriminatorValue(MedlemskapKildeType.DISCRIMINATOR)
public class MedlemskapKildeType extends Kodeliste {

    public static final String DISCRIMINATOR = "MEDLEMSKAP_KILDE";
    public static final MedlemskapKildeType E500 = new MedlemskapKildeType("E500"); //$NON-NLS-1$
    public static final MedlemskapKildeType INFOTR = new MedlemskapKildeType("INFOTR"); //$NON-NLS-1$
    public static final MedlemskapKildeType AVGSYS = new MedlemskapKildeType("AVGSYS"); //$NON-NLS-1$
    public static final MedlemskapKildeType APPBRK = new MedlemskapKildeType("APPBRK"); //$NON-NLS-1$
    public static final MedlemskapKildeType PP01 = new MedlemskapKildeType("PP01"); //$NON-NLS-1$
    public static final MedlemskapKildeType FS22 = new MedlemskapKildeType("FS22"); //$NON-NLS-1$
    public static final MedlemskapKildeType MEDL = new MedlemskapKildeType("MEDL"); //$NON-NLS-1$
    public static final MedlemskapKildeType TPS = new MedlemskapKildeType("TPS"); //$NON-NLS-1$
    public static final MedlemskapKildeType TP = new MedlemskapKildeType("TP"); //$NON-NLS-1$
    public static final MedlemskapKildeType LAANEKASSEN = new MedlemskapKildeType("LAANEKASSEN"); //$NON-NLS-1$
    public static final MedlemskapKildeType ANNEN = new MedlemskapKildeType("ANNEN"); //$NON-NLS-1$

    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final MedlemskapKildeType UDEFINERT = new MedlemskapKildeType("-"); //$NON-NLS-1$

    MedlemskapKildeType() {
        // Hibernate trenger en
    }

    private MedlemskapKildeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
