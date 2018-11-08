package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Aktivitet;

public class Arbeidsforhold {
    private Aktivitet aktivitet;
    private String orgnr;
    private String arbeidsforholdId;
    private String aktørId;
    private ReferanseType referanseType;

    private Arbeidsforhold() {
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Aktivitet getAktivitet() {
        return aktivitet;
    }

    public boolean erFrilanser() {
        return aktivitet == Aktivitet.FRILANSINNTEKT;
    }

    public String getAktørId() {
        return aktørId;
    }

    public ReferanseType getReferanseType() {
        return referanseType;
    }

    @Override
    public boolean equals(Object annet) {
        if (!(annet instanceof Arbeidsforhold)) {
            return false;
        }
        Arbeidsforhold annetAF = (Arbeidsforhold)annet;
        return Objects.equals(aktivitet, annetAF.aktivitet)
                && Objects.equals(orgnr, annetAF.orgnr)
                && Objects.equals(aktørId, annetAF.aktørId)
                && Objects.equals(referanseType, annetAF.referanseType)
                && Objects.equals(arbeidsforholdId, annetAF.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitet, orgnr, aktørId, referanseType, arbeidsforholdId);
    }

    @Override
    public String toString() {
        return "<Arbeidsforhold "
                + "aktivitet " + aktivitet + ", "
                + "orgnr " + orgnr + ", "
                + "arbeidsforholdId " + arbeidsforholdId + ", "
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
            verifiserReferanseType();
            arbeidsforhold.orgnr = orgnr;
            arbeidsforhold.referanseType = ReferanseType.ORG_NR;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            arbeidsforhold.arbeidsforholdId = arbeidsforholdId;
            return this;
        }

        public Builder medAktivitet(Aktivitet aktivitet) {
            arbeidsforhold.aktivitet = aktivitet;
            return this;
        }

        public Builder medAktørId(String aktørId) {
            verifiserReferanseType();
            arbeidsforhold.aktørId = aktørId;
            arbeidsforhold.referanseType = ReferanseType.AKTØR_ID;
            return this;
        }

        public Arbeidsforhold build() {
            verifyForBuild();
            return arbeidsforhold;
        }

        private void verifyForBuild() {
            Objects.requireNonNull(arbeidsforhold.getAktivitet(), "aktivitet");
            if (!arbeidsforhold.getAktivitet().harOrgnr()) {
                arbeidsforhold.orgnr = null;
                arbeidsforhold.arbeidsforholdId = null;
            }
            if (arbeidsforhold.referanseType == ReferanseType.AKTØR_ID) {
                Objects.requireNonNull(arbeidsforhold.getAktørId(), "aktør id");
            } else if (arbeidsforhold.referanseType == ReferanseType.ORG_NR) {
                Objects.requireNonNull(arbeidsforhold.getOrgnr(), "organisasjonsnummer");
            }
        }

        private void verifiserReferanseType() {
            if (arbeidsforhold.referanseType != null) {
                throw new IllegalStateException("Referansetype er allerede satt på arbeidsforholdet: " + arbeidsforhold.referanseType);
            }
        }
    }

    public static Arbeidsforhold frilansArbeidsforhold() {
        return anonymtArbeidsforhold(Aktivitet.FRILANSINNTEKT);
    }

    public static Arbeidsforhold anonymtArbeidsforhold(Aktivitet aktivitet) {
        return Arbeidsforhold.builder().medAktivitet(aktivitet).build();
    }

    public static Arbeidsforhold nyttArbeidsforholdHosVirksomhet(String orgnr) {
        return Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnr).build();
    }

    public static Arbeidsforhold nyttArbeidsforholdHosVirksomhet(String orgnr, String arbeidsforholdId) {
        return Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnr).medArbeidsforholdId(arbeidsforholdId).build();
    }

    public static Arbeidsforhold nyttArbeidsforholdHosPrivatperson(String aktørId) {
        return Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medAktørId(aktørId).build();
    }

    public static Arbeidsforhold nyttArbeidsforholdHosPrivatperson(String aktørId, String arbeidsforholdId) {
        return Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medAktørId(aktørId).medArbeidsforholdId(arbeidsforholdId).build();
    }

}
