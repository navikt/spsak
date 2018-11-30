package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;

class HarLøpendeVedtak {
    private HarLøpendeVedtak() {}

    public static boolean vurder(LocalDate endringsdato, Optional<UttakResultatEntitet> uttakresultatOpt) {
        return harLøpendeVedtak(endringsdato, uttakresultatOpt);
    }

    private static boolean harLøpendeVedtak(LocalDate endringsdato, Optional<UttakResultatEntitet> uttakresultatOpt) {
        if (!uttakresultatOpt.isPresent()) {
            return false;
        }
        UttakResultatEntitet uttakresultat = uttakresultatOpt.get();
        return uttakresultat.getGjeldendePerioder().getPerioder().stream()
            .anyMatch(periode -> !periode.getTom().isBefore(endringsdato) && PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()));
    }

    public static Behandlingsresultat fastsett(Behandling revurdering, List<KonsekvensForYtelsen> konsekvenserForYtelsen) {


        Behandlingsresultat behandlingsresultat = revurdering.getBehandlingsresultat();
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingsresultat);
        konsekvenserForYtelsen.forEach(behandlingsresultatBuilder::leggTilKonsekvensForYtelsen);
        behandlingsresultatBuilder.medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        behandlingsresultatBuilder.medRettenTil(RettenTil.HAR_RETT_TIL_FP);
        behandlingsresultatBuilder.medVedtaksbrev(Vedtaksbrev.AUTOMATISK);
        return behandlingsresultatBuilder.buildFor(revurdering);
    }
}
