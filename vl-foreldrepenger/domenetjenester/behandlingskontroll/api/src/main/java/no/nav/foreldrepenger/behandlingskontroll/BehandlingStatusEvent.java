package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;

/**
 * Event publiseres av {@link BehandlingskontrollTjeneste} når en {@link Behandling} endrer steg.
 * Kan brukes til å lytte på flyt i en Behandling og utføre logikk når det skjer.
 */
public class BehandlingStatusEvent implements BehandlingEvent {

    private BehandlingskontrollKontekst kontekst;

    private BehandlingStatus nyStatus;

    BehandlingStatusEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
        this.kontekst = kontekst;
        this.nyStatus = nyStatus;
    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    public BehandlingskontrollKontekst getKontekst() {
        return kontekst;
    }

    public BehandlingStatus getNyStatus() {
        return nyStatus;
    }

    static void validerRiktigStatus(BehandlingStatus nyStatus, BehandlingStatus expected) {
        if (!Objects.equals(expected, nyStatus)) {
            throw new IllegalArgumentException("Kan bare være " + expected + ", fikk: " + nyStatus);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + kontekst + //$NON-NLS-1$
            ", nyStatus=" + nyStatus + //$NON-NLS-1$
            ">"; //$NON-NLS-1$
    }

    public static class BehandlingAvsluttetEvent extends BehandlingStatusEvent {
        BehandlingAvsluttetEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
            super(kontekst, nyStatus);
            validerRiktigStatus(nyStatus, BehandlingStatus.AVSLUTTET);
        }
    }

    public static class BehandlingOpprettetEvent extends BehandlingStatusEvent {
        BehandlingOpprettetEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
            super(kontekst, nyStatus);
            validerRiktigStatus(nyStatus, BehandlingStatus.OPPRETTET);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends BehandlingStatusEvent> V nyEvent(BehandlingskontrollKontekst kontekst, BehandlingStatus nyStatus) {
        if (BehandlingStatus.AVSLUTTET.equals(nyStatus)) {
            return (V) new BehandlingAvsluttetEvent(kontekst, nyStatus);
        } else if (BehandlingStatus.OPPRETTET.equals(nyStatus)) {
            return (V) new BehandlingOpprettetEvent(kontekst, nyStatus);
        } else {
            return (V) new BehandlingStatusEvent(kontekst, nyStatus);
        }
    }
}
