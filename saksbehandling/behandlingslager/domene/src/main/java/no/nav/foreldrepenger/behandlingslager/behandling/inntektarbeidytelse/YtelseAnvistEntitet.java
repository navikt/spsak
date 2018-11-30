package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "YtelseAnvistEntitet")
@Table(name = "IAY_YTELSE_ANVIST")
class YtelseAnvistEntitet extends BaseEntitet implements YtelseAnvist, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_ANVIST")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private YtelseEntitet ytelse;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet anvistPeriode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep")))
    @ChangeTracked
    private Beløp beløp;

    @Column(name = "dagsats")
    @ChangeTracked
    private BigDecimal dagsats;

    @Column(name = "utbetalingsgrad_prosent")
    @ChangeTracked
    private BigDecimal utbetalingsgradProsent;

    public YtelseAnvistEntitet() {
        // hibernate
    }

    public YtelseAnvistEntitet(YtelseAnvist ytelseAnvist) {
        this.anvistPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistTOM());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(this.anvistPeriode);
    }

    @Override
    public LocalDate getAnvistFOM() {
        return anvistPeriode.getFomDato();
    }

    @Override
    public LocalDate getAnvistTOM() {
        return anvistPeriode.getTomDato();
    }

    @Override
    public Optional<BigDecimal> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    @Override
    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    @Override
    public Optional<BigDecimal> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    public void setBeløp(BigDecimal beløp) {
        this.beløp = new Beløp(beløp);
    }

    public void setDagsats(BigDecimal dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(DatoIntervallEntitet periode) {
        this.anvistPeriode = periode;
    }

    public void setUtbetalingsgradProsent(BigDecimal utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public void setYtelse(YtelseEntitet ytelse) {
        this.ytelse = ytelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelseAnvistEntitet that = (YtelseAnvistEntitet) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(dagsats, that.dagsats) &&
            Objects.equals(utbetalingsgradProsent, that.utbetalingsgradProsent);
    }

    @Override
    public int hashCode() {

        return Objects.hash(anvistPeriode, beløp, dagsats, utbetalingsgradProsent);
    }

    @Override
    public String toString() {
        return "YtelseAnvistEntitet{" +
            "periode=" + anvistPeriode +
            ", beløp=" + beløp +
            ", dagsats=" + dagsats +
            ", utbetalingsgradProsent=" + utbetalingsgradProsent +
            '}';
    }

    boolean hasValues() {
        return beløp != null || anvistPeriode != null || dagsats != null || utbetalingsgradProsent != null;
    }
}
