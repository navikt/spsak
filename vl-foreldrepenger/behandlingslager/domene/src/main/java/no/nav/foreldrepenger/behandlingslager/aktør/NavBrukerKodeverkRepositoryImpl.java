package no.nav.foreldrepenger.behandlingslager.aktør;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

/** TODO (FC): fjern denne, unødvendig adapter. */
@ApplicationScoped
public class NavBrukerKodeverkRepositoryImpl implements NavBrukerKodeverkRepository {

    private KodeverkRepository kodeverkRepository;

    NavBrukerKodeverkRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public NavBrukerKodeverkRepositoryImpl(KodeverkRepository kodeverkRepository){
       Objects.requireNonNull(kodeverkRepository, "kodeverkRepository");
       this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public Optional<RelasjonsRolleType> finnBrukerRolle(String kode) {
        return kodeverkRepository.finnOptional(RelasjonsRolleType.class, kode);
    }

    @Override
    public NavBrukerKjønn finnBrukerKjønn(String kode) {
        return kodeverkRepository.finn(NavBrukerKjønn.class, kode);
    }

    @Override
    public PersonstatusType finnPersonstatus(String kode) {
        return kodeverkRepository.finn(PersonstatusType.class, kode);
    }
}
