package no.nav.foreldrepenger.behandlingslager.testutilities.behandling.repositorystub;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagRepositoryStub implements BeregningsgrunnlagRepository {
    private Map<Behandling, Beregningsgrunnlag> map = new HashMap<>();
    private static final String IKKE_STOTTET = "Ikke støttet av BeregningsgrunnlagRepositoryStub";

    @Override
    public Beregningsgrunnlag hentAggregat(Behandling behandling) {
        return map.get(behandling);
    }

    @Override
    public Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Behandling behandling) {
        return map.containsKey(behandling) ? Optional.of(map.get(behandling)) : Optional.empty();
    }

    @Override
    public Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Long beregningsgrunnlagId) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

    @Override
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitet(Behandling behandling) {
        return hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    @Override
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        return map.containsKey(behandling) ?
            Optional.of(new BeregningsgrunnlagGrunnlagEntitet(behandling, map.get(behandling), beregningsgrunnlagTilstand)) :
            Optional.empty();
    }

    @Override
    public long lagre(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        map.put(behandling, beregningsgrunnlag);
        return 0;
    }

    @Override
    public void deaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling) {
        if (map.containsKey(behandling)) {
            map.remove(behandling);
        }
    }

    @Override
    public void reaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        if (map.containsKey(gammelBehandling)) {
            map.put(nyBehandling, map.get(gammelBehandling));
        }
    }
    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling){
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }
}
