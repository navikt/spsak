package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;

public class ArbeidsforholdIdentifikator {
    private Arbeidsgiver arbeidsgiver;
    private ArbeidsforholdRef arbeidsforholdId;
    private String type;

    public ArbeidsforholdIdentifikator(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdId, String type) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.type = type;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public ArbeidsforholdRef getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public String getType() {
        return type;
    }

    public boolean harArbeidsforholdRef() {
        return arbeidsforholdId != null;
    }

    @Override
    public String toString() {
        return "ArbeidsforholdIdentifikator{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", arbeidsforholdId=" + arbeidsforholdId +
            ", type='" + type + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdIdentifikator that = (ArbeidsforholdIdentifikator) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdId, type);
    }
}
