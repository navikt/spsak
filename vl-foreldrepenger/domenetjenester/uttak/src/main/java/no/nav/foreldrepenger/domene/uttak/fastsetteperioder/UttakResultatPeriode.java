package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Strings;

import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class UttakResultatPeriode {

    private LocalDateInterval tidsperiode;
    private List<UttakResultatPeriodeAktivitet> aktiviteter;
    private PeriodeResultatType type;
    private PeriodeResultatÅrsak årsak;
    private GraderingAvslagÅrsak graderingAvslagÅrsak;
    private String begrunnelse;
    private boolean samtidigUttak;
    private BigDecimal samtidigUttaksprosent;
    private boolean flerbarnsdager;
    private boolean graderingInnvilget;
    private UttakUtsettelseType utsettelseType;

    private UttakResultatPeriode() {

    }

    public LocalDateInterval getTidsperiode() {
        return tidsperiode;
    }

    public List<UttakResultatPeriodeAktivitet> getAktiviteter() {
        return aktiviteter;
    }

    public PeriodeResultatType getResultatType() {
        return type;
    }

    public UttakUtsettelseType getUtsettelseType() {
        return utsettelseType;
    }

    public PeriodeResultatÅrsak getResultatÅrsak() {
        return årsak;
    }

    public GraderingAvslagÅrsak getGraderingAvslagÅrsak() {
        if (graderingAvslagÅrsak == null || graderingInnvilget) {
            return GraderingAvslagÅrsak.UKJENT;
        } else {
            return graderingAvslagÅrsak;
        }
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public PeriodeResultatType getType() {
        return type;
    }

    public PeriodeResultatÅrsak getÅrsak() {
        return årsak;
    }

    @Override
    public String toString() {
        return "UttakResultatPeriode{" +
            "tidsperiode=" + tidsperiode +
            ", type=" + type +
            ", årsak=" + årsak +
            ", samtidigUttak=" + samtidigUttak +
            ", samtidigUttaksprosent=" + samtidigUttaksprosent +
            ", flerbarnsdager=" + flerbarnsdager +
            ", utsettelseType=" + utsettelseType +
            '}';
    }

    public boolean erLik(UttakResultatPeriode periode) {
        return Objects.equals(periode.getTidsperiode(), getTidsperiode())
            && Objects.equals(periode.getResultatType(), getResultatType())
            && Objects.equals(periode.getResultatÅrsak(), getResultatÅrsak())
            && Objects.equals(periode.isSamtidigUttak(), isSamtidigUttak())
            && Objects.equals(periode.getSamtidigUttaksprosent(), getSamtidigUttaksprosent())
            && Objects.equals(periode.isFlerbarnsdager(), isFlerbarnsdager())
            && Objects.equals(periode.getUtsettelseType(), getUtsettelseType())
            && aktiviteterErLike(periode.getAktiviteter());
    }

    private boolean aktiviteterErLike(List<UttakResultatPeriodeAktivitet> aktiviteter) {
        for (UttakResultatPeriodeAktivitet aktivitet : aktiviteter) {
            if (!harLikAktivitet(aktivitet)) {
                return false;
            }
        }
        return true;
    }

    private boolean harLikAktivitet(UttakResultatPeriodeAktivitet aktivitet1) {
        for (UttakResultatPeriodeAktivitet aktivitet2 : getAktiviteter()) {
            if (aktivitet1.likBortsettFraTrekkdager(aktivitet2)) {
                return true;
            }
        }
        return false;
    }

    public void addPeriode(UttakResultatPeriodeAktivitet periode) {
        aktiviteter.add(periode);
    }

    public boolean harDatoIPerioden(LocalDate localDate) {
        return (localDate.isEqual(tidsperiode.getFomDato()) || localDate.isAfter(tidsperiode.getFomDato()))
            && (localDate.isEqual(tidsperiode.getTomDato()) || localDate.isBefore(tidsperiode.getTomDato()));
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public BigDecimal getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    public boolean isGraderingInnvilget() {
        return graderingInnvilget;
    }

    public static class Builder {

        private final UttakResultatPeriode kladd;

        public Builder() {
            kladd = new UttakResultatPeriode();
            kladd.aktiviteter = Collections.emptyList();
            kladd.årsak = PeriodeResultatÅrsak.UKJENT;
        }

        public Builder medTidsperiode(LocalDateInterval tidsperiode) {
            kladd.tidsperiode = tidsperiode;
            return this;
        }

        public Builder medAktiviteter(List<UttakResultatPeriodeAktivitet> aktiviteter) {
            kladd.aktiviteter = aktiviteter;
            return this;
        }

        public Builder medType(PeriodeResultatType type) {
            kladd.type = type;
            return this;
        }

        public Builder medÅrsak(PeriodeResultatÅrsak årsak) {
            kladd.årsak = årsak;
            return this;
        }

        public Builder medGraderingAvslåttÅrsak(GraderingAvslagÅrsak graderingAvslagÅrsak) {
            kladd.graderingAvslagÅrsak = graderingAvslagÅrsak;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            kladd.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medSamtidigUttak(boolean samtidigUttak) {
            kladd.samtidigUttak = samtidigUttak;
            return this;
        }

        public Builder medSamtidigUttaksprosent(BigDecimal samtidigUttaksprosent) {
            kladd.samtidigUttaksprosent = samtidigUttaksprosent;
            return this;
        }

        public Builder medFlerbarnsdager(boolean flerbarnsdager) {
            kladd.flerbarnsdager = flerbarnsdager;
            return this;
        }

        public Builder medGraderingInnvilget(boolean graderingInnvilget) {
            kladd.graderingInnvilget = graderingInnvilget;
            return this;
        }

        public Builder medUtsettelseType(UttakUtsettelseType utsettelseType) {
            kladd.utsettelseType = utsettelseType;
            return this;
        }

        public UttakResultatPeriode build() {
            Objects.requireNonNull(kladd.tidsperiode);
            Objects.requireNonNull(kladd.årsak);
            return kladd;
        }
    }
}
