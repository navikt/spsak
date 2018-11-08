package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatEngangsstønadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;

import java.util.Optional;

public interface BeregningsresultatTjeneste {

    Optional<BeregningsresultatMedUttaksplanDto> lagBeregningsresultatMedUttaksplan(Behandling behandling);

    Optional<BeregningsresultatEngangsstønadDto> lagBeregningsresultatEnkel(Behandling behandling);

}
