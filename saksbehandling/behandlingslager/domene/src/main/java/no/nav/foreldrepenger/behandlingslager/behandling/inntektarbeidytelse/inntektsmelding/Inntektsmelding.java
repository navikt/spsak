package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public interface Inntektsmelding {
    /**
     * Virksomheten som har sendt inn inntektsmeldingen
     *
     * @return {@link Virksomhet}
     */
    Virksomhet getVirksomhet();

    /**
     * Liste over perioder med graderinger
     *
     * @return {@link Gradering}
     */
    List<Gradering> getGraderinger();

    /**
     * Liste over naturalytelser
     *
     * @return {@link NaturalYtelse}
     */
    List<NaturalYtelse> getNaturalYtelser();

    /**
     * Liste over Arbeidsgiverperiode
     *
     * @return {@link Arbeidsgiverperiode}
     */
    List<Arbeidsgiverperiode> getArbeidsgiverperiode();

    /**
     * Arbeidsgivers arbeidsforhold referanse
     *
     * @return {@link ArbeidsforholdRef}
     */
    ArbeidsforholdRef getArbeidsforholdRef();

    /**
     * Gjelder for et spesifikt arbeidsforhold
     *
     * @return {@link Boolean}
     */
    boolean gjelderForEtSpesifiktArbeidsforhold();

    /**
     * Startdato for permisjonen
     *
     * @return {@link LocalDate}
     */
    LocalDate getStartDatoPermisjon();

    /**
     * Referanse til journalposten som benyttes for å markere
     * hvilke dokument som er gjeldende i behandlingen
     *
     * @return {@link JournalpostId}
     */
    JournalpostId getJournalpostId();

    /**
     * Er det nær relasjon mellom søker og arbeidsgiver
     *
     * @return {@link Boolean}
     */
    boolean getErNærRelasjon();

    /**
     * Oppgitt årsinntekt fra arbeidsgiver
     *
     * @return {@link BigDecimal}
     */
    Beløp getInntektBeløp();

    /**
     * Beløpet arbeidsgiver ønsker refundert
     *
     * @return {@link BigDecimal}
     */
    Beløp getRefusjonBeløpPerMnd();

    /**
     * Dersom refusjonen opphører i stønadsperioden angis siste dag det søkes om refusjon for.
     *
     * @return {@link LocalDate}
     */
    LocalDate getRefusjonOpphører();

    /**
     * Liste over endringer i refusjonsbeløp
     *
     * @return {@Link Refusjon}
     */
    List<Refusjon> getEndringerRefusjon();

    /**
     * Beløp brutto utbetalt i arbeidsgiverperioden
     *
     * @return {@link BigDecimal}
     */
    Beløp getArbeidsgiverperiodeBruttoUtbetalt();


    InntektsmeldingInnsendingsårsak getInntektsmeldingInnsendingsårsak();

    LocalDateTime getInnsendingstidspunkt();

    default boolean gjelderSammeArbeidsforhold(Inntektsmelding annen) {
        return getVirksomhet().equals(annen.getVirksomhet())
            && getArbeidsforholdRef().gjelderFor(annen.getArbeidsforholdRef());
    }

}
