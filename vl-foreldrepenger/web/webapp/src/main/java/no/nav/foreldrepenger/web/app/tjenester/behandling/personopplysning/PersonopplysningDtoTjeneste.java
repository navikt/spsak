package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.time.LocalDate;
import java.util.Optional;


public interface PersonopplysningDtoTjeneste {
    Optional<PersonopplysningDto> lagPersonopplysningDto(Long behandlingId, LocalDate now);
}
