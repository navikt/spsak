package no.nav.foreldrepenger.behandlingslager.lagretvedtak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "LagretVedtakType")
@DiscriminatorValue(LagretVedtakType.DISCRIMINATOR)
public class LagretVedtakType extends Kodeliste {

    //TODO (TOPAS): (thaonguyen): Må flytte tilbake til vedtakslager når vedtakslager får sin egen persistence unit.

    public static final String DISCRIMINATOR = "LAGRET_VEDTAK_TYPE"; //$NON-NLS-1$
    public static final LagretVedtakType FODSEL = new LagretVedtakType("FODSEL"); //$NON-NLS-1$
    public static final LagretVedtakType ADOPSJON = new LagretVedtakType("ADOPSJON"); //$NON-NLS-1$
    public static final LagretVedtakType UDEFINERT = new LagretVedtakType("-"); //$NON-NLS-1$

    LagretVedtakType() {
        //Hibernate trenger den
    }

    private LagretVedtakType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
