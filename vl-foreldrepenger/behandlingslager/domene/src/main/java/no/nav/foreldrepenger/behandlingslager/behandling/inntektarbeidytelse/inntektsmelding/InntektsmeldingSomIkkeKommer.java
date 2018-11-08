package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;

public class InntektsmeldingSomIkkeKommer {

    private Arbeidsgiver arbeidsgiver;
    private ArbeidsforholdRef ref;

    public InntektsmeldingSomIkkeKommer(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        this.arbeidsgiver = arbeidsgiver;
        this.ref = ref;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public ArbeidsforholdRef getRef() {
        return ref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InntektsmeldingSomIkkeKommer that = (InntektsmeldingSomIkkeKommer) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(ref, that.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, ref);
    }

    @Override
    public String toString() {
        return "InntektsmeldingSomIkkeKommer{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", ref=" + ref +
            '}';
    }
}
