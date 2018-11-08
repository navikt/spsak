package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class ArkivJournalPost {
    private JournalpostId journalpostId;
    private Saksnummer saksnummer;
    private ArkivDokument hovedDokument;
    private List<ArkivDokument> andreDokument;
    private Kommunikasjonsretning kommunikasjonsretning;
    private String beskrivelse;
    private LocalDate tidspunkt;
    private String journalEnhet;

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public ArkivDokument getHovedDokument() {
        return hovedDokument;
    }

    public void setHovedDokument(ArkivDokument hovedDokument) {
        this.hovedDokument = hovedDokument;
    }

    public List<ArkivDokument> getAndreDokument() {
        return andreDokument;
    }

    public void setAndreDokument(List<ArkivDokument> andreDokument) {
        this.andreDokument = andreDokument;
    }

    public Kommunikasjonsretning getKommunikasjonsretning() {
        return kommunikasjonsretning;
    }

    public void setKommunikasjonsretning(Kommunikasjonsretning kommunikasjonsretning) {
        this.kommunikasjonsretning = kommunikasjonsretning;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public LocalDate getTidspunkt() {
        return tidspunkt;
    }

    public void setTidspunkt(LocalDate tidspunkt) {
        this.tidspunkt = tidspunkt;
    }

    public Optional<String> getJournalEnhet() {
        return Optional.ofNullable(journalEnhet);
    }

    public void setJournalEnhet(String journalEnhet) {
        this.journalEnhet = journalEnhet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArkivJournalPost that = (ArkivJournalPost) o;
        return Objects.equals(journalpostId, that.journalpostId) &&
            Objects.equals(saksnummer, that.saksnummer) &&
            Objects.equals(hovedDokument, that.hovedDokument) &&
            Objects.equals(andreDokument, that.andreDokument) &&
            Objects.equals(kommunikasjonsretning, that.kommunikasjonsretning) &&
            Objects.equals(beskrivelse, that.beskrivelse) &&
            Objects.equals(tidspunkt, that.tidspunkt);
    }

    @Override
    public int hashCode() {

        return Objects.hash(journalpostId, saksnummer, hovedDokument, andreDokument, kommunikasjonsretning, beskrivelse, tidspunkt);
    }

    public static class Builder {
        private final ArkivJournalPost arkivJournalPost;

        private Builder() {
            this.arkivJournalPost = new ArkivJournalPost();
            this.arkivJournalPost.setAndreDokument(new ArrayList<>());
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder medSaksnummer(Saksnummer saksnummer) {
            this.arkivJournalPost.setSaksnummer(saksnummer);
            return this;
        }

        public Builder medJournalpostId(JournalpostId journalpostId) {
            this.arkivJournalPost.setJournalpostId(journalpostId);
            return this;
        }

        public Builder medTidspunkt(LocalDate tidspunkt) {
            this.arkivJournalPost.setTidspunkt(tidspunkt);
            return this;
        }

        public Builder medBeskrivelse(String beskrivelse) {
            this.arkivJournalPost.setBeskrivelse(beskrivelse);
            return this;
        }

        public Builder medKommunikasjonsretning(Kommunikasjonsretning innUtNotat){
            this.arkivJournalPost.setKommunikasjonsretning(innUtNotat);
            return this;
        }

        public Builder medHoveddokument(ArkivDokument hovedDokument){
            this.arkivJournalPost.setHovedDokument(hovedDokument);
            return this;
        }

        public Builder medJournalFørendeEnhet(String enhet) {
            this.arkivJournalPost.setJournalEnhet(enhet);
            return this;
        }

        public Builder leggTillVedlegg(ArkivDokument vedlegg){
            this.arkivJournalPost.getAndreDokument().add(vedlegg);
            return this;
        }

        public ArkivJournalPost build() {
            return this.arkivJournalPost;
        }

    }

}
