package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;

public interface BeregningsgrunnlagRepository extends BehandlingslagerRepository {

    Beregningsgrunnlag hentAggregat(Behandling behandling);

    Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Behandling behandling);

    Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Long beregningsgrunnlagId);

    /**
     * Henter aktivt BeregningsgrunnlagGrunnlagEntitet
     * @param behandling en {@link Behandling}
     * @return Hvis det finnes en aktiv {@link BeregningsgrunnlagGrunnlagEntitet} returneres denne
     */
    Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitet(Behandling behandling);

    /**
     * Henter siste {@link BeregningsgrunnlagGrunnlagEntitet} opprettet i et bestemt steg. Ignorerer om grunnlaget er aktivt eller ikke.
     * @param behandling en {@link Behandling}
     * @param beregningsgrunnlagTilstand steget {@link BeregningsgrunnlagGrunnlagEntitet} er opprettet i
     * @return Hvis det finnes et eller fler BeregningsgrunnlagGrunnlagEntitet som har blitt opprettet i {@code stegOpprettet} returneres den som ble opprettet sist
     */
    Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand);

    long lagre(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand);

    void deaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling);

    void reaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand);

    void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);
}
