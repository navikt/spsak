package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

/** Snapshot av behandling tilstand. */
public class BehandlingskontrollTilstand {

    private final Long behandlingId;
    private final BehandlingType behandlingType;
    private final FagsakYtelseType ytelseType;

    private Optional<StegTilstand> stegTilstand;
    private Map<BehandlingÅrsakType, Boolean> behandlingÅrsaker;
    private BehandlingStatus status;
    private List<Aksjonspunkt> aksjonspunkter;
    private StartpunktType startpunkt;

    public BehandlingskontrollTilstand(Long behandlingId, FagsakYtelseType ytelseType, BehandlingType behandlingType) {
        this.behandlingId = Objects.requireNonNull(behandlingId, "behandlingId");
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.behandlingType = Objects.requireNonNull(behandlingType, "behandlingType");
    }

    private static <V> V verifiserIkkeSattFør(V eksisterende, V ny, String navn) {
        if (eksisterende != null) {
            throw new IllegalStateException("Kan ikke endre verdi [" + navn + "] når satt fra før. Eksisterende verdi=" + eksisterende + ", ny=" + ny);
        } else if (ny == null) {
            throw new IllegalArgumentException("Kan ikke sette verdi til null for " + navn + "");
        }
        return ny;
    }

    public List<Aksjonspunkt> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public BehandlingStegStatus getAktivtBehandlingStegStatus() {
        return stegTilstand.map(StegTilstand::getStatus).orElse(null);
    }

    public BehandlingStegType getAktivtStegType() {
        return stegTilstand.map(StegTilstand::getStegType).orElse(null);
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    public BehandlingStegStatus getStegStatus() {
        return stegTilstand.map(StegTilstand::getStatus).orElse(null);
    }

    public BehandlingStegType getStegType() {
        return stegTilstand.map(StegTilstand::getStegType).orElse(null);
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public boolean harBehandlingÅrsak(BehandlingÅrsakType årsakType) {
        return behandlingÅrsaker.containsKey(årsakType);
    }

    public boolean harBehandlingÅrsakManuell(BehandlingÅrsakType årsakType) {
        return behandlingÅrsaker.get(årsakType) == Boolean.TRUE;
    }

    public void setAksjonspunkter(List<Aksjonspunkt> aksjonspunkter) {
        this.aksjonspunkter = Collections.unmodifiableList(verifiserIkkeSattFør(this.aksjonspunkter, aksjonspunkter, "aksjonspunkter"));
    }

    public void setBehandlingÅrsaker(Map<BehandlingÅrsakType, Boolean> behandlingÅrsaker) {
        this.behandlingÅrsaker = Collections.unmodifiableMap(verifiserIkkeSattFør(this.behandlingÅrsaker, behandlingÅrsaker, "behandlingÅrsaker"));
    }

    public void setStartpunkt(StartpunktType startpunkt) {
        this.startpunkt = verifiserIkkeSattFør(this.startpunkt, startpunkt, "startpunkt");
    }

    public void setStatus(BehandlingStatus status) {
        this.status = verifiserIkkeSattFør(this.status, status, "status");
    }

    public void setStegTilstand(Optional<StegTilstand> stegTilstand) {
        this.stegTilstand = verifiserIkkeSattFør(this.stegTilstand, stegTilstand, "stegTilstand");
    }

    public void setStegTilstand(BehandlingStegType stegType) {
        this.stegTilstand = verifiserIkkeSattFør(this.stegTilstand, Optional.of(new StegTilstand(stegType, null)), "stegTilstand");
    }

    public boolean erStegStatus(BehandlingStegStatus behandlingStegStatus) {
        return Objects.equals(this.getStegStatus(), behandlingStegStatus);
    }

    public boolean erSteg(BehandlingStegType bst) {
        return Objects.equals(this.getAktivtStegType(), bst);
    }

    public boolean erStegStatus(BehandlingStegType bst, BehandlingStegStatus stegStatus) {
        return erSteg(bst) && erStegStatus(stegStatus);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "behandlingId=" + behandlingId
            + ", steg=" + stegTilstand
            + ", startpunkt=" + startpunkt
            + ", aksjonspunkter=" + aksjonspunkter + ">";
    }

}
