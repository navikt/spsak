package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;

/**
 * Event som fyres når Behandlingskontroll STARTER å prosessere en behandling, STOPPER (eks. fordi den er avsluttet,
 * eller stopper i et vurderingspunkt).
 * Eventuelt også dersom en Exception mottas.
 */
public abstract class BehandlingskontrollEvent implements BehandlingEvent {

    private Behandling behandling;
    private BehandlingModellImpl behandlingModell;
    private BehandlingStegType stegType;
    private BehandlingStegStatus stegStatus;
    private BehandlingskontrollKontekst kontekst;

    public BehandlingskontrollEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell) {
        this(kontekst, behandling, behandlingModell, behandling.getAktivtBehandlingSteg(), behandling.getBehandlingStegStatus());
    }

    public BehandlingskontrollEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                                    BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
        this.kontekst = kontekst;
        this.behandling = behandling;
        this.behandlingModell = behandlingModell;
        this.stegType = stegType;
        this.stegStatus = stegStatus;

    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    public List<Aksjonspunkt> getÅpneAksjonspunktForAktivtBehandlingSteg() {
        return filterAksjonspunkt(this.getBehandling().getÅpneAksjonspunkter());
    }

    public List<Aksjonspunkt> getÅpneAksjonspunktForAktivtBehandlingSteg(AksjonspunktType aksjonspunktType) {
        return filterAksjonspunkt(this.getBehandling().getÅpneAksjonspunkter(aksjonspunktType));
    }

    private List<Aksjonspunkt> filterAksjonspunkt(List<Aksjonspunkt> åpneAksjonspunkter) {
        Set<String> aksjonspunktForSteg = behandlingModell.finnAksjonspunktDefinisjoner(stegType);
        return åpneAksjonspunkter.stream()
                .filter(ad -> aksjonspunktForSteg.contains(ad.getAksjonspunktDefinisjon().getKode()))
                .collect(Collectors.toList());
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public BehandlingModellImpl getBehandlingModell() {
        return behandlingModell;
    }

    public BehandlingStegType getStegType() {
        return stegType;
    }

    public BehandlingStegStatus getStegStatus() {
        return stegStatus;
    }

    /**
     * Fyres når {@link BehandlingskontrollTjeneste#prosesserBehandling(BehandlingskontrollKontekst)} starter å kjøre.
     */
    public static class StartetEvent extends BehandlingskontrollEvent {

        public StartetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell) {
            super(kontekst, behandling, behandlingModell);
        }

        public StartetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                            BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
            super(kontekst, behandling, behandlingModell, stegType, stegStatus);
        }
        
    }

    /**
     * Fyres når {@link BehandlingskontrollTjeneste#prosesserBehandling(BehandlingskontrollKontekst)} stopper. Stoppet
     * fordi aksjonspunkter er funnet..
     */
    public static class StoppetEvent extends BehandlingskontrollEvent {

        public StoppetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell) {
            super(kontekst, behandling, behandlingModell);
        }

        public StoppetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                            BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
            super(kontekst, behandling, behandlingModell, stegType, stegStatus);
        }

    }

    /**
     * Fyres når {@link BehandlingskontrollTjeneste#prosesserBehandling(BehandlingskontrollKontekst)} stopper. Stoppet
     * fordi prosessen er avsluttet.
     * 
     * @see StoppetEvent
     */
    public static class AvsluttetEvent extends BehandlingskontrollEvent {

        public AvsluttetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell) {
            super(kontekst, behandling, behandlingModell);
        }

        public AvsluttetEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                              BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
            super(kontekst, behandling, behandlingModell, stegType, stegStatus);
        }

    }

    /**
     * Fyres når {@link BehandlingskontrollTjeneste#prosesserBehandling(BehandlingskontrollKontekst)} får en Exception
     */
    public static class ExceptionEvent extends BehandlingskontrollEvent {

        private RuntimeException exception;

        public ExceptionEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                              RuntimeException exception) {
            super(kontekst, behandling, behandlingModell);
            this.exception = exception;
        }

        public ExceptionEvent(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingModellImpl behandlingModell,
                              BehandlingStegType stegType, BehandlingStegStatus stegStatus, RuntimeException exception) {
            super(kontekst, behandling, behandlingModell, stegType, stegStatus);
            this.exception = exception;
        }

        public RuntimeException getException() {
            return exception;
        }

    }

}
