package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import java.util.Objects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "AksjonspunktType")
@DiscriminatorValue(AksjonspunktType.DISCRIMINATOR)
public class AksjonspunktType extends Kodeliste {

    public static final String DISCRIMINATOR = "AKSJONSPUNKT_TYPE";
    public static final AksjonspunktType MANUELL = new AksjonspunktType("MANU"); //$NON-NLS-1$
    public static final AksjonspunktType AUTOPUNKT = new AksjonspunktType("AUTO"); //$NON-NLS-1$
    public static final AksjonspunktType OVERSTYRING = new AksjonspunktType("OVST"); //$NON-NLS-1$
    public static final AksjonspunktType SAKSBEHANDLEROVERSTYRING = new AksjonspunktType("SAOV"); //$NON-NLS-1$
    public static final AksjonspunktType UDEFINERT = new AksjonspunktType("-"); //$NON-NLS-1$

    @SuppressWarnings("unused")
    private AksjonspunktType() {
        // Hibernate
    }

    public AksjonspunktType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean erManuell() {
        return Objects.equals(this, MANUELL);
    }

    public boolean erAutopunkt() {
        return Objects.equals(this, AUTOPUNKT);
    }

    public boolean erOverstyringpunkt() {
        return Objects.equals(this, OVERSTYRING) || Objects.equals(this, SAKSBEHANDLEROVERSTYRING);
    }
}
