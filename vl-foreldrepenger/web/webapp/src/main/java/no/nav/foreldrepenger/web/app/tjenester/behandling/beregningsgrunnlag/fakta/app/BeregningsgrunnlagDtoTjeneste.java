package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagDto;

public interface BeregningsgrunnlagDtoTjeneste {

    Optional<BeregningsgrunnlagDto> lagBeregningsgrunnlagDto(Behandling behandling);
}
