package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;

public interface BeregningRepository extends BehandlingslagerRepository {

    Sats finnEksaktSats(SatsType satsType, LocalDate dato);

    Optional<Beregning> getSisteBeregning(Long id);

    void lagreBeregning(Long behandlingId, Beregning nyBeregning);

    /**
     * Lagrer beregnignsresultat på behandling. Sørger for at samtidige oppdateringer på samme Behandling, eller andre Behandlinger
     * på samme Fagsak ikke kan gjøres samtidig.
     *
     * @see BehandlingLås
     */
    Long lagre(BeregningResultat beregningResultat, BehandlingLås lås);
}
