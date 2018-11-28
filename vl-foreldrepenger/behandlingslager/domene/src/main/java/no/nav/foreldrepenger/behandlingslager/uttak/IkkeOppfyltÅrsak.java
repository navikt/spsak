package no.nav.foreldrepenger.behandlingslager.uttak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "IkkeOppfyltÅrsak")
@DiscriminatorValue(IkkeOppfyltÅrsak.DISCRIMINATOR)
public class IkkeOppfyltÅrsak extends PeriodeResultatÅrsak {
    static final String DISCRIMINATOR = "IKKE_OPPFYLT_AARSAK";

    // Uttak årsaker

    public static final IkkeOppfyltÅrsak SØKNADSFRIST = new IkkeOppfyltÅrsak("4020");

    private IkkeOppfyltÅrsak(String kode) {
        super(kode, DISCRIMINATOR);
    }

    IkkeOppfyltÅrsak() {
        // For hibernate
    }

}
