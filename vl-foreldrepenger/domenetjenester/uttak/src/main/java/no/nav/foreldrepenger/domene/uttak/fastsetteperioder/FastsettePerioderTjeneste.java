package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface FastsettePerioderTjeneste {

    /**
     * Vurdere søknadsperioder og lagrer uttaksperioder gitt en behandling.
     *
     * @param behandling behandling som det skal fastsettes uttaksperioder for.
     */
    void fastsettePerioder(Behandling behandling);

    void manueltFastsettePerioder(Behandling behandling, UttakResultatPerioder perioder);
}
