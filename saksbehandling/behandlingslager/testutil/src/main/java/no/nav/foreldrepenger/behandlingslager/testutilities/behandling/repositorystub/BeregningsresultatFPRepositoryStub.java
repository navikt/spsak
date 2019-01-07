package no.nav.foreldrepenger.behandlingslager.testutilities.behandling.repositorystub;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;

public class BeregningsresultatFPRepositoryStub implements BeregningsresultatRepository {

    private static final String IKKE_STOTTET = "Ikke støttet av BeregningsresultatFPRepositoryStub";
    private Map<Behandlingsresultat, BeregningsresultatPerioder> map = new HashMap<>();

    @Override
    public Optional<BeregningsresultatPerioder> hentHvisEksisterer(Behandling behandling) {
        return map.containsKey(behandling.getBehandlingsresultat()) ? Optional.of(map.get(behandling.getBehandlingsresultat())) : Optional.empty();
    }

    @Override
    public Optional<BeregningsResultat> hentHvisEksistererFor(Behandlingsresultat behandlingsresultat) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

    @Override
    public long lagre(Behandlingsresultat behandlingsresultat, BeregningsresultatPerioder beregningsresultat) {
        map.put(behandlingsresultat, beregningsresultat);
        return 0;
    }

    @Override
    public void deaktiverBeregningsresultat(Behandling behandling, BehandlingLås skriveLås) {
        throw new UnsupportedOperationException(IKKE_STOTTET);
    }

}
