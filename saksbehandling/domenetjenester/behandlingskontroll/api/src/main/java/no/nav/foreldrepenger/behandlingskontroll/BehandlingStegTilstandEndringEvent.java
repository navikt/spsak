package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class BehandlingStegTilstandEndringEvent implements BehandlingEvent {

    private Optional<StegTilstand> fraTilstand;
    private Optional<StegTilstand> tilTilstand;

    private BehandlingskontrollKontekst kontekst;


    public BehandlingStegTilstandEndringEvent(BehandlingskontrollKontekst kontekst, Optional<StegTilstand> forrigeTilstand){
        super();
        this.kontekst = kontekst;
        this.fraTilstand = forrigeTilstand;
    }

    public void setNyTilstand(Optional<StegTilstand> nyTilstand){
        this.tilTilstand=nyTilstand;
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    public Optional<StegTilstand> getFraTilstand() {
        return fraTilstand;
    }

    public Optional<StegTilstand> getTilTilstand() {
        return tilTilstand;
    }
}
