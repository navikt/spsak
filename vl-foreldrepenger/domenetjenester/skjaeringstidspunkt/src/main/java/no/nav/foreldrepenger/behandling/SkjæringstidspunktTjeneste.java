package no.nav.foreldrepenger.behandling;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface SkjæringstidspunktTjeneste {

    /**
     * Skjæringstidspunkt for foreldrepenger
     * @param behandling behandlingen
     * @return datoen
     */
    LocalDate utledSkjæringstidspunktForForeldrepenger(Behandling behandling);

    /**
     * Skjæringstidspunkt som benyttes for registerinnhenting
     *
     * @param behandling behandlingen
     * @return datoen
     */
    LocalDate utledSkjæringstidspunktForRegisterInnhenting(Behandling behandling);

    /**
     * TODO:
     * @param behandling behanlingen
     * @return dato
     */
    LocalDate førsteUttaksdag(Behandling behandling);

    /**
     * ES: Skjæringstidspunkt for Engangsstønad(hendelses skjæringstidspunkt)
     * FP: Skjæringstidspunkt for Foreldrepenger
     *      1. Opptjeningsperiode tom-dato (Bekreftet)
     *      2. Avklart startdato for permisjon (Delevis avklart)
     *      3. Ønsket startdato for permijson (Uavklart)
     *
     * @param behandling behandlingen
     * @return datoen
     */
    LocalDate utledSkjæringstidspunktFor(Behandling behandling);
}
