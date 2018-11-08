package no.nav.foreldrepenger.dokumentbestiller.api;

import java.util.List;
import java.util.function.Predicate;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BrevmalDto;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public interface DokumentBestillerApplikasjonTjeneste {
    byte[] forhandsvisDokument(Long dokumentDataId);

    void produserDokument(Long dokumentDataId, HistorikkAktør aktør, String dokumentBegrunnelse);

    void dokumentForhandsvist(Long dokumentDataId);

    byte[] forhandsvisVedtaksbrev (BestillVedtakBrevDto dto, Predicate<Behandling> revurderingMedUendretUtfall);

    void produserVedtaksbrev(BehandlingVedtak behandlingVedtak);

    List<String> hentMottakere(Long behandlingId);

    Long lagreDokumentdata(BestillBrevDto bestillBrevDto);

    byte[] hentForhåndsvisningDokument(BestillBrevDto bestillBrevDto);

    boolean erDokumentProdusert(Long behandlingId, String dokumentMalTypeKode);

    Long bestillDokument(BestillBrevDto bestillBrevDto, HistorikkAktør saksbehandler, String begrunnelse);

    Long bestillDokument(BestillBrevDto bestillBrevDto, HistorikkAktør saksbehandler);

    List<BrevmalDto> hentBrevmalerFor(Long behandlingId);

    void knyttVedleggTilForsendelse(JournalpostId knyttesTilJournalpostId, JournalpostId knyttesFraJournalpostId, String dokumentId, String endretAvNavn);

    void ferdigstillForsendelse(JournalpostId journalpostId, String endretAvNavn);

    void settBehandlingPåVent(Long behandlingId, Venteårsak venteårsak);

}
