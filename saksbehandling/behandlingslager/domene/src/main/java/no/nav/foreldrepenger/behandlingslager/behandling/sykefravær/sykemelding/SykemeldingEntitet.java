package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding;

import java.util.Objects;

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

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "SykemeldingEntitet")
@Table(name = "SF_SYKEMELDING")
class SykemeldingEntitet extends BaseEntitet implements Sykemelding {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SF_SYKEMELDING")
    private Long id;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til parent! */}, fetch = FetchType.LAZY)
    @JoinColumn(name = "sykemeldinger_id", updatable = false, nullable = false)
    private SykemeldingerEntitet sykemeldinger;

    @ChangeTracked
    @Column(name = "ekstern_referanse", nullable = false, updatable = false)
    private String eksternReferanse;

    @ChangeTracked
    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "grad")))
    private Prosentsats grad;

    SykemeldingEntitet() {
    }

    SykemeldingEntitet(Sykemelding sykemelding) {
        this.periode = sykemelding.getPeriode();
        this.arbeidsgiver = sykemelding.getArbeidsgiver();
        this.grad = sykemelding.getGrad();
        this.eksternReferanse = sykemelding.getEksternReferanse();
    }

    void setSykemeldinger(SykemeldingerEntitet sykemeldinger) {
        this.sykemeldinger = sykemeldinger;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public Prosentsats getGrad() {
        return grad;
    }

    void setGrad(Prosentsats grad) {
        this.grad = grad;
    }

    @Override
    public String getEksternReferanse() {
        return eksternReferanse;
    }

    void setEksternReferanse(String eksternReferanse) {
        this.eksternReferanse = eksternReferanse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SykemeldingEntitet that = (SykemeldingEntitet) o;
        return Objects.equals(eksternReferanse, that.eksternReferanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eksternReferanse);
    }

    @Override
    public String toString() {
        return "SykemeldingEntitet{" +
            "eksternReferanse=" + eksternReferanse +
            "periode=" + periode +
            ", arbeidsgiver=" + arbeidsgiver +
            ", grad=" + grad +
            '}';
    }
}
