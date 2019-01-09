package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;

public class BehandlingsresultatDto {

    private Long id;
    private BehandlingResultatType type;
    private LocalDate skjaeringstidspunktForeldrepenger;

    public BehandlingsresultatDto() {
        // trengs for deserialisering av JSON
    }

    void setId(Long id) {
        this.id = id;
    }

    void setType(BehandlingResultatType type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public BehandlingResultatType getType() {
        return type;
    }

    public LocalDate getSkjaeringstidspunktForeldrepenger() {
        return skjaeringstidspunktForeldrepenger;
    }

    public void setSkjaeringstidspunktForeldrepenger(LocalDate skjaeringstidspunktForeldrepenger) {
        this.skjaeringstidspunktForeldrepenger = skjaeringstidspunktForeldrepenger;
    }
}
