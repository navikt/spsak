package no.nav.foreldrepenger.dokumentbestiller;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;

class OpprettBehandling {

    private final static long sats = 1L;
    private final static long antallBarn = 1L;
    private final static long tilkjentYtelse = 2L;
    private final static LocalDateTime nå = LocalDateTime.now();

    static void genererBehandlingOgResultat(Behandling behandling) {
        Behandlingsresultat.builderForInngangsvilkår().buildFor(behandling);
        Beregning beregning = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
    }

    static void genererBehandlingOgResultat(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        Behandlingsresultat.builderForInngangsvilkår().medBehandlingResultatType(behandlingResultatType).buildFor(behandling);
        Beregning beregning = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
    }
}
