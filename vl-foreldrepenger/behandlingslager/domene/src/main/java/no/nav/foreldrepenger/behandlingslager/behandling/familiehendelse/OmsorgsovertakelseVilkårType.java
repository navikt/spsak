package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OmsorgsovertakelseVilkårType")
@DiscriminatorValue(OmsorgsovertakelseVilkårType.DISCRIMINATOR)
public class OmsorgsovertakelseVilkårType extends Kodeliste {

    public static final String DISCRIMINATOR = "OMSORGSOVERTAKELSE_VILKAR";

    public static final OmsorgsovertakelseVilkårType OMSORGSVILKÅRET = new OmsorgsovertakelseVilkårType("FP_VK_5"); //$NON-NLS-1$
    public static final OmsorgsovertakelseVilkårType FORELDREANSVARSVILKÅRET_2_LEDD = new OmsorgsovertakelseVilkårType("FP_VK_8"); //$NON-NLS-1$
    public static final OmsorgsovertakelseVilkårType FORELDREANSVARSVILKÅRET_4_LEDD = new OmsorgsovertakelseVilkårType("FP_VK_33"); //$NON-NLS-1$

    /* Legger inn udefinert kode. Må gjerne erstattes av noe annet dersom starttilstand er kjent. */
    public static final OmsorgsovertakelseVilkårType UDEFINERT = new OmsorgsovertakelseVilkårType("-"); //$NON-NLS-1$

    OmsorgsovertakelseVilkårType() {
        // Hibernate trenger en
    }

    private OmsorgsovertakelseVilkårType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
