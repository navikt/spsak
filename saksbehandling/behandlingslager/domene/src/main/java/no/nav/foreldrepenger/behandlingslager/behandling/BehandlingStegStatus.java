package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Kodefor status i intern håndtering av flyt på et steg
 * <p>
 * Kommer kun til anvendelse dersom det oppstår aksjonspunkter eller noe må legges på vent i et steg. Hvis ikke
 * flyter et rett igjennom til UTFØRT.
 */
@Entity(name = "BehandlingStegStatus")
@DiscriminatorValue(BehandlingStegStatus.DISCRIMINATOR)
public class BehandlingStegStatus extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_STEG_STATUS"; //$NON-NLS-1$

    public static final BehandlingStegStatus INNGANG = new BehandlingStegStatus("INNGANG"); //$NON-NLS-1$

    /** midlertidig intern tilstand når steget startes (etter inngang). */
    public static final BehandlingStegStatus STARTET = new BehandlingStegStatus("STARTET"); //$NON-NLS-1$

    public static final BehandlingStegStatus VENTER = new BehandlingStegStatus("VENTER"); //$NON-NLS-1$
    public static final BehandlingStegStatus UTGANG = new BehandlingStegStatus("UTGANG"); //$NON-NLS-1$
    public static final BehandlingStegStatus AVBRUTT = new BehandlingStegStatus("AVBRUTT"); //$NON-NLS-1$
    public static final BehandlingStegStatus UTFØRT = new BehandlingStegStatus("UTFØRT"); //$NON-NLS-1$
    public static final BehandlingStegStatus FREMOVERFØRT = new BehandlingStegStatus("FREMOVERFØRT"); //$NON-NLS-1$
    public static final BehandlingStegStatus TILBAKEFØRT = new BehandlingStegStatus("TILBAKEFØRT"); //$NON-NLS-1$

    private static final Set<BehandlingStegStatus> KAN_UTFØRE_STEG = new HashSet<>(Arrays.asList(STARTET, VENTER));
    private static final Set<BehandlingStegStatus> KAN_FORTSETTE_NESTE = new HashSet<>(Arrays.asList(UTFØRT, FREMOVERFØRT));
    private static final Set<BehandlingStegStatus> SLUTT_STATUSER = new HashSet<>(Arrays.asList(AVBRUTT, UTFØRT, TILBAKEFØRT));

    /** Kun for intern bruk. */
    public static final BehandlingStegStatus UDEFINERT = new BehandlingStegStatus("-"); //$NON-NLS-1$


    BehandlingStegStatus() {
        // Hibernate trenger den
    }

    private BehandlingStegStatus(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean kanUtføreSteg() {
        return KAN_UTFØRE_STEG.contains(this);
    }

    public boolean kanFortsetteTilNeste() {
        return KAN_FORTSETTE_NESTE.contains(this);
    }

    public static boolean erSluttStatus(BehandlingStegStatus status) {
        return SLUTT_STATUSER.contains(status);
    }

    public boolean erVedInngang() {
       return Objects.equals(INNGANG, this);
    }

    public boolean erVedUtgang() {
        return Objects.equals(UTGANG, this);
     }
}
