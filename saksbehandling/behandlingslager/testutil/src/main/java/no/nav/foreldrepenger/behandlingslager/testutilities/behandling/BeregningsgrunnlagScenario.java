package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagScenario implements TestScenarioResultatTillegg {

    private Beregningsgrunnlag.Builder beregningsgrunnlagBuilder;

    BeregningsgrunnlagScenario() {
        this.beregningsgrunnlagBuilder = Beregningsgrunnlag.builder();
    }

    public Beregningsgrunnlag.Builder getBeregningsgrunnlagBuilder() {
        return beregningsgrunnlagBuilder;
    }

    @Override
    public void lagre(Behandling behandling, ResultatRepositoryProvider repositoryProvider) {
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagBuilder.build();
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
    }
}
