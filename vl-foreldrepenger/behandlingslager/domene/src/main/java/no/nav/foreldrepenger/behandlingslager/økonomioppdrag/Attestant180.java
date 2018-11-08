package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity(name = "Attestant180")
@Table(name = "OKO_ATTESTANT_180")
public class Attestant180 extends BaseEntitet{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_ATTESTANT_180")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "attestant_id", nullable = false)
    private String attestantId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrags_linje_150_id", nullable = false, updatable=false)
    private Oppdragslinje150 oppdragslinje150;

    public Attestant180() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttestantId() {
        return attestantId;
    }

    public void setAttestantId(String attestantId) {
        this.attestantId = attestantId;
    }

    public Oppdragslinje150 getOppdragslinje150() { return oppdragslinje150; }

    public void setOppdragslinje150(Oppdragslinje150 oppdragslinje150) { this.oppdragslinje150 = oppdragslinje150; }

    @Override
    public boolean equals(Object object){
        if (object == this) {
            return true;
        }
        if (!(object instanceof Attestant180)) {
            return false;
        }
        Attestant180 attestant180 = (Attestant180) object;
        return Objects.equals(attestantId, attestant180.getAttestantId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(attestantId);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String attestantId;
        private Oppdragslinje150 oppdragslinje150;

        public Builder medAttestantId(String attestantId) { this.attestantId = attestantId; return this; }

        public Builder medOppdragslinje150(Oppdragslinje150 oppdragslinje150){ this.oppdragslinje150 = oppdragslinje150; return this; }

        public Attestant180 build() {
            verifyStateForBuild();
            Attestant180 attestant180 = new Attestant180();
            attestant180.attestantId = attestantId;
            attestant180.oppdragslinje150 = oppdragslinje150;
            oppdragslinje150.addAttestant180(attestant180);

            return attestant180;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(attestantId, "attestantId");
            Objects.requireNonNull(oppdragslinje150, "oppdragslinje150");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "attestantId=" + attestantId + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

}
