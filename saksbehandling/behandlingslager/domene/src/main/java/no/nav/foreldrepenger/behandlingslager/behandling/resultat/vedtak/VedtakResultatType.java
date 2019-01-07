package no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VedtakResultatType")
@DiscriminatorValue(VedtakResultatType.DISCRIMINATOR)
public class VedtakResultatType extends Kodeliste {

    public static final String DISCRIMINATOR = "VEDTAK_RESULTAT_TYPE"; //$NON-NLS-1$
    public static final VedtakResultatType INNVILGET = new VedtakResultatType("INNVILGET"); //$NON-NLS-1$
    public static final VedtakResultatType DELVIS_INNVILGET = new VedtakResultatType("DELVIS_INNVILGET"); //$NON-NLS-1$
    public static final VedtakResultatType AVSLAG = new VedtakResultatType("AVSLAG"); //$NON-NLS-1$
    public static final VedtakResultatType OPPHØR = new VedtakResultatType("OPPHØR"); //$NON-NLS-1$;
    public static final VedtakResultatType UDEFINERT = new VedtakResultatType("-"); //$NON-NLS-1$

    private VedtakResultatType() {
        //Hibernate trenger den
    }

    private VedtakResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
