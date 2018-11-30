package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFPKobling;

public interface BeregningsresultatFPRepository extends BehandlingslagerRepository {

    Optional<BeregningsresultatFP> hentBeregningsresultatFP(Behandling behandling);

    Optional<BeregningsresultatFPKobling> hentBeregningsresultatFPKobling(Behandling behandling);

    long lagre(Behandling behandling, BeregningsresultatFP beregningsresultatFP);

    void deaktiverBeregningsresultatFP(Behandling behandling, BehandlingLås skriveLås);
}
