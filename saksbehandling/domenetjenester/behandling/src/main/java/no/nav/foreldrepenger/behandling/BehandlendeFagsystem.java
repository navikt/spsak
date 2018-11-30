package no.nav.foreldrepenger.behandling;

import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class BehandlendeFagsystem {
    private BehandlendeSystem behandlendeSystem;
    private Saksnummer saksnummer;
    private LocalDateTime prøvIgjenTidspunkt;

    public BehandlendeFagsystem(BehandlendeSystem behandlendeSystem) {
        this.behandlendeSystem = behandlendeSystem;
    }

    public BehandlendeFagsystem medSaksnummer(Saksnummer saksnummer){
        this.saksnummer = saksnummer;
        return this;
    }

    public BehandlendeFagsystem medPrøvIgjenTidspunkt(LocalDateTime prøvIgjenTidspunkt){
        this.prøvIgjenTidspunkt = prøvIgjenTidspunkt;
        return this;
    }

    public Optional<Saksnummer> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public BehandlendeSystem getBehandlendeSystem() {
        return behandlendeSystem;
    }

    public LocalDateTime getPrøvIgjenTidspunkt() {
        return prøvIgjenTidspunkt;
    }

    public enum BehandlendeSystem {
        VEDTAKSLØSNING,
        INFOTRYGD,
        MANUELL_VURDERING,
        PRØV_IGJEN
    }
}
