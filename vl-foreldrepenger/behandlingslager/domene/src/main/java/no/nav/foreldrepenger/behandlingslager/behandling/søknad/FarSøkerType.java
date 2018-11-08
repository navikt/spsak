package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FarSøkerType")
@DiscriminatorValue(FarSøkerType.DISCRIMINATOR)
public class FarSøkerType extends Kodeliste {
    public static final String DISCRIMINATOR = "FAR_SOEKER_TYPE";
    public static final FarSøkerType ADOPTERER_ALENE = new FarSøkerType("ADOPTERER_ALENE"); //$NON-NLS-1$
    public static final FarSøkerType ANDRE_FORELDER_DØD = new FarSøkerType("ANDRE_FORELDER_DØD"); //$NON-NLS-1$
    public static final FarSøkerType OVERTATT_OMSORG = new FarSøkerType("OVERTATT_OMSORG"); //$NON-NLS-1$
    public static final FarSøkerType OVERTATT_OMSORG_F = new FarSøkerType("OVERTATT_OMSORG_F"); //$NON-NLS-1$
    public static final FarSøkerType ANDRE_FORELD_DØD_F = new FarSøkerType("ANDRE_FORELD_DØD_F"); //$NON-NLS-1$

    /* Legger inn udefinert kode.  Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final FarSøkerType UDEFINERT = new FarSøkerType("-"); //$NON-NLS-1$
    
    FarSøkerType() {
        // Hibernate trenger en
    }

    private FarSøkerType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
