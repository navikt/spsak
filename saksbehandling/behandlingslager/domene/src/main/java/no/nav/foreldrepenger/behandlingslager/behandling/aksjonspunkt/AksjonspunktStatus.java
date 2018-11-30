package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "AksjonspunktStatus")
@DiscriminatorValue(AksjonspunktStatus.DISCRIMINATOR)
public class AksjonspunktStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "AKSJONSPUNKT_STATUS";

    public static final AksjonspunktStatus OPPRETTET = new AksjonspunktStatus("OPPR"); //$NON-NLS-1$
    public static final AksjonspunktStatus UTFØRT = new AksjonspunktStatus("UTFO"); //$NON-NLS-1$
    public static final AksjonspunktStatus AVBRUTT = new AksjonspunktStatus("AVBR"); //$NON-NLS-1$

    private static final List<String> ÅPNE_AKSJONSPUNKT_KODER = Arrays.asList(OPPRETTET.getKode());
    private static final List<String> BEHANDLEDE_AKSJONSPUNKT_KODER = Arrays.asList(UTFØRT.getKode());

    private AksjonspunktStatus() {
        // for hibernate
    }

    private AksjonspunktStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean erÅpentAksjonspunkt() {
        return ÅPNE_AKSJONSPUNKT_KODER.contains(getKode());  // NOSONAR
    }
    public boolean erBehandletAksjonspunkt() {
        return BEHANDLEDE_AKSJONSPUNKT_KODER.contains(getKode());  // NOSONAR
    }


    public static List<String> getÅpneAksjonspunktKoder() {
        return new ArrayList<>(ÅPNE_AKSJONSPUNKT_KODER);
    }
}
