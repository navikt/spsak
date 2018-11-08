package no.nav.foreldrepenger.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface VurderOmSakSkalTilInfotrygdTjeneste {

    /** Hvis skjæringstidspunkt for ytelsen inntreffer før 01.01.2019 return = true
     * ellers return = false. (PGA av regelsett)
     *
     * Til INFO
     * benytter skjæringstidspunkt utledet av metoden utledSkjæringstidspunktForForeldrepenger()
     * kan bare brukes av en sak med FagsakYtelseType == FORELDREPENGER
     * @param behandling behandlingen
     * @return true/false
     */
    boolean skalForeldrepengersakBehandlesAvInfotrygd(Behandling behandling);
}
