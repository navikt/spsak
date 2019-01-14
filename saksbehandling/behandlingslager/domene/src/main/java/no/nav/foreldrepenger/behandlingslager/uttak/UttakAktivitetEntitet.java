package no.nav.foreldrepenger.behandlingslager.uttak;

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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity
@Table(name = "UTTAK_AKTIVITET")
public class UttakAktivitetEntitet extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_AKTIVITET")
    private Long id;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    @ChangeTracked
    private ArbeidsforholdRef arbeidsforholdRef;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "UTTAK_ARBEID_TYPE", referencedColumnName = "kode")),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "kl_uttak_arbeid_type"))})
    private UttakArbeidType uttakArbeidType;

    public Long getId() {
        return id;
    }

    public String getArbeidsforholdOrgnr() {
        if (arbeidsgiver == null) {
            return null;
        }
        return arbeidsgiver.getVirksomhet().getOrgnr();
    }

    public String getArbeidsforholdId() {
        if (arbeidsforholdRef == null) {
            return null;
        }
        return arbeidsforholdRef.getReferanse();
    }

    public VirksomhetEntitet getVirksomhet() {
        if (arbeidsgiver == null) {
            return null;
        }
        return (VirksomhetEntitet) arbeidsgiver.getVirksomhet();
    }

    public ArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }

    @Override
    public String toString() {
        String tomStreng = "";
        return "UttakAktivitetEntitet{" +
            "id=" + id +
            ", arbeidsgiver='" + (arbeidsgiver != null ? arbeidsgiver : tomStreng) + '\'' +
            ", arbeidsforholdRef='" + (arbeidsforholdRef != null ? arbeidsforholdRef.toString() : tomStreng) + '\'' +
            ", uttakArbeidType=" + uttakArbeidType.getKode() +
            '}';
    }

    @Override
    public boolean equals(Object annen) {
        if (annen == this) {
            return true;
        }
        if (!(annen instanceof UttakAktivitetEntitet)) {
            return false;
        }

        UttakAktivitetEntitet uttakAktivitet = (UttakAktivitetEntitet) annen;
        return Objects.equals(this.getArbeidsforholdId(), uttakAktivitet.getArbeidsforholdId()) &&
            Objects.equals(this.getArbeidsforholdOrgnr(), uttakAktivitet.getArbeidsforholdOrgnr()) &&
            Objects.equals(this.getUttakArbeidType(), uttakAktivitet.getUttakArbeidType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsforholdId(), getArbeidsforholdOrgnr(), getUttakArbeidType());
    }

    public static class Builder {

        private UttakAktivitetEntitet kladd = new UttakAktivitetEntitet();

        public Builder medArbeidsforhold(VirksomhetEntitet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
            if (virksomhet != null) {
                kladd.arbeidsgiver = Arbeidsgiver.virksomhet(virksomhet);
            }
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medUttakArbeidType(UttakArbeidType uttakArbeidType) {
            kladd.uttakArbeidType = uttakArbeidType;
            return this;
        }

        public UttakAktivitetEntitet build() {
            Objects.requireNonNull(kladd.uttakArbeidType, "uttakArbeidType");
            return kladd;
        }
    }
}
