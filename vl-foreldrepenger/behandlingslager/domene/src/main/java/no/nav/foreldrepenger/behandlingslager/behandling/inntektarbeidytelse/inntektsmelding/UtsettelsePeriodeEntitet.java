package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "UtsettelsePeriode")
@Table(name = "IAY_UTSETTELSE_PERIODE")
public class UtsettelsePeriodeEntitet extends BaseEntitet implements UtsettelsePeriode, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTSETTELSE_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private InntektsmeldingEntitet inntektsmelding;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + UtsettelseÅrsak.DISCRIMINATOR + "'")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "utsettelse_aarsak_type", referencedColumnName = "kode")),
    })
    @ChangeTracked
    private UtsettelseÅrsak årsak = UtsettelseÅrsak.UDEFINERT;


    private UtsettelsePeriodeEntitet(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
        this.årsak = UtsettelseÅrsak.FERIE;
    }

    private UtsettelsePeriodeEntitet(LocalDate fom, LocalDate tom, UtsettelseÅrsak årsak) {
        this.årsak = årsak;
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    UtsettelsePeriodeEntitet() {
    }

    UtsettelsePeriodeEntitet(UtsettelsePeriode utsettelsePeriode) {
        this.periode = utsettelsePeriode.getPeriode();
        this.årsak = utsettelsePeriode.getÅrsak();
    }

    public static UtsettelsePeriode ferie(LocalDate fom, LocalDate tom) {
        return new UtsettelsePeriodeEntitet(fom, tom);
    }

    public static UtsettelsePeriode utsettelse(LocalDate fom, LocalDate tom, UtsettelseÅrsak årsak) {
        return new UtsettelsePeriodeEntitet(fom, tom, årsak);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(årsak, periode);
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }


    @Override
    public UtsettelseÅrsak getÅrsak() {
        return årsak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtsettelsePeriodeEntitet that = (UtsettelsePeriodeEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(årsak, that.årsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, årsak);
    }

    @Override
    public String toString() {
        return "UtsettelsePeriodeEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", årsak=" + årsak +
            '}';
    }

    void setInntektsmelding(InntektsmeldingEntitet inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }
}
