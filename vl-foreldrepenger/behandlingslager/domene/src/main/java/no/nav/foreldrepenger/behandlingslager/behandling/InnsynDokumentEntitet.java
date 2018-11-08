package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "InnsynDokument")
@Table(name = "INNSYN_DOKUMENT")
public class InnsynDokumentEntitet extends BaseEntitet implements InnsynDokument, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNSYN_DOKUMENT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "innsyn_id", nullable = false, updatable = false)
    private InnsynEntitet innsyn;

    @Column(name = "journalpost_id", nullable = false)
    private JournalpostId journalpostId;

    @Column(name = "dokument_id", nullable = false)
    private String dokumentId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "fikk_innsyn", nullable = false)
    private boolean fikkInnsyn;

    @SuppressWarnings("unused")
    private InnsynDokumentEntitet() {
        // for hibernate
    }

    public InnsynDokumentEntitet(boolean fikkInnsyn, JournalpostId journalpostId, String dokumentId) {
        this.fikkInnsyn = fikkInnsyn;
        this.journalpostId = journalpostId;
        this.dokumentId = dokumentId;
    }
    
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(journalpostId, dokumentId);
    }

    @Override
    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    @Override
    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    @Override
    public boolean isFikkInnsyn() {
        return fikkInnsyn;
    }

    public void setFikkInnsyn(boolean fikkInnsyn) {
        this.fikkInnsyn = fikkInnsyn;
    }

    public InnsynEntitet getInnsyn() {
        return innsyn;
    }

    public void setInnsyn(InnsynEntitet innsyn) {
        this.innsyn = innsyn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InnsynDokumentEntitet)) {
            return false;
        }
        InnsynDokumentEntitet that = (InnsynDokumentEntitet) o;
        return Objects.equals(journalpostId, that.journalpostId) &&
            Objects.equals(dokumentId, that.dokumentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId, dokumentId);
    }

    @Override
    public String toString() {
        return "InnsynDokumentEntitet{" +
            "id=" + id +
            ", journalpostId='" + journalpostId + '\'' +
            ", dokumentId='" + dokumentId + '\'' +
            ", fikkInnsyn=" + fikkInnsyn +
            '}';
    }
}
