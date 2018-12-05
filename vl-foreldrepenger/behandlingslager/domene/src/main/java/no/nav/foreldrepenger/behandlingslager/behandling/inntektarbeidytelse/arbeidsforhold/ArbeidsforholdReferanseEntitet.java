package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.util.Objects;

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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;

@Entity(name = "ArbeidsforholdReferanse")
@Table(name = "IAY_ARBEIDSFORHOLD_REFER")
public class ArbeidsforholdReferanseEntitet extends BaseEntitet implements IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_ARBEIDSFORHOLD_REFER")
    private Long id;

    @ChangeTracked
    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "referanse", column = @Column(name = "intern_referanse", nullable = false))
    })
    private ArbeidsforholdRef internReferanse;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "referanse", column = @Column(name = "ekstern_referanse", nullable = false))
    })
    private ArbeidsforholdRef eksternReferanse;

    @ManyToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true, nullable = false)
    private ArbeidsforholdInformasjonEntitet informasjon;

    ArbeidsforholdReferanseEntitet() {
    }

    ArbeidsforholdReferanseEntitet(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef internReferanse, ArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiver = arbeidsgiver;
        this.internReferanse = internReferanse;
        this.eksternReferanse = eksternReferanse;
    }

    ArbeidsforholdReferanseEntitet(ArbeidsforholdReferanseEntitet arbeidsforholdInformasjonEntitet) {
        this(arbeidsforholdInformasjonEntitet.arbeidsgiver, arbeidsforholdInformasjonEntitet.internReferanse, arbeidsforholdInformasjonEntitet.eksternReferanse);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(internReferanse, eksternReferanse);
    }

    ArbeidsforholdRef getInternReferanse() {
        return internReferanse;
    }

    ArbeidsforholdRef getEksternReferanse() {
        return eksternReferanse;
    }

    Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setInformasjon(ArbeidsforholdInformasjonEntitet informasjon) {
        this.informasjon = informasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArbeidsforholdReferanseEntitet that = (ArbeidsforholdReferanseEntitet) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(internReferanse, that.internReferanse) &&
            Objects.equals(eksternReferanse, that.eksternReferanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internReferanse, eksternReferanse);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdReferanseEntitet{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", internReferanse=" + internReferanse +
            ", eksternReferanse=" + eksternReferanse +
            '}';
    }
}
