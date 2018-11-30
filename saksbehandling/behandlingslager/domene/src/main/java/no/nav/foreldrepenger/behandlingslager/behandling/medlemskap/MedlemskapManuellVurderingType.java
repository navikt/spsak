package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapManuellVurderingType")
@DiscriminatorValue(MedlemskapManuellVurderingType.DISCRIMINATOR)
public class MedlemskapManuellVurderingType extends Kodeliste {

    public static final String DISCRIMINATOR = "MEDLEMSKAP_MANUELL_VURD";
    public static final MedlemskapManuellVurderingType MEDLEM = new MedlemskapManuellVurderingType("MEDLEM"); //$NON-NLS-1$
    public static final MedlemskapManuellVurderingType UNNTAK = new MedlemskapManuellVurderingType("UNNTAK"); //$NON-NLS-1$
    public static final MedlemskapManuellVurderingType IKKE_RELEVANT = new MedlemskapManuellVurderingType("IKKE_RELEVANT"); //$NON-NLS-1$
    public static final MedlemskapManuellVurderingType SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS = new MedlemskapManuellVurderingType("OPPHOR_PGA_ENDRING_I_TPS"); //$NON-NLS-1$


    public static final MedlemskapManuellVurderingType UDEFINERT = new MedlemskapManuellVurderingType("-"); //$NON-NLS-1$


    MedlemskapManuellVurderingType() {
        // Hibernate trenger en
    }

    private MedlemskapManuellVurderingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean visesPåKlient() {
        String skalVises = getJsonField("gui"); //$NON-NLS-1$
        if (skalVises == null) {
            return true;
        }
        return Boolean.parseBoolean(skalVises);
    }

}
