package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "Journalpost")
@Table(name = "JOURNALPOST")
public class Journalpost extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOURNALPOST")
    private Long id;

    @Column(name = "journalpost_id", nullable = false, unique = true)
    private JournalpostId journalpostId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fagsak_id", nullable = false)
    private Fagsak fagsak;

    Journalpost() {
    }

    public Journalpost(JournalpostId journalpostId, Fagsak fagsak) {
        this.journalpostId = Objects.requireNonNull(journalpostId, "journalpostId er påkrevd");
        this.fagsak = Objects.requireNonNull(fagsak, "fagsak er påkrevd");
    }

    public Journalpost(String journalpostId, Fagsak fagsak) {
        this.journalpostId = new JournalpostId(Objects.requireNonNull(journalpostId, "journalpostId er påkrevd"));
        this.fagsak = Objects.requireNonNull(fagsak, "fagsak er påkrevd");
    }


    public Long getId() {
        return id;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public Fagsak getFagsak() {
        return fagsak;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Journalpost)) {
            return false;
        }
        Journalpost other = (Journalpost) obj;

        return Objects.equals(this.journalpostId, other.journalpostId)
                && Objects.equals(this.fagsak, other.fagsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(journalpostId, journalpostId);
    }
}
