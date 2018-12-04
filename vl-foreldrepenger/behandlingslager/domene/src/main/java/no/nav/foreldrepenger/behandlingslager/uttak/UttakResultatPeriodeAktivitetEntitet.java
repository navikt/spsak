package no.nav.foreldrepenger.behandlingslager.uttak;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity
@Table(name = "UTTAK_RESULTAT_PERIODE_AKT")
public class UttakResultatPeriodeAktivitetEntitet extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_RESULTAT_PERIODE_AKT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uttak_resultat_periode_id", nullable = false, updatable = false)
    private UttakResultatPeriodeEntitet periode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uttak_aktivitet_id", nullable = false, updatable = false)
    private UttakAktivitetEntitet uttakAktivitet;

    @Column(name = "trekkdager", nullable = false)
    private int trekkdager;

    @Column(name = "arbeidstidsprosent", nullable = false)
    private BigDecimal arbeidsprosent;

    @Column(name = "utbetalingsprosent")
    private BigDecimal utbetalingsprosent;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "gradering", updatable = false, nullable = false)
    private boolean gradering;

    @Override
    public String toString() {
        return "UttakResultatPeriodeAktivitetEntitet{" +
            "periode=" + periode.getId() +
            ", trekkdager='" + trekkdager + '\'' +
            ", arbeidsprosent=" + arbeidsprosent +
            ", utbetalingsprosent=" + utbetalingsprosent +
            ", uttakAktivitet=" + uttakAktivitet +
            ", gradering=" + gradering +
            '}';
    }

    public Long getId() {
        return id;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    public BigDecimal getUtbetalingsprosent() {
        return utbetalingsprosent;
    }

    public UttakResultatPeriodeEntitet getPeriode() {
        return periode;
    }

    public UttakAktivitetEntitet getUttakAktivitet() {
        return uttakAktivitet;
    }

    public static Builder builder(UttakResultatPeriodeEntitet periode, UttakAktivitetEntitet uttakAktivitet) {
        return new Builder(periode, uttakAktivitet);
    }

    public void setPeriode(UttakResultatPeriodeEntitet periode) {
        this.periode = periode;
    }

    public void setUttakAktivitet(UttakAktivitetEntitet uttakAktivitet) {
        this.uttakAktivitet = uttakAktivitet;
    }

    public LocalDate getFom() {
        return this.periode.getFom();
    }

    public LocalDate getTom() {
        return this.periode.getTom();
    }

    public boolean isGraderingInnvilget() {
        return periode.isGraderingInnvilget() && isSøktGradering();
    }

    public String getArbeidsforholdId() {
        return uttakAktivitet.getArbeidsforholdId();
    }

    public String getArbeidsforholdOrgnr() {
        return uttakAktivitet.getArbeidsforholdOrgnr();
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakAktivitet.getUttakArbeidType();
    }

    public boolean isSøktGradering() {
        return gradering;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UttakResultatPeriodeAktivitetEntitet that = (UttakResultatPeriodeAktivitetEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(uttakAktivitet, that.uttakAktivitet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(periode, uttakAktivitet);
    }

    public static class Builder {
        UttakResultatPeriodeAktivitetEntitet kladd;

        public Builder(UttakResultatPeriodeEntitet periode, UttakAktivitetEntitet uttakAktivitet) {
            kladd = new UttakResultatPeriodeAktivitetEntitet();
            kladd.periode = periode;
            kladd.uttakAktivitet = uttakAktivitet;
            periode.leggTilAktivitet(kladd);
        }

        public UttakResultatPeriodeAktivitetEntitet.Builder medTrekkdager(Integer trekkdager) {
            kladd.trekkdager = trekkdager;
            return this;
        }

        public UttakResultatPeriodeAktivitetEntitet.Builder medArbeidsprosent(BigDecimal arbeidsprosent) {
            kladd.arbeidsprosent = arbeidsprosent;
            return this;
        }

        public UttakResultatPeriodeAktivitetEntitet.Builder medUtbetalingsprosent(BigDecimal utbetalingsprosent) {
            kladd.utbetalingsprosent = utbetalingsprosent;
            return this;
        }

        public UttakResultatPeriodeAktivitetEntitet.Builder medErSøktGradering(boolean gradering) {
            kladd.gradering = gradering;
            return this;
        }

        public UttakResultatPeriodeAktivitetEntitet build() {
            Objects.requireNonNull(kladd.periode, "periode");
            Objects.requireNonNull(kladd.arbeidsprosent, "arbeidsprosent");
            Objects.requireNonNull(kladd.uttakAktivitet, "uttakAktivitet");
            return kladd;
        }
    }
}
