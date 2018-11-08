package no.nav.foreldrepenger.domene.uttak.saldo;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;

public class Aktivitet {

    private UttakArbeidType uttakArbeidType;
    private String arbeidsforholdOrgnr;
    private String arbeidsforholdId;

    public Aktivitet(UttakArbeidType uttakArbeidType, String arbeidsforholdOrgnr, String arbeidsforholdId) {
        this.uttakArbeidType = uttakArbeidType;
        this.arbeidsforholdOrgnr = arbeidsforholdOrgnr;
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }

    public String getArbeidsforholdOrgnr() {
        return arbeidsforholdOrgnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Aktivitet aktivitet = (Aktivitet) o;

        if (uttakArbeidType != null ? !uttakArbeidType.equals(aktivitet.uttakArbeidType) : aktivitet.uttakArbeidType != null)
            return false;
        if (arbeidsforholdOrgnr != null ? !arbeidsforholdOrgnr.equals(aktivitet.arbeidsforholdOrgnr) : aktivitet.arbeidsforholdOrgnr != null)
            return false;
        return arbeidsforholdId != null ? arbeidsforholdId.equals(aktivitet.arbeidsforholdId) : aktivitet.arbeidsforholdId == null;
    }

    @Override
    public int hashCode() {
        int result = uttakArbeidType != null ? uttakArbeidType.hashCode() : 0;
        result = 31 * result + (arbeidsforholdOrgnr != null ? arbeidsforholdOrgnr.hashCode() : 0);
        result = 31 * result + (arbeidsforholdId != null ? arbeidsforholdId.hashCode() : 0);
        return result;
    }
}
