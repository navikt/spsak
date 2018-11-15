package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Gradering")
@Table(name = "IAY_ARBEIDSGIVERPERIODE")
class ArbeidsgiverperiodeEntitet extends BaseEntitet implements Arbeidsgiverperiode, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ARBEIDSGIVERPERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private InntektsmeldingEntitet inntektsmelding;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    ArbeidsgiverperiodeEntitet() {
    }

    public ArbeidsgiverperiodeEntitet(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    public ArbeidsgiverperiodeEntitet(LocalDate fom, LocalDate tom) {
        this(tom == null ? DatoIntervallEntitet.fraOgMed(fom) : DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
    }

    ArbeidsgiverperiodeEntitet(Arbeidsgiverperiode periode) {
        this(periode.getPeriode());
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode);
    }

    void setInntektsmelding(InntektsmeldingEntitet inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public String toString() {
        return "ArbeidsgiverperiodeEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsgiverperiodeEntitet that = (ArbeidsgiverperiodeEntitet) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
