package no.nav.foreldrepenger.behandling.historikk;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDokumentLink;

public class HistorikkInnslagDokumentLinkDto {

    private String tag;
    private URI url;

    private String journalpostId;
    private String dokumentId;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = URI.create(url);
    }

    static List<HistorikkInnslagDokumentLinkDto> mapFra(List<HistorikkinnslagDokumentLink> dokumentLinkList) {
        return dokumentLinkList.stream().map(HistorikkInnslagDokumentLinkDto::mapFra).collect(Collectors.toList());
    }

    private static HistorikkInnslagDokumentLinkDto mapFra(HistorikkinnslagDokumentLink dokumentLink) {
        HistorikkInnslagDokumentLinkDto dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(dokumentLink.getLinkTekst());
        dto.setDokumentId(dokumentLink.getDokumentId());
        dto.setJournalpostId(dokumentLink.getJournalpostId().getVerdi());
        return dto;
    }
}
