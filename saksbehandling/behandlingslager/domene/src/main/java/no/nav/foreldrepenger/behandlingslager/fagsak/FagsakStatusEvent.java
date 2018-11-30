package no.nav.foreldrepenger.behandlingslager.fagsak;

import no.nav.foreldrepenger.domene.typer.AktørId;

/**
 * Event publiseres når Fagsak endrer status
 */
public class FagsakStatusEvent implements FagsakEvent {

    private Long fagsakId;
    private FagsakStatus forrigeStatus;
    private FagsakStatus nyStatus;
    private AktørId aktørId;

    FagsakStatusEvent(Long fagsakId, AktørId aktørId, FagsakStatus forrigeStatus, FagsakStatus nyStatus) {
        super();
        this.fagsakId = fagsakId;
        this.aktørId = aktørId;
        this.forrigeStatus = forrigeStatus;
        this.nyStatus = nyStatus;
    }

    @Override
    public AktørId getAktørId() {
        return aktørId;
    }
    
    @Override
    public Long getFagsakId() {
        return fagsakId;
    }

    public FagsakStatus getForrigeStatus() {
        return forrigeStatus;
    }

    public FagsakStatus getNyStatus() {
        return nyStatus;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + fagsakId + //$NON-NLS-1$
                ", forrigeStatus=" + forrigeStatus + //$NON-NLS-1$
                ", nyStatus=" + nyStatus + //$NON-NLS-1$
                ">"; //$NON-NLS-1$
    }
}
