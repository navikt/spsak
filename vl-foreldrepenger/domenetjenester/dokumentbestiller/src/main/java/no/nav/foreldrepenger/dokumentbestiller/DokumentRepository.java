package no.nav.foreldrepenger.dokumentbestiller;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;

public interface DokumentRepository extends BehandlingslagerRepository {
    Long lagre(DokumentData dokumentData);

    Long lagre(DokumentAdresse adresse);

    DokumentData hentDokumentData(Long dokumentDataId);

    List<DokumentMalType> hentAlleDokumentMalTyper();

    DokumentMalType hentDokumentMalType(String kode);

    List<DokumentData> hentDokumentDataListe(Long behandlingId, String dokumentmal);
}
