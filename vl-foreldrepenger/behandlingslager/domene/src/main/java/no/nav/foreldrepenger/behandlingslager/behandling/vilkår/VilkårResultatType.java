package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkarResultatType")
@DiscriminatorValue(VilkårResultatType.DISCRIMINATOR)
public class VilkårResultatType extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_RESULTAT_TYPE";
    public static final VilkårResultatType INNVILGET = new VilkårResultatType("INNVILGET"); //$NON-NLS-1$
    public static final VilkårResultatType AVSLÅTT = new VilkårResultatType("AVSLAATT"); //$NON-NLS-1$
    public static final VilkårResultatType IKKE_FASTSATT = new VilkårResultatType("IKKE_FASTSATT"); //$NON-NLS-1$
    
    public static final VilkårResultatType UDEFINERT = new VilkårResultatType("-"); //$NON-NLS-1$
    
    VilkårResultatType() {
        // Hibernate trenger den
    }

    private VilkårResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
