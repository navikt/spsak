package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;

@ApplicationScoped
public class SøknadDtoTjenesteImpl implements SøknadDtoTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;

    SøknadDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public SøknadDtoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public Optional<SoknadDto> mapFra(Behandling behandling) {
        Optional<Søknad> søknadOpt = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling);
        if (søknadOpt.isPresent()) {
            søknadOpt.get();
        }
        return Optional.empty();
    }

}
