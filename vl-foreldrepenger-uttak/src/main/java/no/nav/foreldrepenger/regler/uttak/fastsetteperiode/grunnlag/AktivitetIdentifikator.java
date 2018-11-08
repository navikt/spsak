package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.util.Objects;

public class AktivitetIdentifikator {

    public enum ArbeidsgiverType {
        PERSON, VIRKSOMHET
    }

    private final AktivitetType aktivitetType;
    private final String arbeidsforholdId;
    private final String arbeidsgiverIdentifikator;
    private final ArbeidsgiverType arbeidsgiverType;

    private AktivitetIdentifikator(AktivitetType aktivitetType, String arbeidsforholdIdentifikator, String arbeidsforholdId, ArbeidsgiverType arbeidsgiverType) {
        this.aktivitetType = aktivitetType;
        this.arbeidsgiverType = arbeidsgiverType;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsgiverIdentifikator = arbeidsforholdIdentifikator;
    }

    private AktivitetIdentifikator(AktivitetType aktivitetType, ArbeidsgiverType arbeidsgiverType) {
        this(aktivitetType, null, null, arbeidsgiverType);
    }

    public static AktivitetIdentifikator forArbeid(String arbeidsgiverIdentifikator, String arbeidsforholdId) {
        return forArbeid(arbeidsgiverIdentifikator, arbeidsforholdId, ArbeidsgiverType.VIRKSOMHET);
    }

    public static AktivitetIdentifikator forArbeid(String arbeidsgiverIdentifikator, String arbeidsforholdId, ArbeidsgiverType arbeidsgiverType) {
        return new AktivitetIdentifikator(AktivitetType.ARBEID, arbeidsgiverIdentifikator, arbeidsforholdId, arbeidsgiverType);
    }

    public static AktivitetIdentifikator forSelvstendigNæringsdrivende() {
        return new AktivitetIdentifikator(AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE, null, null, null);
    }

    public static AktivitetIdentifikator forFrilans() {
        return new AktivitetIdentifikator(AktivitetType.FRILANS, null, null, null);
    }

    public static AktivitetIdentifikator annenAktivitet() {
        return new AktivitetIdentifikator(AktivitetType.ANNET, null);
    }

    public AktivitetType getAktivitetType() {
        return aktivitetType;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public ArbeidsgiverType getArbeidsgiverType() {
        return arbeidsgiverType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AktivitetIdentifikator that = (AktivitetIdentifikator) o;
        return arbeidsgiverType == that.arbeidsgiverType &&
                aktivitetType == that.aktivitetType &&
                Objects.equals(arbeidsgiverIdentifikator, that.arbeidsgiverIdentifikator) &&
                Objects.equals(arbeidsforholdId, that.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetType, arbeidsgiverIdentifikator, arbeidsforholdId, arbeidsgiverType);
    }

    @Override
    public String toString() {
        return "AktivitetIdentifikator{" +
                "aktivitetType=" + aktivitetType +
                ", arbeidsgiverIdentifikator='" + arbeidsgiverIdentifikator + '\'' +
                ", arbeidsforholdId='" + arbeidsforholdId + '\'' +
                ", arbeidsgiverType=" + arbeidsgiverType +
                '}';
    }
}
