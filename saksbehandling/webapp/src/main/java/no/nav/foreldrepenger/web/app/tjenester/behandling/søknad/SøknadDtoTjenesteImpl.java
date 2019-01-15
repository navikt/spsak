package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;

@ApplicationScoped
public class SøknadDtoTjenesteImpl implements SøknadDtoTjeneste {

    private GrunnlagRepositoryProvider repositoryProvider;

    SøknadDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public SøknadDtoTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public Optional<SoknadDto> mapFra(Behandling behandling) {
        Optional<Søknad> søknadOpt = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling);
        if (søknadOpt.isPresent()) {
            Søknad søknad = søknadOpt.get();
            SoknadSykepengerDto søknadDto = new SoknadSykepengerDto();
            søknadDto.setMottattDato(søknad.getMottattDato());
            søknadDto.setTilleggsopplysninger(søknad.getTilleggsopplysninger());
            søknadDto.setSykemeldingReferanse(søknad.getSykemeldingReferanse());
            søknadDto.setSøknadReferanse(søknad.getSøknadReferanse());

            return Optional.of(søknadDto);
        }
        return Optional.empty();
    }

}
