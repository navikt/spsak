package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ProsessTaskGruppeIdDto implements AbacDto {

    @Nullable
    @Size(min = 1, max = 250)
    @Pattern(regexp = "[a-zA-Z0-9-.]+")
    private String gruppe;

    public ProsessTaskGruppeIdDto() {
        gruppe = null; // NOSONAR
    }

    public ProsessTaskGruppeIdDto(String gruppe) {
        this.gruppe = gruppe;
    }

    public String getGruppe() {
        return gruppe;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); // Har ikke noe å bidra med her
    }

    @Override
    public String toString() {
        return "BehandlingIdDto{" +
            "behandlingId=" + gruppe +
            '}';
    }
}
