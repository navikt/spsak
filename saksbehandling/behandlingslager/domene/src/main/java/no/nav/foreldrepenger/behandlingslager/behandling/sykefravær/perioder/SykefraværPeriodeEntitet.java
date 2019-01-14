package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "SykefraværPeriodeEntitet")
@Table(name = "SF_SYKEFRAVAER_PERIODE")
class SykefraværPeriodeEntitet extends BaseEntitet implements SykefraværPeriode {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SF_SYKEFRAVAER_PERIODE")
    private Long id;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til parent! */}, fetch = FetchType.LAZY)
    @JoinColumn(name = "sykefravaer_id", updatable = false, nullable = false)
    private SykefraværEntitet sykefravær;

    @ChangeTracked
    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "fravaer_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + SykefraværPeriodeType.DISCRIMINATOR + "'"))
    private SykefraværPeriodeType type = SykefraværPeriodeType.UDEFINERT;

    @ChangeTracked
    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "arbeidsgrad")))
    private Prosentsats arbeidsgrad = new Prosentsats(100);

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "gradering")))
    private Prosentsats gradering = new Prosentsats(0);

    SykefraværPeriodeEntitet() {
    }

    SykefraværPeriodeEntitet(SykefraværPeriode fraværPeriode) {
        this.periode = fraværPeriode.getPeriode();
        this.arbeidsgiver = fraværPeriode.getArbeidsgiver();
        this.arbeidsgrad = fraværPeriode.getArbeidsgrad();
        this.type = fraværPeriode.getType();
        fraværPeriode.getGradering().ifPresent(g -> this.gradering = g);
    }

    @Override
    public LocalDate getFom() {
        return periode.getFomDato();
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public SykefraværPeriodeType getType() {
        return type;
    }

    void setType(SykefraværPeriodeType type) {
        this.type = type;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public boolean getSkalGradere() {
        return getGradering().isPresent();
    }

    @Override
    public Optional<Prosentsats> getGradering() {
        return Optional.ofNullable(gradering);
    }

    void setGradering(Prosentsats gradering) {
        this.gradering = gradering;
    }

    @Override
    public Prosentsats getArbeidsgrad() {
        return arbeidsgrad;
    }

    void setArbeidsgrad(Prosentsats arbeidsgrad) {
        this.arbeidsgrad = arbeidsgrad;
    }

    void setSykefravær(SykefraværEntitet sykefraværEntitet) {
        this.sykefravær = sykefraværEntitet;
    }

    @Override
    public String toString() {
        return "SykefraværPeriodeEntitet{" +
            "periode=" + periode +
            ", arbeidsgiver=" + arbeidsgiver +
            ", arbeidsgrad=" + arbeidsgrad +
            ", gradering=" + gradering +
            '}';
    }

}
