package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Objects;
import java.util.Optional;

public class BehandlingUtil {

    public static boolean erSaksbehandlingAvsluttet(Behandling behandling, Behandlingsresultat behandlingsresultat) {
        Objects.requireNonNull(behandling, "Behandling");
        Optional<Behandlingsresultat> resultat = Optional.ofNullable(behandlingsresultat);

        return (behandling.erAvsluttet() || behandling.erUnderIverksettelse() || resultat.map(Behandlingsresultat::isBehandlingHenlagt).orElse(false));
    }
}
