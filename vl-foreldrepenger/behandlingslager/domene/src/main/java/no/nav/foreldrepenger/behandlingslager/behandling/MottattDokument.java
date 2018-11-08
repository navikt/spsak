package no.nav.foreldrepenger.behandlingslager.behandling;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for mottatte dokument.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */

@Entity(name = "MottattDokument")
@Table(name = "MOTTATT_DOKUMENT")
public class MottattDokument extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MOTTATT_DOKUMENT")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "journalpostId", column = @Column(name = "journalpost_id")))
    private JournalpostId journalpostId;

    @Column(name = "forsendelse_id")
    private UUID forsendelseId;

    @Column(name = "dokument_id", updatable = false)
    private String dokumentId;

    @Column(name = "variantformat")
    private String variantFormat;

    @Column(name = "journal_enhet")
    private String journalEnhet;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + DokumentTypeId.DISCRIMINATOR + "'"))
    private DokumentTypeId dokumentTypeId = DokumentTypeId.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "dokument_kategori", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + DokumentKategori.DISCRIMINATOR + "'"))
    private DokumentKategori dokumentKategori = DokumentKategori.UDEFINERT;

    @Column(name = "behandling_id", updatable = false)
    private Long behandlingId;

    @Column(name = "mottatt_dato")
    private LocalDate mottattDato;

    @Lob
    @Column(name = "xml_payload")
    private String xmlPayload;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "elektronisk_registrert", nullable = false)
    private boolean elektroniskRegistrert;

    @Column(name = "fagsak_id", nullable = false)
    private Long fagsakId;

    MottattDokument() {
        // Hibernate
    }

    MottattDokument(MottattDokument mottatteDokument) {
        this.journalpostId = mottatteDokument.journalpostId;
        this.dokumentId = mottatteDokument.dokumentId;
        this.variantFormat = mottatteDokument.variantFormat;
        this.dokumentTypeId = mottatteDokument.dokumentTypeId;
        this.dokumentKategori = mottatteDokument.dokumentKategori;
        this.behandlingId = mottatteDokument.behandlingId;
        this.elektroniskRegistrert = mottatteDokument.elektroniskRegistrert;
        this.fagsakId = mottatteDokument.fagsakId;
        this.forsendelseId = mottatteDokument.forsendelseId;
        this.mottattDato = mottatteDokument.mottattDato;
        this.xmlPayload = mottatteDokument.xmlPayload;
        this.journalEnhet = mottatteDokument.journalEnhet;
    }

    public Long getId() {
        return id;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public String getVariantFormat() {
        return variantFormat;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
    }

    public Optional<String> getJournalEnhet() {
        return Optional.ofNullable(journalEnhet);
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public String getPayloadXml() {
        return xmlPayload;
    }

    public boolean getElektroniskRegistrert() {
        return elektroniskRegistrert;
    }

    void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    void setVariantFormat(String variantFormat) {
        this.variantFormat = variantFormat;
    }

    void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        this.dokumentTypeId = dokumentTypeId;
    }

    void setDokumentKategori(DokumentKategori dokumentKategori) {
        this.dokumentKategori = dokumentKategori;
    }

    void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    void setElektroniskRegistrert(boolean elektroniskRegistrert) {
        this.elektroniskRegistrert = elektroniskRegistrert;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    void setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public void setForsendelseId(UUID forsendelseId) {
        this.forsendelseId = forsendelseId;
    }

    public void setJournalEnhet(String enhet) {
        this.journalEnhet = enhet;
    }

    public static class Builder {
        private MottattDokument mottatteDokumentMal;

        public Builder() {
            mottatteDokumentMal = new MottattDokument();
        }

        public Builder(MottattDokument mottatteDokument) {
            if (mottatteDokument != null) {
                mottatteDokumentMal = new MottattDokument(mottatteDokument);
            } else {
                mottatteDokumentMal = new MottattDokument();
            }
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
            mottatteDokumentMal.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public Builder medDokumentKategori(DokumentKategori dokumentKategori) {
            mottatteDokumentMal.dokumentKategori = dokumentKategori;
            return this;
        }

        public Builder medJournalPostId(JournalpostId journalPostId) {
            mottatteDokumentMal.journalpostId = journalPostId;
            return this;
        }

        public Builder medDokumentId(String dokumentId) {
            mottatteDokumentMal.dokumentId = dokumentId;
            return this;
        }

        public Builder medVariantFormat(String variantFormat) {
            mottatteDokumentMal.variantFormat = variantFormat;
            return this;
        }

        public Builder medJournalFørendeEnhet(String journalEnhet) {
            mottatteDokumentMal.journalEnhet = journalEnhet;
            return this;
        }

        public Builder medBehandlingId(Long behandlingId) {
            mottatteDokumentMal.behandlingId = behandlingId;
            return this;
        }

        public Builder medMottattDato(LocalDate mottattDato) {
            mottatteDokumentMal.mottattDato = mottattDato;
            return this;
        }

        public Builder medXmlPayload(String xmlPayload) {
            mottatteDokumentMal.xmlPayload = xmlPayload;
            return this;
        }

        public Builder medElektroniskRegistrert(boolean elektroniskRegistrert) {
            mottatteDokumentMal.elektroniskRegistrert = elektroniskRegistrert;
            return this;
        }

        public Builder medFagsakId(Long fagsakId) {
            mottatteDokumentMal.fagsakId = fagsakId;
            return this;
        }

        public Builder medForsendelseId(UUID forsendelseId) {
            mottatteDokumentMal.forsendelseId = forsendelseId;
            return this;
        }

        public Builder medId(Long mottattDokumentId) {
            mottatteDokumentMal.id = mottattDokumentId;
            return this;
        }

        public MottattDokument build() {
            Objects.requireNonNull(mottatteDokumentMal.fagsakId, "Trenger fagsak id for å opprette MottatteDokument.");
            return mottatteDokumentMal;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof MottattDokument)) {
            return false;
        }
        MottattDokument other = (MottattDokument) obj;
        return Objects.equals(this.dokumentId, other.dokumentId)
            && Objects.equals(this.dokumentTypeId, other.dokumentTypeId)
            && Objects.equals(this.dokumentKategori, other.dokumentKategori)
            && Objects.equals(this.journalpostId, other.journalpostId)
            && Objects.equals(this.variantFormat, other.variantFormat)
            && Objects.equals(this.xmlPayload, other.xmlPayload)
            && Objects.equals(this.elektroniskRegistrert, other.elektroniskRegistrert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dokumentId, dokumentTypeId, dokumentKategori, journalpostId, variantFormat, xmlPayload, elektroniskRegistrert);
    }
}
