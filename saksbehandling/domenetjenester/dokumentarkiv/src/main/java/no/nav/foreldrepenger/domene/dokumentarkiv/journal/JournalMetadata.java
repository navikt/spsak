package no.nav.foreldrepenger.domene.dokumentarkiv.journal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class JournalMetadata<T extends DokumentTypeId> {

    public enum Journaltilstand {
        MIDLERTIDIG,
        UTGAAR,
        ENDELIG
    }

    private JournalpostId journalpostId;
    private String dokumentId;
    private VariantFormat variantFormat;
    private MottakKanal mottakKanal;
    private Journaltilstand journaltilstand;
    private T dokumentType;
    private DokumentKategori dokumentKategori;
    private ArkivFilType arkivFilType;
    private boolean erHoveddokument;
    private LocalDate forsendelseMottatt;
    private List<String> brukerIdentListe;

    private JournalMetadata() {
        // skjult
    }

    public static <T extends DokumentTypeId> Builder<T> builder() {
        return new Builder<>();
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
    }

    public MottakKanal getMottakKanal() {
        return mottakKanal;
    }

    public T getDokumentType() {
        return dokumentType;
    }

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
    }

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public Journaltilstand getJournaltilstand() {
        return journaltilstand;
    }

    public boolean getErHoveddokument() {
        return erHoveddokument;
    }

    public List<String> getBrukerIdentListe() {
        if (brukerIdentListe == null) {
            brukerIdentListe = new ArrayList<>();
        }
        return brukerIdentListe;
    }

    public LocalDate getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public static class Builder<T extends DokumentTypeId> {
        private JournalpostId journalpostId;
        private String dokumentId;
        private VariantFormat variantFormat;
        private MottakKanal mottakKanal;
        private T dokumentType;
        private DokumentKategori dokumentKategori;
        private ArkivFilType arkivFilType;
        private Journaltilstand journaltilstand;
        private boolean erHoveddokument;
        private LocalDate forsendelseMottatt;
        private List<String> brukerIdentListe;

        public Builder<T> medJournalpostId(JournalpostId journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder<T> medDokumentId(String dokumentId) {
            this.dokumentId = dokumentId;
            return this;
        }

        public Builder<T> medVariantFormat(VariantFormat variantFormat) {
            this.variantFormat = variantFormat;
            return this;
        }

        public Builder<T> medMottakKanal(MottakKanal mottakKanal) {
            this.mottakKanal = mottakKanal;
            return this;
        }

        public Builder<T> medDokumentType(T dokumentType) {
            this.dokumentType = dokumentType;
            return this;
        }

        public Builder<T> medDokumentKategori(DokumentKategori dokumentKategori) {
            this.dokumentKategori = dokumentKategori;
            return this;
        }

        public Builder<T> medArkivFilType(ArkivFilType arkivFilType) {
            this.arkivFilType = arkivFilType;
            return this;
        }

        public Builder<T> medJournaltilstand(Journaltilstand journaltilstand) {
            this.journaltilstand = journaltilstand;
            return this;
        }

        public Builder<T> medErHoveddokument(boolean erHoveddokument) {
            this.erHoveddokument = erHoveddokument;
            return this;
        }

        public Builder<T> medForsendelseMottatt(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public Builder<T> medBrukerIdentListe(List<String> brukerIdentListe) {
            this.brukerIdentListe = brukerIdentListe;
            return this;
        }

        public JournalMetadata<T> build() {
            JournalMetadata<T> jmd = new JournalMetadata<>();
            jmd.journalpostId = this.journalpostId;
            jmd.dokumentId = this.dokumentId;
            jmd.variantFormat = this.variantFormat;
            jmd.mottakKanal = this.mottakKanal;
            jmd.dokumentType = this.dokumentType;
            jmd.dokumentKategori = this.dokumentKategori;
            jmd.arkivFilType = this.arkivFilType;
            jmd.journaltilstand = this.journaltilstand;
            jmd.erHoveddokument = this.erHoveddokument;
            jmd.forsendelseMottatt = this.forsendelseMottatt;
            jmd.brukerIdentListe = this.brukerIdentListe;
            return jmd;
        }
    }
}
