package no.nav.foreldrepenger.behandling;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface OpplysningsPeriodeTjeneste {

    /**
     * Beregner opplysningsperioden (Perioden vi ber om informasjon fra registerne) for en gitt behandling.
     *
     * Benytter konfig-verdier for å setter lengden på intervallene på hver side av skjæringstidspunkt for registerinnhenting.
     *
     * @param behandling behandlingen
     * @return intervallet
     */
    Interval beregn(Behandling behandling);
}
