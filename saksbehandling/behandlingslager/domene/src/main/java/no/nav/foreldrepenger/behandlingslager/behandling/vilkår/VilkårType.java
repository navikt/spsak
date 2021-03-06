package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkarType")
@DiscriminatorValue(VilkårType.DISCRIMINATOR)
public class VilkårType extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_TYPE"; //$NON-NLS-1$

    /**
     * statisk koder, kun for konfigurasjon. Bruk VilkårType konstanter for API'er og skriving.
     */
    public static final String FP_VK_2 = "FP_VK_2"; //$NON-NLS-1$
    public static final String FP_VK_3 = "FP_VK_3";
    public static final String FP_VK_21 = "FP_VK_21"; //$NON-NLS-1$
    public static final String FP_VK_23 = "FP_VK_23"; //$NON-NLS-1$
    public static final String FP_VK_34 = "FP_VK_34"; //$NON-NLS-1$
    public static final String FP_VK_41 = "FP_VK_41"; //$NON-NLS-1$

    public static final VilkårType MEDLEMSKAPSVILKÅRET = new VilkårType(FP_VK_2);

    public static final VilkårType SØKNADSFRISTVILKÅRET = new VilkårType(FP_VK_3);
    public static final VilkårType OPPTJENINGSPERIODEVILKÅR = new VilkårType(FP_VK_21);
    public static final VilkårType OPPTJENINGSVILKÅRET = new VilkårType(FP_VK_23);
    public static final VilkårType BEREGNINGSGRUNNLAGVILKÅR = new VilkårType(FP_VK_41);

    /**
     * Brukes i stedet for null der det er optional.
     */
    public static final VilkårType UDEFINERT = new VilkårType("-"); //$NON-NLS-1$

    @Transient
    private String lovReferanse;

    VilkårType() {
        // Hibernate trenger den
    }

    public VilkårType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public String getLovReferanse(FagsakYtelseType fagsakYtelseType) {
        if (lovReferanse == null) {
            lovReferanse = getJsonField("fagsakYtelseType", fagsakYtelseType.getKode(), "lovreferanse"); //$NON-NLS-1$
        }
        return lovReferanse;
    }

    @Override
    public String toString() {
        return super.toString() +
            "<lovReferanse FP=" + getLovReferanse(FagsakYtelseType.FORELDREPENGER) + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
