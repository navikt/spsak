package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;

class FastsettBehandlingsresultatVedEndring {
    private FastsettBehandlingsresultatVedEndring() {}

    public static Behandlingsresultat fastsett(Behandling revurdering,
                                               boolean erEndringIBeregning) {
        List<KonsekvensForYtelsen> konsekvenserForYtelsen = utledKonsekvensForYtelsen(erEndringIBeregning);

        BehandlingResultatType behandlingResultatType = utledBehandlingResultatType(konsekvenserForYtelsen);
        return buildBehandlingsresultat(revurdering, behandlingResultatType, konsekvenserForYtelsen);
    }

    private static BehandlingResultatType utledBehandlingResultatType(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        if (konsekvenserForYtelsen.contains(KonsekvensForYtelsen.INGEN_ENDRING)) {
            return BehandlingResultatType.INGEN_ENDRING;
        }
        return BehandlingResultatType.FORELDREPENGER_ENDRET;
    }

    private static List<KonsekvensForYtelsen> utledKonsekvensForYtelsen(boolean erEndringIBeregning) {
        List<KonsekvensForYtelsen> konsekvensForYtelsen = new ArrayList<>();

        if (erEndringIBeregning) {
            konsekvensForYtelsen.add(KonsekvensForYtelsen.ENDRING_I_BEREGNING);
        }
        if (konsekvensForYtelsen.isEmpty()) {
            konsekvensForYtelsen.add(KonsekvensForYtelsen.INGEN_ENDRING);
        }
        return konsekvensForYtelsen;
    }

    private static Behandlingsresultat buildBehandlingsresultat(Behandling revurdering, 
                                                                BehandlingResultatType behandlingResultatType,
                                                                List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        behandlingsresultatBuilder.medBehandlingResultatType(behandlingResultatType);
        konsekvenserForYtelsen.forEach(behandlingsresultatBuilder::leggTilKonsekvensForYtelsen);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }
}
