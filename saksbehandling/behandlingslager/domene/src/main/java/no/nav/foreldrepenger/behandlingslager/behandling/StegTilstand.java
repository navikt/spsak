package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Objects;
import java.util.Optional;

public class StegTilstand {

    private BehandlingStegType steg;
    private BehandlingStegStatus status;

    public StegTilstand(BehandlingStegType steg, BehandlingStegStatus status) {
        this.steg = steg;
        this.status = status;
    }

    public BehandlingStegStatus getStatus() {
        return status;
    }

    public BehandlingStegType getStegType() {
        return steg;
    }

    public boolean erVedInngangAvSteg() {
        return this.status != null && this.status.erVedInngang();
    }

    public boolean erVedUtgangAvSteg() {
        return this.status != null && this.status.erVedUtgang();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        StegTilstand other = (StegTilstand) obj;
        return Objects.equals(this.steg, other.steg) && Objects.equals(this.status, other.status);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() +"<" + steg +", " + status + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(steg, status);
    }

    public static Optional<StegTilstand> fra(BehandlingskontrollTilstand tilstand) {
        return tilstand == null ? Optional.empty() : Optional.ofNullable(tilstand).map(t -> new StegTilstand(t.getStegType(), t.getStegStatus()));
    }

    public static Optional<StegTilstand> fra(BehandlingStegTilstand stegTilstand) {
        return stegTilstand == null ? Optional.empty() : fra(Optional.ofNullable(stegTilstand));
    }

    public static Optional<StegTilstand> fra(Optional<BehandlingStegTilstand> tilstand) {
        return tilstand.map(t -> new StegTilstand(t.getStegType(), t.getStatus()));
    }

}