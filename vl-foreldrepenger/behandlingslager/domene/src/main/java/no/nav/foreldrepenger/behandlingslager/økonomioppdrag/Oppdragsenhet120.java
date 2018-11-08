package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import java.time.LocalDate;
import java.util.Objects;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity(name = "Oppdragsenhet120")
@Table(name = "OKO_OPPDRAG_ENHET_120")
public class Oppdragsenhet120 extends BaseEntitet{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OKO_OPPDRAG_ENHET_120")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "type_enhet", nullable = false)
    private String typeEnhet;

    @Column(name = "enhet", nullable = false)
    private String enhet;

    @Column(name = "dato_enhet_fom", nullable = false)
    private LocalDate datoEnhetFom;

    /* bruker @ManyToOne siden JPA ikke støtter OneToOne join på non-PK column. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "oppdrag110_id", nullable = false)
    private Oppdrag110 oppdrag110;

    public Oppdragsenhet120() {  }

    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeEnhet() {
        return typeEnhet;
    }

    public void setTypeEnhet(String typeEnhet) {
        this.typeEnhet = typeEnhet;
    }

    public String getEnhet() {
        return enhet;
    }

    public void setEnhet(String enhet) {
        this.enhet = enhet;
    }

    public LocalDate getDatoEnhetFom() {
        return datoEnhetFom;
    }

    public void setDatoEnhetFom(LocalDate datoEnhetFom) {
        this.datoEnhetFom = datoEnhetFom;
    }

    public Oppdrag110 getOppdrag110() { return oppdrag110; }

    public void setOppdrag110(Oppdrag110 oppdrag110) { this.oppdrag110 = oppdrag110; }

    @Override
    public boolean equals(Object object){
        if (object == this) {
            return true;
        }
        if (!(object instanceof Oppdragsenhet120)) {
            return false;
        }
        Oppdragsenhet120 oppdrenhet120 = (Oppdragsenhet120) object;
        return Objects.equals(typeEnhet, oppdrenhet120.getTypeEnhet())
                && Objects.equals(enhet, oppdrenhet120.getEnhet())
                && Objects.equals(datoEnhetFom, oppdrenhet120.getDatoEnhetFom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeEnhet, enhet, datoEnhetFom);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder {
        private String typeEnhet;
        private String enhet;
        private LocalDate datoEnhetFom;
        private Oppdrag110 oppdrag110;

        public Builder medTypeEnhet(String typeEnhet) { this.typeEnhet = typeEnhet; return this; }

        public Builder medEnhet(String enhet) { this.enhet = enhet; return this; }

        public Builder medDatoEnhetFom(LocalDate datoEnhetFom) { this.datoEnhetFom = datoEnhetFom; return this; }

        public Builder medOppdrag110(Oppdrag110 oppdrag110) { this.oppdrag110 = oppdrag110; return this; }

        public Oppdragsenhet120 build() {
            verifyStateForBuild();
            Oppdragsenhet120 oppdragsenhet120 = new Oppdragsenhet120();
            oppdragsenhet120.typeEnhet = typeEnhet;
            oppdragsenhet120.enhet = enhet;
            oppdragsenhet120.datoEnhetFom = datoEnhetFom;
            oppdragsenhet120.oppdrag110 = oppdrag110;
            oppdrag110.addOppdragsenhet120(oppdragsenhet120);

            return oppdragsenhet120;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(typeEnhet, "typeEnhet");
            Objects.requireNonNull(enhet, "enhet");
            Objects.requireNonNull(datoEnhetFom, "datoEnhetFom");
            Objects.requireNonNull(oppdrag110, "oppdrag110");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + "typeEnhet=" + typeEnhet + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "enhet=" + enhet + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "datoEnhetFom=" + datoEnhetFom + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }
}
