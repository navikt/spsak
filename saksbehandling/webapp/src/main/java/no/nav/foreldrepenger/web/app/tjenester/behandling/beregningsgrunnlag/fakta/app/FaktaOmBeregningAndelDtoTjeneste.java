package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.ATogFLISammeOrganisasjonDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningAndelDto;

public interface FaktaOmBeregningAndelDtoTjeneste {

    Optional<FaktaOmBeregningAndelDto> lagFrilansAndelDto(Beregningsgrunnlag beregningsgrunnlag);

    /// ATFL I samme organisasjon
    List<ATogFLISammeOrganisasjonDto> lagATogFLISAmmeOrganisasjonListe(Behandling behandling);

    // Arbeidsforhold uten inntektsmelding
    Optional<List<FaktaOmBeregningAndelDto>> lagArbeidsforholdUtenInntektsmeldingDtoList(Behandling behandling);

}
