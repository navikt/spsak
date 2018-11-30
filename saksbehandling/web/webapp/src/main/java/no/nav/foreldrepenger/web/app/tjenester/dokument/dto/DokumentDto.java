package no.nav.foreldrepenger.web.app.tjenester.dokument.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.Kommunikasjonsretning;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class DokumentDto {
    private JournalpostId journalpostId;
    private String dokumentId;
    private List<Long> behandlinger;
    private LocalDate tidspunkt;
    private String tittel;
    private Kommunikasjonsretning kommunikasjonsretning;
    private String gjelderFor;

    public DokumentDto() {
        // trengs for deserialisering av JSON
    }

    public DokumentDto(ArkivJournalPost arkivJournalPost, ArkivDokument arkivDokument) {
        this.journalpostId = arkivJournalPost.getJournalpostId();
        this.dokumentId = arkivDokument.getDokumentId();
        this.tidspunkt = arkivJournalPost.getTidspunkt();
        this.tittel = arkivDokument.getTittel();
        this.kommunikasjonsretning = arkivJournalPost.getKommunikasjonsretning();
        this.behandlinger = new ArrayList<>();
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public LocalDate getTidspunkt() {
        return tidspunkt;
    }

    public void setTidspunkt(LocalDate tidspunkt) {
        this.tidspunkt = tidspunkt;
    }

    public Kommunikasjonsretning getKommunikasjonsretning() {
        return kommunikasjonsretning;
    }

    public void setKommunikasjonsretning(Kommunikasjonsretning kommunikasjonsretning) {
        this.kommunikasjonsretning = kommunikasjonsretning;
    }

    public List<Long> getBehandlinger() {
        return behandlinger;
    }

    public void setBehandlinger(List<Long> behandlinger) {
        this.behandlinger = behandlinger;
    }

    public String getGjelderFor() {
        return gjelderFor;
    }

    public void setGjelderFor(String gjelderFor) {
        this.gjelderFor = gjelderFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DokumentDto that = (DokumentDto) o;
        return kommunikasjonsretning == that.kommunikasjonsretning &&
            Objects.equals(journalpostId, that.journalpostId) &&
            Objects.equals(dokumentId, that.dokumentId) &&
            Objects.equals(behandlinger, that.behandlinger) &&
            Objects.equals(tidspunkt, that.tidspunkt) &&
            Objects.equals(tittel, that.tittel) &&
            Objects.equals(gjelderFor, that.gjelderFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId, dokumentId, behandlinger, tidspunkt, tittel, kommunikasjonsretning, gjelderFor);
    }
}
