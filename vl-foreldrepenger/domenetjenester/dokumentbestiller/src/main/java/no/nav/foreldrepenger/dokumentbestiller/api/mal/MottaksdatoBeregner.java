package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;

public class MottaksdatoBeregner {


    private MottaksdatoBeregner() {
        // Sonar - Add a private constructor to hide the implicit public one (for utility classes)
    }

    /**
     * Bestem søknadstidspunktet ut fra strukturert søknad eller mottatt søknadsdokument
     * Dersom intet søknadsdokument finnes, defaultes dato til første mottatte dokument
     * @param mottatteDokumentRepository
     * @param søknadOpt
     * @param id
     */
    public static LocalDate finnSøknadsdato(MottatteDokumentRepository mottatteDokumentRepository, Optional<Søknad> søknadOpt, Long id) {
        if (søknadOpt.isPresent()) {
            Søknad søknad = søknadOpt.get();
            if (søknad.getMottattDato() != null) {
                return søknad.getMottattDato();
            }
            if (søknad.getSøknadsdato() != null) {
                return søknad.getSøknadsdato();
            }
        }
        // Ustrukturerte søknader eller andre typer dokument enn søknad har ikke opprettet en søknad på behandlingen ennå
        Optional<MottattDokument> førstMottatteDokument = Stream.of(
            finnSøknadsdokumentForBehandling(id, mottatteDokumentRepository), // 1. Søk søknadsdokument
            finnTidligstMottatteDokument(id, mottatteDokumentRepository)) // 2. Dersom ingen søknad - søk alle andre typer dokument
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
        if (!førstMottatteDokument.isPresent()) {
            throw new IllegalStateException("Skal være mulig å utlede mottattdato");
        }
        return førstMottatteDokument.get().getMottattDato();
    }

    private static Optional<MottattDokument> finnSøknadsdokumentForBehandling(Long behandlingId, MottatteDokumentRepository mottatteDokumentRepository) {
        return finnDokumentForBehandling(behandlingId, mottatteDokumentRepository, DokumentTypeId.getSøknadTyper());
    }

    private static Optional<MottattDokument> finnTidligstMottatteDokument(Long behandlingId, MottatteDokumentRepository mottatteDokumentRepository) {
        List<MottattDokument> mottatteDokumenter = mottatteDokumentRepository.hentMottatteDokument(behandlingId);
        return mottatteDokumenter.stream()
            .min(Comparator.comparing(MottattDokument::getMottattDato));
    }

    private static Optional<MottattDokument> finnKlagedokumentForBehandling(Long behandlingId, MottatteDokumentRepository mottatteDokumentRepository) {
        return finnDokumentForBehandling(behandlingId, mottatteDokumentRepository, Collections.singleton(DokumentTypeId.KLAGE_DOKUMENT));
    }

    private static Optional<MottattDokument> finnDokumentForBehandling(Long behandlingId, MottatteDokumentRepository mottatteDokumentRepository,
            Set<DokumentTypeId> søknadsdokumenter) {
        List<MottattDokument> mottatteDokumenter = mottatteDokumentRepository.hentMottatteDokument(behandlingId);
        return mottatteDokumenter.stream().filter(dok -> søknadsdokumenter.contains(dok.getDokumentTypeId())).findFirst();
    }

    /**
     * Bestem klagetidspunktet ut fra mottatt klagedokument
     * @param mottatteDokumentRepository
     */
    public static Optional<LocalDate> finnKlagedato(Behandling behandling, MottatteDokumentRepository mottatteDokumentRepository) {
        if (BehandlingType.KLAGE.equals(behandling.getType())) {
            Optional<MottattDokument> klagedokument = finnKlagedokumentForBehandling(behandling.getId(), mottatteDokumentRepository);
            if (klagedokument.isPresent()) {
                return Optional.of(klagedokument.get().getMottattDato());
            }
        }
        return Optional.empty();
    }



}
