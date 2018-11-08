package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "Refusjonsinfo156")
@Table(name = "OKO_REFUSJONSINFO_156")
public class Refusjonsinfo156 extends BaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_REFUSJONSINFO_156")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @OneToOne(optional = false)
    @JoinColumn(name = "oppdrags_linje_150_id", nullable = false, updatable = false)
    private Oppdragslinje150 oppdragslinje150;

    @Column(name = "maks_dato", nullable = false)
    private LocalDate maksDato;

    @Column(name = "refunderes_id", nullable = false)
    private String refunderesId;

    @Column(name = "dato_fom", nullable = false)
    private LocalDate datoFom;

    public Refusjonsinfo156() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getMaksDato() {
        return maksDato;
    }

    public void setMaksDato(LocalDate maksDato) {
        this.maksDato = maksDato;
    }

    public String getRefunderesId() {
        return refunderesId;
    }

    public void setRefunderesId(String refunderesId) {
        this.refunderesId = refunderesId;
    }

    public LocalDate getDatoFom() {
        return datoFom;
    }

    public void setDatoFom(LocalDate datoFom) {
        this.datoFom = datoFom;
    }

    public Oppdragslinje150 getOppdragslinje150() {
        return oppdragslinje150;
    }

    public void setOppdragslinje150(Oppdragslinje150 oppdragslinje150) {
        this.oppdragslinje150 = oppdragslinje150;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Refusjonsinfo156)) {
            return false;
        }
        Refusjonsinfo156 refusjonsinfo156 = (Refusjonsinfo156) object;
        return Objects.equals(maksDato, refusjonsinfo156.getMaksDato())
             && Objects.equals(refunderesId, refusjonsinfo156.getRefunderesId())
            && Objects.equals(datoFom, refusjonsinfo156.getDatoFom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(maksDato, refunderesId, datoFom);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate maksDato;
        private String refunderesId;
        private LocalDate datoFom;
        private Oppdragslinje150 oppdragslinje150;

        public Builder medMaksDato(LocalDate maksDato) {
            this.maksDato = maksDato;
            return this;
        }

        public Builder medRefunderesId(String refunderesId) {
            this.refunderesId = refunderesId;
            return this;
        }

        public Builder medDatoFom(LocalDate datoFom) {
            this.datoFom = datoFom;
            return this;
        }

        public Builder medOppdragslinje150(Oppdragslinje150 oppdragslinje150) {
            this.oppdragslinje150 = oppdragslinje150;
            return this;
        }

        public Refusjonsinfo156 build() {
            verifyStateForBuild();
            Refusjonsinfo156 refusjon156 = new Refusjonsinfo156();
            refusjon156.maksDato = maksDato;
            refusjon156.refunderesId = refunderesId;
            refusjon156.datoFom = datoFom;
            refusjon156.oppdragslinje150 = oppdragslinje150;
            oppdragslinje150.setRefusjonsinfo156(refusjon156);

            return refusjon156;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(maksDato, "maksDato");
            Objects.requireNonNull(refunderesId, "refunderesId");
            Objects.requireNonNull(datoFom, "datoFom");
            Objects.requireNonNull(oppdragslinje150, "oppdragslinje150");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "maksDato=" + maksDato + ", " //$NON-NLS-1$
            + "refunderesId=" + refunderesId + ", " //$NON-NLS-1$
            + "datoFom=" + datoFom + ", " //$NON-NLS-1$
            + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

}
