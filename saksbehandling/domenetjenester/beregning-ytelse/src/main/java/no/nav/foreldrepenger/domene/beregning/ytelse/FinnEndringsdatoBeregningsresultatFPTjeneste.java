package no.nav.foreldrepenger.domene.beregning.ytelse;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;

public interface FinnEndringsdatoBeregningsresultatFPTjeneste {

    /**
     * Finner endringsdatoen for en revurdering. Beregningsresultatet fra revurderingen og den originale behandlingen
     * blir sammenlignet, og endringsdatoen blir lik datoen hvor den første endringen fant sted.
     *
     * @param behandling - En behandling av type revurdering.
     * @param revurderingBeregningsresultat - Beregningsresultatet for revurderingen
     * @return En tom Optional hvis ingen endring funnet.
     *         En Optional med endringsdatoen hvis en endring blir funnet.
     */
    Optional<LocalDate> finnEndringsdato(Behandling behandling, BeregningsresultatFP revurderingBeregningsresultat);

}
