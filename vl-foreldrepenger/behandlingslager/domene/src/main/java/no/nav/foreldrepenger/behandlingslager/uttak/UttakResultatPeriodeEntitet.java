package no.nav.foreldrepenger.behandlingslager.uttak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity
@Table(name = "UTTAK_RESULTAT_PERIODE")
public class UttakResultatPeriodeEntitet extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_RESULTAT_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uttak_resultat_perioder_id", nullable = false, updatable = false, unique = true)
    private UttakResultatPerioderEntitet perioder;

    @OneToMany(mappedBy = "periode")
    private List<UttakResultatPeriodeAktivitetEntitet> aktiviteter = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "periode")
    private UttakResultatDokRegelEntitet dokRegel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "fom", nullable = false)),
            @AttributeOverride(name = "tomDato", column = @Column(name = "tom", nullable = false))
    })
    private DatoIntervallEntitet tidsperiode;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "gradering_innvilget", nullable = false)
    private boolean graderingInnvilget;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + UttakUtsettelseType.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "uttak_utsettelse_type", referencedColumnName = "kode")),
    })
    private UttakUtsettelseType utsettelseType;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + PeriodeResultatType.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "periode_resultat_type", referencedColumnName = "kode")),
    })
    private PeriodeResultatType periodeResultatType;

    @Column(name = "kl_periode_resultat_aarsak", nullable = false)
    private String klPeriodeResultatÅrsak = PeriodeResultatÅrsak.DISCRIMINATOR;

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "PERIODE_RESULTAT_AARSAK" /* bruker kolonnenavn, da discriminator kan variere */ , referencedColumnName = "kode")),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "kl_periode_resultat_aarsak")) })
    private PeriodeResultatÅrsak periodeResultatÅrsak;

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "gradering_avslag_aarsak", referencedColumnName = "kode")),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + GraderingAvslagÅrsak.DISCRIMINATOR + "'")) })
    private GraderingAvslagÅrsak graderingAvslagÅrsak = GraderingAvslagÅrsak.UKJENT;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "flerbarnsdager", nullable = false)
    private boolean flerbarnsdager;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "samtidig_uttak", nullable = false)
    private boolean samtidigUttak;

    @Column(name = "samtidig_uttaksprosent")
    private BigDecimal samtidigUttaksprosent;

    @ManyToOne
    @JoinColumn(name = "periode_soknad_id", updatable = false)
    private UttakResultatPeriodeSøknadEntitet periodeSøknad;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "manuelt_behandlet", nullable = false, updatable = false)
    private boolean manueltBehandlet;

    @Override
    public String toString() {
        return "UttakResultatPeriodeEntitet{" +
            "tidsperiode=" + tidsperiode +
            ", graderingInnvilget=" + graderingInnvilget +
            ", utsettelseType=" + utsettelseType +
            ", periodeResultatType=" + periodeResultatType.getKode() +
            ", periodeResultatÅrsak=" + periodeResultatÅrsak.getKode() +
            ", samtidigUttak=" + samtidigUttak +
            ", samtidigUttaksprosent=" + samtidigUttaksprosent +
            ", manueltBehandlet=" + manueltBehandlet +
            '}';
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFom() {
        return tidsperiode.getFomDato();
    }

    public LocalDate getTom() {
        return tidsperiode.getTomDato();
    }

    public DatoIntervallEntitet getTidsperiode() {
        return tidsperiode;
    }

    public UttakUtsettelseType getUtsettelseType() {
        return utsettelseType;
    }

    public PeriodeResultatType getPeriodeResultatType() {
        return periodeResultatType;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public PeriodeResultatÅrsak getPeriodeResultatÅrsak() {
        return periodeResultatÅrsak;
    }

    public UttakResultatDokRegelEntitet getDokRegel() {
        return dokRegel;
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

    public void leggTilAktivitet(UttakResultatPeriodeAktivitetEntitet aktivitet) {
        if (!aktiviteter.contains(aktivitet)) {
            this.aktiviteter.add(aktivitet);
            aktivitet.setPeriode(this);
        }
    }

    public List<UttakResultatPeriodeAktivitetEntitet> getAktiviteter() {
        return aktiviteter;
    }

    public void setPerioder(UttakResultatPerioderEntitet perioder) {
        this.perioder = perioder;
    }

    public ManuellBehandlingÅrsak getManuellBehandlingÅrsak() {
        return dokRegel == null ? null : dokRegel.getManuellBehandlingÅrsak();
    }

    public Optional<UttakResultatPeriodeSøknadEntitet> getPeriodeSøknad() {
        return Optional.ofNullable(periodeSøknad);
    }

    public boolean isManueltBehandlet() {
        return manueltBehandlet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UttakResultatPeriodeEntitet that = (UttakResultatPeriodeEntitet) o;
        return Objects.equals(perioder, that.perioder) &&
            Objects.equals(tidsperiode, that.tidsperiode);
    }
    @Override
    public int hashCode() {

        return Objects.hash(perioder, tidsperiode);
    }

    public static class Builder {
        private UttakResultatPeriodeEntitet kladd;

        public Builder(LocalDate fom, LocalDate tom) {
            this.kladd = new UttakResultatPeriodeEntitet();
            this.kladd.tidsperiode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
        }

        public Builder medGraderingInnvilget(boolean innvilget) {
            kladd.graderingInnvilget = innvilget;
            return this;
        }

        public Builder medUtsettelseType(UttakUtsettelseType utsettelseType) {
            kladd.utsettelseType = utsettelseType;
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

        public Builder medManueltBehandlet(boolean manueltBehandlet) {
            kladd.manueltBehandlet = manueltBehandlet;
            return this;
        }

        public Builder medPeriodeResultat(PeriodeResultatType periodeResultatType, PeriodeResultatÅrsak periodeResultatÅrsak) {
            kladd.periodeResultatType = periodeResultatType;
            kladd.periodeResultatÅrsak = periodeResultatÅrsak;
            kladd.klPeriodeResultatÅrsak = periodeResultatÅrsak.getKodeverk();
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            kladd.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medDokRegel(UttakResultatDokRegelEntitet dokRegel) {
            kladd.dokRegel = dokRegel;
            return this;
        }

        public Builder medPeriodeSoknad(UttakResultatPeriodeSøknadEntitet periodeSøknad) {
            kladd.periodeSøknad = periodeSøknad;
            return this;
        }

        public Builder medGraderingAvslagÅrsak(GraderingAvslagÅrsak graderingAvslagÅrsak) {
            kladd.graderingAvslagÅrsak = graderingAvslagÅrsak;
            return this;
        }

        public UttakResultatPeriodeEntitet build() {
            Objects.requireNonNull(kladd.tidsperiode, "tidsperiode");
            Objects.requireNonNull(kladd.periodeResultatType, "periodeResultatType");
            if (kladd.dokRegel != null) {
                kladd.dokRegel.setPeriode(kladd);
            }
            if (kladd.utsettelseType == null) {
                kladd.utsettelseType = UttakUtsettelseType.UDEFINERT;
            }
            return kladd;
        }

    }
}
