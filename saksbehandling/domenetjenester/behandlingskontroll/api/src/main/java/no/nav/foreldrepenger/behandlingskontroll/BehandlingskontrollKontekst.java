package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.domene.typer.AktørId;

/**
 * Container som holder kontekst under prosessering av {@link BehandlingSteg}.
 */
public class BehandlingskontrollKontekst {

    private BehandlingLås behandlingLås;
    private AktørId aktørId;
    private Long fagsakId;

    /**
     * NB: Foretrekk {@link BehandlingskontrollTjeneste#initBehandlingskontroll} i stedet for å opprette her direkte.
     */
    public BehandlingskontrollKontekst(Long fagsakId, AktørId aktørId, BehandlingLås behandlingLås) {
        Objects.requireNonNull(behandlingLås, "behandlingLås"); //$NON-NLS-1$
        this.fagsakId = fagsakId;
        this.aktørId = aktørId;
        this.behandlingLås = behandlingLås;
    }

    public BehandlingLås getSkriveLås() {
        return behandlingLås;
    }

    public Long getBehandlingId() {
        return behandlingLås.getBehandlingId();
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BehandlingskontrollKontekst)) {
            return false;
        }
        BehandlingskontrollKontekst other = (BehandlingskontrollKontekst) obj;
        return Objects.equals(fagsakId, other.fagsakId)
                && Objects.equals(aktørId, other.aktørId)
                && Objects.equals(getBehandlingId(), other.getBehandlingId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fagsakId, aktørId, getBehandlingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<fagsakId=" + fagsakId + ", aktørId=" + aktørId + ", behandlingId=" + getBehandlingId() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
