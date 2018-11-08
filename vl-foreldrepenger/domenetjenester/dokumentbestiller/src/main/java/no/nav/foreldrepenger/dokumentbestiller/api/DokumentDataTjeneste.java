package no.nav.foreldrepenger.dokumentbestiller.api;

import java.util.Collection;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;

public interface DokumentDataTjeneste {

    Long lagreDokumentData(Long behandlingId, DokumentType dokumentMal);

    DokumentData hentDokumentData(Long dokumentDataId);

    void oppdaterDokumentData(DokumentData dokumentData);

    DokumentMalType hentDokumentMalType(String kode);

    Collection<DokumentMalType> hentAlleDokumentMalTyper();

    List<DokumentData> hentDokumentDataListe(Long behandlingId, String kode);

    void opprettDokumentBestillerTask(Long dokumentDataId, HistorikkAktør aktør, String dokumentBegrunnelse);

    BrevParametere getBrevParametere();
}
