package no.nav.foreldrepenger.dokumentbestiller;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

@ApplicationScoped
public class BrevHistorikkinnslag {
    private DokumentDataTjeneste dokumentDataTjeneste;
    private HistorikkRepository historikkRepository;

    public BrevHistorikkinnslag() {
        // for cdi proxy
    }

    @Inject
    public BrevHistorikkinnslag(DokumentDataTjeneste dokumentDataTjeneste, HistorikkRepository historikkRepository) {
        this.dokumentDataTjeneste = dokumentDataTjeneste;
        this.historikkRepository = historikkRepository;
    }

    public void opprettHistorikkinnslagForSendtBrev(HistorikkAktør historikkAktør,
                                                    String dokumentBegrunnelse,
                                                    DokumentData dokumentData,
                                                    String dokumentId,
                                                    JournalpostId journalpostId) {
        HistorikkinnslagType historikkinnslagType = HistorikkinnslagType.BREV_SENT;
        opprettHistorikkinnslag(historikkAktør, dokumentBegrunnelse, dokumentData, dokumentId, journalpostId, historikkinnslagType);
    }

    public void opprettHistorikkinnslagForBestiltBrev(HistorikkAktør historikkAktør, String dokumentBegrunnelse, DokumentData dokumentData) {
        HistorikkinnslagType historikkinnslagType = HistorikkinnslagType.BREV_BESTILT;
        opprettHistorikkinnslag(historikkAktør, dokumentBegrunnelse, dokumentData, null, null, historikkinnslagType);
    }

    private void opprettHistorikkinnslag(HistorikkAktør historikkAktør,
                                         String dokumentBegrunnelse,
                                         DokumentData dokumentData,
                                         String dokumentId,
                                         JournalpostId journalpostId,
                                         HistorikkinnslagType historikkinnslagType) {
        DokumentMalType dokumentMalType = dokumentDataTjeneste.hentDokumentMalType(dokumentData.getDokumentMalType().getKode());
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setBehandling(dokumentData.getBehandling());
        historikkinnslag.setAktør(historikkAktør);
        historikkinnslag.setType(historikkinnslagType);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder().medHendelse(historikkinnslagType);
        if (dokumentBegrunnelse != null) {
            builder.medBegrunnelse(dokumentBegrunnelse);
        } else if (HistorikkinnslagType.BREV_BESTILT.equals(historikkinnslagType)) {
            builder.medBegrunnelse(dokumentMalType.getNavn());
        }
        builder.build(historikkinnslag);

        if (HistorikkinnslagType.BREV_SENT.equals(historikkinnslagType)) {
            HistorikkinnslagDokumentLink dokumentLink = new HistorikkinnslagDokumentLink();
            dokumentLink.setHistorikkinnslag(historikkinnslag);
            dokumentLink.setDokumentId(dokumentId);
            dokumentLink.setJournalpostId(journalpostId);
            dokumentLink.setLinkTekst(dokumentMalType.getNavn());
            historikkinnslag.setDokumentLinker(Collections.singletonList(dokumentLink));
        }
        historikkRepository.lagre(historikkinnslag);
    }
}
