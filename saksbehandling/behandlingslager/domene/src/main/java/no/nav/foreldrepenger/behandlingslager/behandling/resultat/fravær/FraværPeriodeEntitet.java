package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "FraværPeriode")
@Table(name = "FR_FRAVAER_PERIODE")
public class FraværPeriodeEntitet extends BaseEntitet implements FraværPeriode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FR_FRAVAER_PERIODE")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "perioder_id", updatable = false, nullable = false)
    private FraværPerioderEntitet perioder;

    @ChangeTracked
    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @ChangeTracked
    @Embedded
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "graderings_prosent")))
    private Prosentsats graderingProsent = new Prosentsats(0);

    @ChangeTracked
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "gradering", nullable = false)
    private boolean gradering = false;

    FraværPeriodeEntitet() {

    }

    FraværPeriodeEntitet(FraværPeriode periode) {
        this.arbeidsgiver = periode.getArbeidsgiver();
        this.periode = periode.getPeriode();
        this.gradering = periode.getErGradert();
        periode.getGradering().ifPresent(it -> this.graderingProsent = it);
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
    public boolean getErGradert() {
        return gradering;
    }

    @Override
    public Optional<Prosentsats> getGradering() {
        return Optional.ofNullable(graderingProsent);
    }

    void setPerioder(FraværPerioderEntitet perioder) {
        this.perioder = perioder;
    }

    @Override
    public String toString() {
        return "FraværPeriodeEntitet{" +
            "id=" + id +
            ", perioder=" + perioder +
            ", arbeidsgiver=" + arbeidsgiver +
            ", periode=" + periode +
            ", graderingProsent=" + graderingProsent +
            ", gradering=" + gradering +
            '}';
    }
}
