package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface MedlemDtoTjeneste {

    Optional<MedlemDto> lagMedlemDto(Behandling behandling);

    Optional<MedlemDto> lagMedlemDto(Long behandlingId);

    Optional<MedlemV2Dto> lagMedlemPeriodisertDto(Long behandlingId);
}
