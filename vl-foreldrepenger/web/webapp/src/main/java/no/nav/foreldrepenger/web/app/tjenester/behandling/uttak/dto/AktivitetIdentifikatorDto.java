package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;

public class AktivitetIdentifikatorDto {

    private final UttakArbeidType uttakArbeidType;
    private final String arbeidsforholdOrgnr;
    private final String arbeidsforholdId;
    private final String arbeidsforholdNavn;

    public AktivitetIdentifikatorDto(UttakArbeidType uttakArbeidType, String arbeidsforholdOrgnr, String arbeidsforholdId, String arbeidsforholdNavn) {

        this.uttakArbeidType = uttakArbeidType;
        this.arbeidsforholdOrgnr = arbeidsforholdOrgnr;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsforholdNavn = arbeidsforholdNavn;
    }

    public AktivitetIdentifikatorDto(UttakAktivitetEntitet uttakAktivitet, String arbeidsforholdNavn) {
        this(uttakAktivitet.getUttakArbeidType(), uttakAktivitet.getArbeidsforholdOrgnr(), uttakAktivitet.getArbeidsforholdId(), arbeidsforholdNavn);
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

    public String getArbeidsforholdNavn() {
        return arbeidsforholdNavn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AktivitetIdentifikatorDto that = (AktivitetIdentifikatorDto) o;
        return Objects.equals(uttakArbeidType, that.uttakArbeidType) &&
            Objects.equals(arbeidsforholdOrgnr, that.arbeidsforholdOrgnr) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(arbeidsforholdNavn, that.arbeidsforholdNavn);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uttakArbeidType, arbeidsforholdOrgnr, arbeidsforholdId, arbeidsforholdNavn);
    }
}
