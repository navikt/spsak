package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandling.aksjonspunkt.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class OverstyrteAksjonspunkterDto implements AbacDto {

    @Valid
    private BehandlingIdDto behandlingId;

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

    @Valid
    @Size(min = 1, max = 10)
    private Collection<OverstyringAksjonspunktDto> overstyrteAksjonspunktDtoer;

    public static OverstyrteAksjonspunkterDto lagDto(Long behandlingId, Long behandlingVersjon,
            Collection<OverstyringAksjonspunktDto> overstyrteAksjonspunktDtoer) {
        OverstyrteAksjonspunkterDto dto = new OverstyrteAksjonspunkterDto();
        dto.behandlingId = new BehandlingIdDto(behandlingId);
        dto.behandlingVersjon = behandlingVersjon;
        dto.overstyrteAksjonspunktDtoer = overstyrteAksjonspunktDtoer;
        return dto;
    }

    public BehandlingIdDto getBehandlingId() {
        return behandlingId;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    public Collection<OverstyringAksjonspunktDto> getOverstyrteAksjonspunktDtoer() {
        return overstyrteAksjonspunktDtoer;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abac = AbacDataAttributter.opprett().leggTilBehandlingsId(getBehandlingId().getBehandlingId());
        overstyrteAksjonspunktDtoer.forEach(apDto -> abac.leggTil(apDto.abacAttributter()));
        return abac;
    }
}
