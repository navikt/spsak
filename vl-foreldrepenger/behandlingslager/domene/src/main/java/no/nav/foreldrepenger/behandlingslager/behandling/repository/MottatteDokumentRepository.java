package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;

public interface MottatteDokumentRepository extends BehandlingslagerRepository {

    MottattDokument lagre(MottattDokument mottattDokument);

    Optional<MottattDokument> hentMottattDokument(long mottattDokumentId);

    List<MottattDokument> hentMottatteDokument(long behandlingId);

    List<MottattDokument> hentMottatteDokumentMedFagsakId(long fagsakId);

    List<MottattDokument> hentMottatteDokumentMedForsendelseId(UUID forsendelseId);

    List<MottattDokument> hentMottatteDokumentVedleggPåBehandlingId(long behandlingId);

    // Henter alle dokument med type som ikke er søknad, endringssøknad, klage, IM (eller udefinert)
    List<MottattDokument> hentMottatteDokumentAndreTyperPåBehandlingId(long behandlingId);

    void oppdaterMedBehandling(MottattDokument mottattDokument, long behandlingId);
}
