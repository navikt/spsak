package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

public interface MottatteDokumentTjeneste {

    List<MottattDokument> hentMottatteDokumentVedlegg(Long behandlingId);

    boolean harMottattDokumentSet(Long behandlingId, Set<DokumentTypeId> dokumentTypeIdSet);

    boolean harMottattDokumentKat(Long behandlingId, DokumentKategori dokumentKategori);

    List<MottattDokument> hentMottatteDokument(Long behandlingId);

    Long lagreMottattDokumentPåFagsak(Long fagsakId, MottattDokument mottattDokument);

    void persisterDokumentinnhold(Behandling behandling, MottattDokument mottattDokument, Optional<LocalDate> gjelderFra);

    void oppdaterMottattDokumentMedBehandling(MottattDokument mottattDokument, Long behandlingId);

    Optional<MottattDokument> hentMottattDokument(Long mottattDokumentId);


    boolean erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(Fagsak sak);

    /**
     * Beregnes fra og med vedtaksdato
     */
    boolean harFristForInnsendingAvDokGåttUt(Fagsak sak);
}
