package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;

/**
 * Event publiseres av {@link BehandlingskontrollTjeneste} når en {@link Behandling} endrer steg.
 * Kan brukes til å lytte på flyt i en Behandling og utføre logikk når det skjer.
 */
public class BehandlingStegOvergangEvent implements BehandlingEvent {



    /**
     * Event som fyres dersom vi tilbakefører (går bakover i behandlingstegene)
     */
    public static class BehandlingStegTilbakeføringEvent extends BehandlingStegOvergangEvent {

        public BehandlingStegTilbakeføringEvent(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> forrigeTilstand,
                                                Optional<BehandlingStegTilstand> nyTilstand) {
            super(kontekst, forrigeTilstand, nyTilstand);

        }

        @Override
        public BehandlingStegType getFørsteSteg() {
            // siden hopper bakover blir dette tilSteg
            return getTilStegType();
        }

        @Override
        public BehandlingStegType getSisteSteg() {
            // siden hopper bakover blir dette fraSteg
            return getFraStegType();
        }

        @Override
        public Optional<BehandlingStegStatus> getSisteStegStatus() {
            // siden hopper bakover blir dette fraSteg
            Optional<BehandlingStegTilstand> tilstand = getFraTilstand();
            return Optional.ofNullable(tilstand.isPresent() ? tilstand.get().getBehandlingStegStatus() : null);
        }

        @Override
        public Optional<BehandlingStegStatus> getFørsteStegStatus() {
            // siden hopper bakover blir dette tilSteg
            Optional<BehandlingStegTilstand> tilstand = getTilTilstand();
            return Optional.ofNullable(tilstand.isPresent() ? tilstand.get().getBehandlingStegStatus() : null);
        }
    }

    public static class BehandlingStegOverstyringTilbakeføringEvent extends BehandlingStegTilbakeføringEvent{
        public BehandlingStegOverstyringTilbakeføringEvent(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> forrigeTilstand,
                                           Optional<BehandlingStegTilstand> nyTilstand) {
            super(kontekst, forrigeTilstand, nyTilstand);
        }
        @Override
        public BehandlingSteg.TransisjonType getSkalTil(){return BehandlingSteg.TransisjonType.ETTER_UTGANG;}
    }

    /**
     * Event som fyres dersom vi gjør overhopp (hopper framover i stegene)
     */
    public static class BehandlingStegOverhoppEvent extends BehandlingStegOvergangEvent {

        public BehandlingStegOverhoppEvent(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> forrigeTilstand,
                                           Optional<BehandlingStegTilstand> nyTilstand) {
            super(kontekst, forrigeTilstand, nyTilstand);
        }
    }

    private BehandlingskontrollKontekst kontekst;

    private Optional<BehandlingStegTilstand> fraTilstand;
    private Optional<BehandlingStegTilstand> tilTilstand;

    public BehandlingStegOvergangEvent(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> forrigeTilstand,
                                       Optional<BehandlingStegTilstand> nyTilstand) {
        super();
        this.kontekst = kontekst;
        this.fraTilstand = forrigeTilstand;
        this.tilTilstand = nyTilstand;
    }

    public BehandlingskontrollKontekst getKontekst() {
        return kontekst;
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

    public Optional<BehandlingStegTilstand> getFraTilstand() {
        return fraTilstand;
    }

    public Optional<BehandlingStegTilstand> getTilTilstand() {
        return tilTilstand;
    }

    public BehandlingSteg.TransisjonType getSkalTil(){return BehandlingSteg.TransisjonType.FØR_INNGANG;}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + kontekst + //$NON-NLS-1$
            ", fraTilstand=" + fraTilstand + //$NON-NLS-1$
            ", tilTilstand=" + tilTilstand + //$NON-NLS-1$
            ">"; //$NON-NLS-1$
    }

    public static BehandlingStegOvergangEvent nyEvent(BehandlingskontrollKontekst kontekst,
                                                      Optional<BehandlingStegTilstand> forrigeTilstand, Optional<BehandlingStegTilstand> nyTilstand, int relativForflytning, boolean erOverstyring) {
        if (relativForflytning == 1) {
            // normal forover
            return new BehandlingStegOvergangEvent(kontekst, forrigeTilstand, nyTilstand);
        } else if (relativForflytning < 1) {
            // tilbakeføring
            if(erOverstyring){
                return new BehandlingStegOvergangEvent.BehandlingStegOverstyringTilbakeføringEvent(kontekst, forrigeTilstand, nyTilstand);
            }
            return new BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent(kontekst, forrigeTilstand, nyTilstand);
        } else {
            // > 1
            // framføring
            return new BehandlingStegOvergangEvent.BehandlingStegOverhoppEvent(kontekst, forrigeTilstand, nyTilstand);
        }
    }

    public BehandlingStegType getTilStegType() {
        Optional<BehandlingStegTilstand> tilstand = getTilTilstand();
        if (tilstand.isPresent()) {
            return tilstand.get().getBehandlingSteg();
        } else {
            return null;
        }

    }

    public BehandlingStegType getFraStegType() {
        Optional<BehandlingStegTilstand> tilstand = getFraTilstand();
        if (tilstand.isPresent()) {
            return tilstand.get().getBehandlingSteg();
        } else {
            return null;
        }

    }

    public BehandlingStegType getFørsteSteg() {
        // siden hopper framover blir dette fraSteg
        return getFraStegType();
    }

    public BehandlingStegType getSisteSteg() {
        // siden hopper framover blir dette tilSteg
        return getTilStegType();
    }

    public Optional<BehandlingStegStatus> getFørsteStegStatus() {
        Optional<BehandlingStegTilstand> tilstand = getFraTilstand();
        return Optional.ofNullable(tilstand.isPresent() ? tilstand.get().getBehandlingStegStatus() : null);
    }

    public Optional<BehandlingStegStatus> getSisteStegStatus() {
        Optional<BehandlingStegTilstand> tilstand = getTilTilstand();
        return Optional.ofNullable(tilstand.isPresent() ? tilstand.get().getBehandlingStegStatus() : null);
    }
}
