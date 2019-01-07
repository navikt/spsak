package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.Vedtaksbrev;

class FastsettBehandlingsresultatVedEndring {
    private FastsettBehandlingsresultatVedEndring() {}

    public static Behandlingsresultat fastsett(Behandling revurdering,
                                               boolean erEndringIBeregning,
                                               boolean erVarselOmRevurderingSendt,
                                               LocalDate endringsdato) {
        List<KonsekvensForYtelsen> konsekvenserForYtelsen = utledKonsekvensForYtelsen(erEndringIBeregning);

        Vedtaksbrev vedtaksbrev = utledVedtaksbrev(konsekvenserForYtelsen, erVarselOmRevurderingSendt);
        BehandlingResultatType behandlingResultatType = utledBehandlingResultatType(konsekvenserForYtelsen);
        return buildBehandlingsresultat(revurdering, behandlingResultatType, konsekvenserForYtelsen, vedtaksbrev);
    }

    private static Vedtaksbrev utledVedtaksbrev(List<KonsekvensForYtelsen> konsekvenserForYtelsen, boolean erVarselOmRevurderingSendt) {
        if (!erVarselOmRevurderingSendt && konsekvenserForYtelsen.contains(KonsekvensForYtelsen.INGEN_ENDRING)) {
            return Vedtaksbrev.INGEN;
        }
        return Vedtaksbrev.AUTOMATISK;
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

    private static Behandlingsresultat buildBehandlingsresultat(Behandling revurdering, BehandlingResultatType behandlingResultatType,
                                                                List<KonsekvensForYtelsen> konsekvenserForYtelsen, Vedtaksbrev vedtaksbrev) {
        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        behandlingsresultatBuilder.medBehandlingResultatType(behandlingResultatType);
        behandlingsresultatBuilder.medVedtaksbrev(vedtaksbrev);
        behandlingsresultatBuilder.medRettenTil(RettenTil.HAR_RETT_TIL_FP);
        konsekvenserForYtelsen.forEach(behandlingsresultatBuilder::leggTilKonsekvensForYtelsen);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }
}
