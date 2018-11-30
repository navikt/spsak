package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.persistence.LockModeType;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

/**
 * Lås, fungerer som token som indikerer at write-lock er tatt ut. Kreves av lagre metoder.
 * Er knyttet til underliggende lock på database rad ({@link LockModeType#PESSIMISTIC_WRITE}).
 * Går out-of-scope og vil ikke være gyldig transaksjonen er ferdig.
 * <p>
 * Låsen initialiseres utelukkende via {@link BehandlingRepository}. Og verfisers også her senere ved lagring som
 * påvirker {@link Behandling}.
 * <p>
 * NB: Kan kun holdes per request/transaksjon.
 * <p>
 * Når lagring skjer vil relevante entiteter også få sin versjon inkrementert
 */
public class BehandlingLås {

    /** brukes kun for nye behandlinger som dummy. */
    private Long behandlingId;

    /**
     * protected, unngå å opprette utenfor denne pakken. Kan overstyres kun til test
     */
    public BehandlingLås(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getBehandlingId() {
        return this.behandlingId;
    }

    void setBehandlingId(long behandlingId) {
        if (this.behandlingId != null && !Objects.equals(behandlingId, this.behandlingId)) {
            throw new IllegalStateException(
                "Kan ikke endre behandlingId til annen verdi, var [" + //$NON-NLS-1$
                    this.behandlingId + "], forsøkte å sette til [" + //$NON-NLS-1$ // NOSONAR
                    behandlingId + "]"); //$NON-NLS-1$ // NOSONAR
        }
        this.behandlingId = behandlingId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof BehandlingLås)) {
            return false;
        }
        BehandlingLås other = (BehandlingLås) obj;
        return Objects.equals(getBehandlingId(), other.getBehandlingId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBehandlingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            "<behandlingId=" + getBehandlingId() + //$NON-NLS-1$
            ">"; //$NON-NLS-1$
    }
}
