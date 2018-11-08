package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "SoeknadPeriode")
@Table(name = "YF_FORDELING_PERIODE")
public class OppgittPeriodeEntitet extends BaseEntitet implements OppgittPeriode, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YF_FORDELING_PERIODE")
    private Long id;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "periode_type", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + UttakPeriodeType.DISCRIMINATOR + "'"))
    @ChangeTracked
    private UttakPeriodeType uttakPeriodeType;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "vurdering_type", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + UttakPeriodeVurderingType.DISCRIMINATOR + "'"))
    @ChangeTracked
    private UttakPeriodeVurderingType periodeVurderingType = UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "arbeidstaker", nullable = false)
    @ChangeTracked
    private boolean erArbeidstaker;

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Column(name = "kl_aarsak_type", nullable = false)
    @ChangeTracked
    private String årsakType = Årsak.DISCRIMINATOR;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "kl_aarsak_type" /* bruker kolonnenavn, da discriminator kan variere*/, referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "aarsak_type", referencedColumnName = "kode")),
    })
    @ChangeTracked
    private Årsak årsak = Årsak.UDEFINERT;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @Column(name = "arbeidsprosent")
    @ChangeTracked
    private BigDecimal arbeidsprosent;

    @Column(name = "begrunnelse")
    @ChangeTracked
    private String begrunnelse;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "MORS_AKTIVITET", referencedColumnName = "kode")),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + MorsAktivitet.DISCRIMINATOR + "'"))
    })
    @ChangeTracked
    private MorsAktivitet morsAktivitet = MorsAktivitet.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fordeling_id", nullable = false, updatable = false, unique = true)
    private OppgittFordelingEntitet oppgittFordeling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "samtidig_uttak", nullable = false)
    private boolean samtidigUttak;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "flerbarnsdager", nullable = false)
    private boolean flerbarnsdager;

    @Column(name = "samtidig_uttaksprosent")
    private BigDecimal samtidigUttaksprosent;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "FORDELING_PERIODE_KILDE", referencedColumnName = "kode")),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FordelingPeriodeKilde.DISCRIMINATOR + "'"))
    })
    private FordelingPeriodeKilde periodeKilde = FordelingPeriodeKilde.SØKNAD;

    OppgittPeriodeEntitet() {
        // Hibernate
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(uttakPeriodeType, årsakType, årsak, arbeidsgiver, periode);
    }

    @Override
    public UttakPeriodeType getPeriodeType() {
        return uttakPeriodeType;
    }

    void setPeriodeType(UttakPeriodeType uttakPeriodeType) {
        this.uttakPeriodeType = uttakPeriodeType;
    }

    void setOppgittFordeling(OppgittFordelingEntitet oppgittFordeling) {
        this.oppgittFordeling = oppgittFordeling;
    }

    @Override
    public LocalDate getFom() {
        return periode.getFomDato();
    }

    @Override
    public LocalDate getTom() {
        return periode.getTomDato();
    }

    void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    @Override
    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }

    void setArbeidsprosent(BigDecimal arbeidsprosent) {
        this.arbeidsprosent = arbeidsprosent;
    }

    void setÅrsakType(String årsakType) {
        this.årsakType = årsakType;
    }

    @Override
    public Årsak getÅrsak() {
        return årsak;
    }

    void setÅrsak(Årsak årsak) {
        this.årsak = årsak;
    }

    @Override
    public MorsAktivitet getMorsAktivitet() {
        return morsAktivitet;
    }

    void setMorsAktivitet(MorsAktivitet morsAktivitet) {
        this.morsAktivitet = morsAktivitet;
    }

    @Override
    public Optional<String> getBegrunnelse() {
        return Optional.ofNullable(begrunnelse);
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    @Override
    public boolean getErArbeidstaker() {
        return erArbeidstaker;
    }

    void setErArbeidstaker(boolean erArbeidstaker) {
        this.erArbeidstaker = erArbeidstaker;
    }

    @Override
    public Virksomhet getVirksomhet() {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getVirksomhet();
    }

    void setVirksomhet(Virksomhet virksomhet) {
        if (virksomhet != null) {
            this.arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
        }
    }

    @Override
    public UttakPeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    void setPeriodeVurderingType(UttakPeriodeVurderingType periodeVurderingType) {
        this.periodeVurderingType = periodeVurderingType;
    }

    @Override
    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    void setSamtidigUttak(boolean samtidigUttak) {
        this.samtidigUttak = samtidigUttak;
    }

    @Override
    public BigDecimal getSamtidigUttaksprosent() {
        return samtidigUttaksprosent;
    }

    void setSamtidigUttaksprosent(BigDecimal samtidigUttaksprosent) {
        this.samtidigUttaksprosent = samtidigUttaksprosent;
    }

    @Override
    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    void setFlerbarnsdager(boolean flerbarnsdager) {
        this.flerbarnsdager = flerbarnsdager;
    }

    @Override
    public FordelingPeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }

    void setPeriodeKilde(FordelingPeriodeKilde periodeKilde) {
        this.periodeKilde = periodeKilde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OppgittPeriodeEntitet)) {
            return false;
        }
        OppgittPeriodeEntitet that = (OppgittPeriodeEntitet) o;
        return Objects.equals(uttakPeriodeType, that.uttakPeriodeType) &&
            Objects.equals(årsakType, that.årsakType) &&
            Objects.equals(årsak, that.årsak) &&
            Objects.equals(periode, that.periode) &&
            Objects.equals(arbeidsprosent, that.arbeidsprosent) &&
            Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(erArbeidstaker, that.erArbeidstaker) &&
            Objects.equals(morsAktivitet, that.morsAktivitet) &&
            Objects.equals(samtidigUttak, that.samtidigUttak) &&
            Objects.equals(periodeKilde, that.periodeKilde) &&
            Objects.equals(samtidigUttaksprosent, that.samtidigUttaksprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uttakPeriodeType, årsakType, årsak, periode, arbeidsprosent, morsAktivitet, erArbeidstaker, arbeidsgiver, periodeKilde, samtidigUttaksprosent);
    }
}
