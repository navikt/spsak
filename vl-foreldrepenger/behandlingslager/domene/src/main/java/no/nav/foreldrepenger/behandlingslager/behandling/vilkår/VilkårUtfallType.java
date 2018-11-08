package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkarUtfallType")
@DiscriminatorValue(VilkårUtfallType.DISCRIMINATOR)
public class VilkårUtfallType extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_UTFALL_TYPE";
    public static final VilkårUtfallType OPPFYLT = new VilkårUtfallType("OPPFYLT"); //$NON-NLS-1$
    public static final VilkårUtfallType IKKE_OPPFYLT = new VilkårUtfallType("IKKE_OPPFYLT"); //$NON-NLS-1$
    public static final VilkårUtfallType IKKE_VURDERT = new VilkårUtfallType("IKKE_VURDERT"); //$NON-NLS-1$
    
    
    public static final VilkårUtfallType UDEFINERT = new VilkårUtfallType("-"); //$NON-NLS-1$

    VilkårUtfallType() {
        // Hibernate trenger den
    }

    private VilkårUtfallType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
