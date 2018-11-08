package no.nav.foreldrepenger.domene.uttak;

import static java.lang.Boolean.TRUE;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;

public final class UttakOmsorgUtil {

    private UttakOmsorgUtil() {
    }

    public static boolean harAleneomsorg(YtelseFordelingAggregat ytelseFordelingAggregat) {
        Optional<PerioderAleneOmsorg> perioderAleneOmsorg = ytelseFordelingAggregat.getPerioderAleneOmsorg();
        if (perioderAleneOmsorg.isPresent()) {
            return !perioderAleneOmsorg.get().getPerioder().isEmpty();
        }
        return TRUE.equals(ytelseFordelingAggregat.getOppgittRettighet().getHarAleneomsorgForBarnet());
    }

    public static boolean harSøkerRett(Behandling behandling) {
        return !behandling.getBehandlingsresultat().isVilkårAvslått();
    }

    public static boolean harAnnenForelderRett(OppgittRettighet oppgittRettighet) {
        Objects.requireNonNull(oppgittRettighet, "oppgittRettighet");
        return oppgittRettighet.getHarAnnenForeldreRett() == null || oppgittRettighet.getHarAnnenForeldreRett();
    }
}
