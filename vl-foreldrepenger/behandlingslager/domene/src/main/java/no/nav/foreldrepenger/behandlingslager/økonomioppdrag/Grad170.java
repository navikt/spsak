package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "Grad170")
@Table(name = "OKO_GRAD_170")
public class Grad170 extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_GRAD_170")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrags_linje_150_id", nullable = false, updatable = false)
    private Oppdragslinje150 oppdragslinje150;

    @Column(name = "type_grad", nullable = false)
    private String typeGrad;

    @Column(name = "grad", nullable = false)
    private int grad;

    public Grad170() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeGrad() {
        return typeGrad;
    }

    public void setTypeGrad(String typeGrad) {
        this.typeGrad = typeGrad;
    }

    public int getGrad() {
        return grad;
    }

    public void setGrad(int grad) {
        this.grad = grad;
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
        if (!(object instanceof Grad170)) {
            return false;
        }
        Grad170 grad170 = (Grad170) object;
        return Objects.equals(typeGrad, grad170.getTypeGrad())
            && Objects.equals(grad, grad170.getGrad());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeGrad, grad);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String typeGrad;
        private int grad;
        private Oppdragslinje150 oppdragslinje150;

        public Builder medTypeGrad(String typeGrad) {
            this.typeGrad = typeGrad;
            return this;
        }

        public Builder medGrad(int grad) {
            this.grad = grad;
            return this;
        }

        public Builder medOppdragslinje150(Oppdragslinje150 oppdragslinje150) {
            this.oppdragslinje150 = oppdragslinje150;
            return this;
        }

        public Grad170 build() {
            verifyStateForBuild();
            Grad170 attestant180 = new Grad170();
            attestant180.typeGrad = typeGrad;
            attestant180.grad = grad;
            attestant180.oppdragslinje150 = oppdragslinje150;
            oppdragslinje150.addGrad170(attestant180);

            return attestant180;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(typeGrad, "typeGrad");
            Objects.requireNonNull(oppdragslinje150, "oppdragslinje150");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
            + "typeGrad=" + typeGrad + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "grad=" + grad + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

}
