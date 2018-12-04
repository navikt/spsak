package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.domene.typer.Beløp;

@Entity(name = "Refusjon")
@Table(name = "IAY_REFUSJON")
public class RefusjonEntitet extends BaseEntitet implements Refusjon {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REFUSJON")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private InntektsmeldingEntitet inntektsmelding;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjonsbeloep_mnd", nullable = false)))
    private Beløp refusjonsbeløpMnd;

    @Column(name = "fom", nullable = false)
    private LocalDate fom;

    public RefusjonEntitet() {
    }

    public RefusjonEntitet(BigDecimal refusjonsbeløpMnd, LocalDate fom) {
        this.refusjonsbeløpMnd = new Beløp(refusjonsbeløpMnd);
        this.fom = fom;
    }

    RefusjonEntitet(Refusjon refusjon) {
        this.refusjonsbeløpMnd = refusjon.getRefusjonsbeløp();
        this.fom = refusjon.getFom();
    }

    public void setInntektsmelding(InntektsmeldingEntitet inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }


    @Override
    public Beløp getRefusjonsbeløp() {
        return refusjonsbeløpMnd;
    }

    @Override
    public LocalDate getFom() {
        return fom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonEntitet that = (RefusjonEntitet) o;
        return Objects.equals(refusjonsbeløpMnd, that.refusjonsbeløpMnd) &&
            Objects.equals(fom, that.fom);
    }

    @Override
    public int hashCode() {

        return Objects.hash(refusjonsbeløpMnd, fom);
    }
}
