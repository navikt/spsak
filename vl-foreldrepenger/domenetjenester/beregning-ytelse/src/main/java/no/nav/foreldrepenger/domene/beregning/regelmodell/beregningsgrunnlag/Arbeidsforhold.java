package no.nav.foreldrepenger.domene.beregning.regelmodell.beregningsgrunnlag;

import java.util.Objects;

public class Arbeidsforhold {
    private String orgnr;
    private String arbeidsforholdId;
    private boolean frilanser;

    Arbeidsforhold() {
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public boolean erFrilanser() {
        return frilanser;
    }

    @Override
    public boolean equals(Object annet) {
        if (annet == null || !(annet instanceof Arbeidsforhold)) {
            return false;
        }
        Arbeidsforhold annetAF = (Arbeidsforhold)annet;
        return Objects.equals(frilanser, annetAF.frilanser)
                && Objects.equals(orgnr, annetAF.orgnr)
                && Objects.equals(arbeidsforholdId, annetAF.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frilanser, orgnr, arbeidsforholdId);
    }

    @Override
    public String toString() {
        return "<Arbeidsforhold "
                + "orgnr " + orgnr + ", "
                + "arbeidsforholdId " + arbeidsforholdId + ", "
                + "frilanser " + frilanser + ", "
                + ">";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Arbeidsforhold arbeidsforhold;

        private Builder() {
            arbeidsforhold = new Arbeidsforhold();
        }

        public Builder medOrgnr(String orgnr) {
            arbeidsforhold.orgnr = orgnr;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            arbeidsforhold.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medFrilanser(boolean frilanser) {
            arbeidsforhold.frilanser = frilanser;
            return this;
        }

        public Arbeidsforhold build() {
            verifyForBuild();
            return arbeidsforhold;
        }

        private void verifyForBuild() {
            if (arbeidsforhold.frilanser) {
                arbeidsforhold.orgnr = null;
                arbeidsforhold.arbeidsforholdId = null;
            }
        }
    }

    public static Arbeidsforhold frilansArbeidsforhold() {
        return Arbeidsforhold.builder().medFrilanser(true).build();
    }

    public static Arbeidsforhold nyttArbeidsforhold(String orgnr) {
        return Arbeidsforhold.builder().medOrgnr(orgnr).build();
    }

    public static Arbeidsforhold nyttArbeidsforhold(String orgnr, String arbeidsforholdId) {
        return Arbeidsforhold.builder().medOrgnr(orgnr).medArbeidsforholdId(arbeidsforholdId).build();
    }
}
