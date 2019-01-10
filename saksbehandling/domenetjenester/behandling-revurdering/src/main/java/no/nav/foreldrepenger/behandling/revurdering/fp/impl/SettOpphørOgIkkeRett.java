package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;

class SettOpphørOgIkkeRett {
    private SettOpphørOgIkkeRett() {}

    public static Behandlingsresultat fastsett(Behandling revurdering) {
        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        behandlingsresultatBuilder.medBehandlingResultatType(BehandlingResultatType.OPPHØR);
        behandlingsresultatBuilder.leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.YTELSE_OPPHØRER);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }
}
