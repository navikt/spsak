package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkarUtfallMerknad")
@DiscriminatorValue(VilkårUtfallMerknad.DISCRIMINATOR)
public class VilkårUtfallMerknad extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_UTFALL_MERKNAD";
    public static final VilkårUtfallMerknad VM_1001 = new VilkårUtfallMerknad("1001"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1002 = new VilkårUtfallMerknad("1002"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1003 = new VilkårUtfallMerknad("1003"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1004 = new VilkårUtfallMerknad("1004"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1005 = new VilkårUtfallMerknad("1005"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1006 = new VilkårUtfallMerknad("1006"); //$NON-NLS-1$

    public static final VilkårUtfallMerknad VM_1020 = new VilkårUtfallMerknad("1020"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1021 = new VilkårUtfallMerknad("1021"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1022 = new VilkårUtfallMerknad("1022"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1023 = new VilkårUtfallMerknad("1023"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1024 = new VilkårUtfallMerknad("1024"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1025 = new VilkårUtfallMerknad("1025"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1026 = new VilkårUtfallMerknad("1026"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1027 = new VilkårUtfallMerknad("1027"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1028 = new VilkårUtfallMerknad("1028"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1029 = new VilkårUtfallMerknad("1029"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_1051 = new VilkårUtfallMerknad("1051"); //$NON-NLS-1$

    /** opptjeningsvilkåret */
    public static final VilkårUtfallMerknad VM_1035 = new VilkårUtfallMerknad("1035"); //$NON-NLS-1$

    /** beregningsvilkåret */
    public static final VilkårUtfallMerknad VM_1041 = new VilkårUtfallMerknad("1041"); //$NON-NLS-1$

    /** søknadfristvilkåret*/
    public static final VilkårUtfallMerknad VM_5007 = new VilkårUtfallMerknad("5007"); //$NON-NLS-1$

    public static final VilkårUtfallMerknad VM_7001 = new VilkårUtfallMerknad("7001"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_7002 = new VilkårUtfallMerknad("7002"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_7003 = new VilkårUtfallMerknad("7003"); //$NON-NLS-1$
    public static final VilkårUtfallMerknad VM_7004 = new VilkårUtfallMerknad("7004"); //$NON-NLS-1$


    public static final VilkårUtfallMerknad UDEFINERT = new VilkårUtfallMerknad("-"); //$NON-NLS-1$

    VilkårUtfallMerknad() {
        // Hibernate trenger den
    }

    private VilkårUtfallMerknad(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
