package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public abstract class UttakPeriode extends LukketPeriode {

    private Stønadskontotype stønadskontotype;
    private Periodetype periodetype;
    private Perioderesultattype perioderesultattype = Perioderesultattype.IKKE_FASTSATT;
    private Avkortingårsaktype avkortingårsaktype;
    private Årsak årsak;
    private Manuellbehandlingårsak manuellbehandlingårsak;
    private BigDecimal gradertArbeidsprosent;
    private List<AktivitetIdentifikator> gradertAktiviteter = Collections.emptyList();
    private Map<AktivitetIdentifikator, BigDecimal> arbeidsprosenter = new HashMap<>();
    private GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak;
    private Map<AktivitetIdentifikator, BigDecimal> utbetalingsgrader = new HashMap<>();
    private OverføringÅrsak overføringÅrsak;
    private PeriodeVurderingType periodeVurderingType;
    private PeriodeKilde periodeKilde;

    private final boolean flerbarnsdager;
    private final boolean samtidigUttak;


    public UttakPeriode(Stønadskontotype stønadskontotype, Periodetype periodetype, PeriodeKilde periodeKilde, LocalDate fom, LocalDate tom, boolean samtidigUttak, boolean flerbarnsdager) {
        super(fom, tom);
        this.samtidigUttak = samtidigUttak;
        this.flerbarnsdager = flerbarnsdager;
        Objects.requireNonNull(stønadskontotype);
        Objects.requireNonNull(periodetype);
        Objects.requireNonNull(periodeKilde);
        this.periodetype = periodetype;
        this.stønadskontotype = stønadskontotype;
        this.periodeKilde = periodeKilde;
    }

    protected UttakPeriode(UttakPeriode kilde, LocalDate fom, LocalDate tom) {
        this(kilde.stønadskontotype, kilde.periodetype, kilde.periodeKilde, fom, tom, kilde.isSamtidigUttak(), kilde.isFlerbarnsdager());
        perioderesultattype = kilde.perioderesultattype;
        avkortingårsaktype = kilde.avkortingårsaktype;
        årsak = kilde.årsak;
        manuellbehandlingårsak = kilde.manuellbehandlingårsak;
        gradertArbeidsprosent = kilde.gradertArbeidsprosent;
        gradertAktiviteter = kilde.gradertAktiviteter;
        overføringÅrsak = kilde.overføringÅrsak;
        arbeidsprosenter = new HashMap<>(kilde.arbeidsprosenter);
        utbetalingsgrader = new HashMap<>(kilde.utbetalingsgrader);
        periodeVurderingType = kilde.periodeVurderingType;
    }

    public void setPeriodeVurderingType(PeriodeVurderingType periodeResultat) {
        this.periodeVurderingType = periodeResultat;
    }

    public PeriodeVurderingType getPeriodeVurderingType() {
        return periodeVurderingType;
    }

    public BigDecimal getUtbetalingsgrad(AktivitetIdentifikator aktivitetIdentifikator) {
        return utbetalingsgrader.get(aktivitetIdentifikator);
    }

    public void setUtbetalingsgrad(AktivitetIdentifikator aktivitetIdentifikator, BigDecimal utbetalingsgrad) {
        this.utbetalingsgrader.put(aktivitetIdentifikator, utbetalingsgrad);
    }

    public void setGradertAktivitet(List<AktivitetIdentifikator> gradertAktivitet, BigDecimal prosentArbeid) {
        Objects.requireNonNull(gradertAktivitet);
        Objects.requireNonNull(prosentArbeid);
        this.gradertAktiviteter = gradertAktivitet;
        this.gradertArbeidsprosent = prosentArbeid;
    }

    public void setArbeidsprosent(AktivitetIdentifikator aktivitet, BigDecimal arbeidsprosent) {
        Objects.requireNonNull(arbeidsprosent);
        Objects.requireNonNull(aktivitet);
        arbeidsprosenter.put(aktivitet, arbeidsprosent);
    }

    public List<AktivitetIdentifikator> getGradertAktiviteter() {
        return gradertAktiviteter;
    }

    public boolean harGradering() {
        return !gradertAktiviteter.isEmpty();
    }

    public boolean harGradering(AktivitetIdentifikator aktivitetIdentifikator) {
        for (AktivitetIdentifikator gradertAktivitet : gradertAktiviteter) {
            if (Objects.equals(gradertAktivitet, aktivitetIdentifikator)) {
                return true;
            }
        }
        return false;
    }

    public boolean harSøktOmOverføringAvKvote() {
        return overføringÅrsak != null;
    }

    public OverføringÅrsak getOverføringÅrsak() {
        return overføringÅrsak;
    }

    public void setOverføringÅrsak(OverføringÅrsak overføringÅrsak) {
        this.overføringÅrsak = overføringÅrsak;
    }

    abstract <T extends UttakPeriode> T kopiMedNyPeriode(LocalDate fom, LocalDate tom);

    public Periodetype getPeriodetype() {
        return periodetype;
    }

    public PeriodeKilde getPeriodeKilde() {
        return periodeKilde;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public Stønadskontotype getTrekkKonto() {
        return stønadskontotype;
    }

    public Perioderesultattype getPerioderesultattype() {
        return perioderesultattype;
    }

    public Årsak getÅrsak() {
        return årsak;
    }

    public Avkortingårsaktype getAvkortingårsaktype() {
        return avkortingårsaktype;
    }

    public Manuellbehandlingårsak getManuellbehandlingårsak() {
        return manuellbehandlingårsak;
    }

    void setPerioderesultattype(Perioderesultattype perioderesultattype) {
        this.perioderesultattype = perioderesultattype;
    }

    void setÅrsak(Årsak årsak) {
        this.årsak = årsak;
    }

    void setAvkortingårsaktype(Avkortingårsaktype avkortingårsaktype) {
        this.avkortingårsaktype = avkortingårsaktype;
    }

    void setManuellbehandlingårsak(Manuellbehandlingårsak manuellbehandlingårsak) {
        this.manuellbehandlingårsak = manuellbehandlingårsak;
    }

    public abstract int getTrekkdager(AktivitetIdentifikator aktivitetIdentifikator);

    public abstract int getMinimumTrekkdager();

    public abstract int getMaksimumTrekkdager();

    public BigDecimal getProsentArbeid(AktivitetIdentifikator aktivitet) {
        return arbeidsprosenter.get(aktivitet);
    }

    public BigDecimal getGradertArbeidsprosent() {
        return gradertArbeidsprosent;
    }

    public void opphevGradering(GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        gradertAktiviteter = Collections.emptyList();
        this.graderingIkkeInnvilgetÅrsak = graderingIkkeInnvilgetÅrsak;
    }

    public GraderingIkkeInnvilgetÅrsak getGraderingIkkeInnvilgetÅrsak() {
        return graderingIkkeInnvilgetÅrsak;
    }

    public boolean isSamtidigUttak() {
        return samtidigUttak;
    }

    public boolean isFlerbarnsdager() {
        return flerbarnsdager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UttakPeriode that = (UttakPeriode) o;
        return stønadskontotype == that.stønadskontotype &&
                perioderesultattype == that.perioderesultattype &&
                periodetype == that.periodetype &&
                periodeKilde == that.periodeKilde &&
                avkortingårsaktype == that.avkortingårsaktype &&
                manuellbehandlingårsak == that.manuellbehandlingårsak &&
                Objects.equals(årsak, that.årsak) &&
                Objects.equals(gradertArbeidsprosent, that.gradertArbeidsprosent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stønadskontotype, perioderesultattype, periodetype, periodeKilde, avkortingårsaktype, manuellbehandlingårsak, årsak, gradertArbeidsprosent);
    }

    @Override
    public String toString() {
        return "UttakPeriode{" +
                "fom=" + getFom() +
                ", tom=" + getTom() +
                ", stønadskontotype=" + stønadskontotype +
                ", perioderesultattype=" + perioderesultattype +
                ", manuellbehandlingårsak=" + manuellbehandlingårsak +
                '}';
    }
}
