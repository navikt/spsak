
package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface UtledVurderingsdatoerForMedlemskapTjeneste {

    /** Utleder vurderingsdatoer for:
     * - utledVurderingsdatoerForPersonopplysninger
     * - utledVurderingsdatoerForBortfallAvInntekt
     * - utledVurderingsdatoerForMedlemskap
     *
     * @param behandlingId id i databasen
     * @return datoer med diff i medlemskap
     */
    Set<LocalDate> finnVurderingsdatoer(Long behandlingId);

    Map<LocalDate, Set<VurderingsÅrsak>> finnVurderingsdatoerMedÅrsak(Long behandlingId);
}
