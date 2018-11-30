package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;

class SettOpphørOgIkkeRett {
    private SettOpphørOgIkkeRett() {}

    public static Behandlingsresultat fastsett(Behandling revurdering) {
        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        behandlingsresultatBuilder.medBehandlingResultatType(BehandlingResultatType.OPPHØR);
        behandlingsresultatBuilder.medRettenTil(RettenTil.HAR_IKKE_RETT_TIL_FP);
        behandlingsresultatBuilder.leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER);
        behandlingsresultatBuilder.medVedtaksbrev(Vedtaksbrev.AUTOMATISK);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }
}
