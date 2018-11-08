package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;
import java.util.Set;

import no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat;

public interface VurderMedlemskapTjeneste {

    /**
     *
     * @param behandlingId id i databasen
     * @param vurderingsdato hvilken dato vurderingstjenesten skal kjøre for
     * @return Liste med MedlemResultat
     */
    Set<MedlemResultat> vurderMedlemskap(Long behandlingId, LocalDate vurderingsdato);
}
