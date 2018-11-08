package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFPKobling;

public class BeregningsresultatFPRepositoryStub implements BeregningsresultatFPRepository {

    private static final String IKKE_STOTTET = "Ikke støttet av BeregningsresultatFPRepositoryStub";
    private Map<Behandling, BeregningsresultatFP> map = new HashMap<>();

    @Override
    public Optional<BeregningsresultatFP> hentBeregningsresultatFP(Behandling behandling) {
        return map.containsKey(behandling) ? Optional.of(map.get(behandling)) : Optional.empty();
    }

    @Override
    public Optional<BeregningsresultatFPKobling> hentBeregningsresultatFPKobling(Behandling behandling) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

    @Override
    public long lagre(Behandling behandling, BeregningsresultatFP beregningsresultatFP) {
        map.put(behandling, beregningsresultatFP);
        return 0;
    }

    @Override
    public void deaktiverBeregningsresultatFP(Behandling behandling, BehandlingLås skriveLås) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

}
