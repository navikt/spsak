package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;

public class RyddUttakTjeneste {

    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider;

    public RyddUttakTjeneste(Behandling behandling, BehandlingRepositoryProvider repositoryProvider) {
        this.behandling = behandling;
        this.repositoryProvider = repositoryProvider;
    }

    public void ryddAvklarteUttaksperioder() {
        YtelsesFordelingRepository ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        ytelsesFordelingRepository.tilbakestillOverstyringOgDokumentasjonsperioder(behandling);
    }

    public void ryddUttaksresultat() {
        repositoryProvider.getUttakRepository().deaktivterAktivtResultat(behandling);
    }
}
