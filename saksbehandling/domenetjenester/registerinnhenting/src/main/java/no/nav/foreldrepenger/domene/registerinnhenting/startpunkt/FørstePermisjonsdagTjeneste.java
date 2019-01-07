package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;

@ApplicationScoped
public class FørstePermisjonsdagTjeneste {

    private UttakRepository uttakRepository;

    FørstePermisjonsdagTjeneste() {
        //CDI proxy
    }

    @Inject
    public FørstePermisjonsdagTjeneste(ResultatRepositoryProvider repositoryProvider) {
        this.uttakRepository = repositoryProvider.getUttakRepository();
    }

    public Optional<LocalDate> henteFørstePermisjonsdag(Behandling behandling) {
        List<UttakResultatPeriodeEntitet> uttakPerioder = uttakRepository.hentUttakResultat(behandling).getGjeldendePerioder().getPerioder();
        return uttakPerioder.stream()
            .min(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
            .map(UttakResultatPeriodeEntitet::getFom);
    }

}
