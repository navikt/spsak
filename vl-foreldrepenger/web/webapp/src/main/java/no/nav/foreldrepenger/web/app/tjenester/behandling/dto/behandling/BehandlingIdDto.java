package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;


import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

/** Referanse til en behandling. */
@JsonInclude(Include.NON_NULL)
public class BehandlingIdDto implements AbacDto {

    @Nullable
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long saksnummer;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    public BehandlingIdDto() {
        behandlingId = null; // NOSONAR
    }

    public BehandlingIdDto(String behandlingId) {
        this.behandlingId = Long.valueOf(behandlingId);
    }

    public BehandlingIdDto(Long saksnummer, Long behandlingId) {
        this.saksnummer = saksnummer;
        this.behandlingId = behandlingId;
    }

    public BehandlingIdDto(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getSaksnummer() {
        return saksnummer;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abac = AbacDataAttributter.opprett();
        // FIXME (FC) Utkommentert saksnummer. Fra Frode: Tror ikke PEP håndterer dette riktig hvis den får flere attributter p.t
        // abac. leggTilSaksnummer(saksnummer.getVerdi())
        return abac.leggTilBehandlingsId(getBehandlingId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' +
            (saksnummer == null ? "" : "saksnummer=" + saksnummer + ", ") +
            "behandlingId=" + behandlingId +
            '>';
    }
}
