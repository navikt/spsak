package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningDto;

public interface FaktaOmBeregningDtoTjeneste {

    Optional<FaktaOmBeregningDto> lagFaktaOmBeregningDto(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag);

}
