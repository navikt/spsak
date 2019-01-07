package no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "IverksettingStatus")
@DiscriminatorValue(IverksettingStatus.DISCRIMINATOR)
public class IverksettingStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "IVERKSETTING_STATUS"; //$NON-NLS-1$
    public static final IverksettingStatus IKKE_IVERKSATT = new IverksettingStatus("IKKE_IVERKSATT"); //$NON-NLS-1$
    public static final IverksettingStatus UNDER_IVERKSETTING = new IverksettingStatus("UNDER_IVERKSETTING"); //$NON-NLS-1$
    public static final IverksettingStatus IVERKSATT = new IverksettingStatus("IVERKSATT"); //$NON-NLS-1$
    public static final IverksettingStatus UDEFINERT = new IverksettingStatus("-"); //$NON-NLS-1$

    private IverksettingStatus() {
        //Hibernate trenger den
    }

    private IverksettingStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
