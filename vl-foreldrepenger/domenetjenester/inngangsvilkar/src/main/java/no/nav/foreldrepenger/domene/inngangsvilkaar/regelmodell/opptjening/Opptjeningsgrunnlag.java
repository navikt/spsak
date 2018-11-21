package no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.VilkårGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;
import no.nav.fpsak.tidsserie.LocalDateInterval;

/**
 * Input for vurdering av opptjeningsvilkår. Består av sett av AktivitetPeriodeInput for ulike aktivitetet og eventuelt
 * ulike arbeidsgivere.
 */
@RuleDocumentationGrunnlag
public class Opptjeningsgrunnlag implements VilkårGrunnlag {

    /** Input med aktiviteter som skal inngå i vurderingen. */
    @JsonProperty("aktivitetPerioder")
    private final List<AktivitetPeriode> aktivitetPerioder = new ArrayList<>();

    /** Behandlingstidspunkt - normalt dagens dato. */
    @JsonProperty("behandlingsDato")
    private LocalDate behandlingsDato;

    /** Første dato med opptjening som teller med. (fra-og-med) */
    @JsonProperty("forsteDatoOpptjening")
    private LocalDate førsteDatoOpptjening;

    /** Skjæringstidspunkt (siste punkt med opptjening). (til-og-med) */
    @JsonProperty("sisteDatoForOpptjening")
    private LocalDate sisteDatoForOpptjening;

    /** Input med inntekter som benyttes i vurderingen for å avsjekke Arbeidsforhold (fra AAReg). */
    @JsonProperty("inntektPerioder")
    private final List<InntektPeriode> inntektPerioder = new ArrayList<>();

    /**
     * Maks periode i en mellomliggende periode for et arbeidsforhold for at den skal kunne regnes med.
     */
    @JsonIgnore
    private Period maksMellomliggendePeriodeForArbeidsforhold = Period.ofDays(14);

    /**
     * Minste periode for en foregående periode i et arbeidsforhold for at en mellomliggende periode skal regnes med.
     */
    @JsonIgnore
    private Period minForegåendeForMellomliggendePeriodeForArbeidsforhold = Period.ofWeeks(4);

    /**
     * Minste antall dager med bekreftet opptjening for å kunne legge på vent (sees i sammenheng med
     * {@link #minsteAntallMånederForVent}.
     */
    @JsonIgnore
    private int minsteAntallDagerForVent = 0;

    /**
     * Minste antall dager som kreves godkjent dersom det er færre enn ({@link #minsteAntallMånederGodkjent}+1 måneder .
     */
    @JsonIgnore
    private int minsteAntallDagerGodkjent = 26;

    /**
     * Minste antall måneder med bekreftet opptjening for å kunne legge på vent (sees i sammenheng med
     * {@link #minsteAntallDagerForVent}.
     */
    @JsonIgnore
    private int minsteAntallMånederForVent = 4;

    /**
     * Minste antall måneder som kreves godkjent. Hvis eksakt samme, så sjekkes også {@link #minsteAntallDagerGodkjent}.
     */
    @JsonIgnore
    private int minsteAntallMånederGodkjent = 5;

    /** Minste godkjente inntekt i en periode. */
    @JsonIgnore
    private final Long minsteInntekt = 1L; // NOSONAR

    /**
     * Periode før behandlingstidspunkt hvor arbeid kan antas godkjent selv om ikke inntekter er rapportert inn ennå.
     */
    @JsonIgnore
    private final Period periodeAntattGodkjentFørBehandlingstidspunkt = Period.ofMonths(2); // NOSONAR

    @JsonCreator
    protected Opptjeningsgrunnlag() {
    }

    public Opptjeningsgrunnlag(LocalDate behandlingstidspunkt,
                               LocalDate startDato,
                               LocalDate skjæringstidspunkt) {
        this.behandlingsDato = behandlingstidspunkt;
        this.førsteDatoOpptjening = startDato;
        this.sisteDatoForOpptjening = skjæringstidspunkt;
    }

    public List<AktivitetPeriode> getAktivitetPerioder() {
        return Collections.unmodifiableList(new ArrayList<>(aktivitetPerioder));
    }

    public LocalDate getBehandlingsTidspunkt() {
        return behandlingsDato;
    }

    public LocalDate getFørsteDatoIOpptjening() {
        return førsteDatoOpptjening;
    }

    public List<InntektPeriode> getInntektPerioder() {
        return Collections.unmodifiableList(new ArrayList<>(inntektPerioder));
    }

    public Period getMaksMellomliggendePeriodeForArbeidsforhold() {
        return maksMellomliggendePeriodeForArbeidsforhold;
    }

    public Period getMinForegåendeForMellomliggendePeriodeForArbeidsforhold() {
        return minForegåendeForMellomliggendePeriodeForArbeidsforhold;
    }

    public int getMinsteAntallDagerForVent() {
        return minsteAntallDagerForVent;
    }

    public int getMinsteAntallDagerGodkjent() {
        return minsteAntallDagerGodkjent;
    }

    public int getMinsteAntallMånederForVent() {
        return minsteAntallMånederForVent;
    }

    public int getMinsteAntallMånederGodkjent() {
        return minsteAntallMånederGodkjent;
    }

    public Long getMinsteInntekt() {
        return minsteInntekt;
    }

    public LocalDateInterval getOpptjeningPeriode() {
        return new LocalDateInterval(førsteDatoOpptjening, sisteDatoForOpptjening);
    }

    public Period getPeriodeAntattGodkjentFørBehandlingstidspunkt() {
        return periodeAntattGodkjentFørBehandlingstidspunkt;
    }

    public LocalDate getSisteDatoForOpptjening() {
        return sisteDatoForOpptjening;
    }

    public void leggTil(AktivitetPeriode input) {
        aktivitetPerioder.add(input);
    }

    public void leggTil(InntektPeriode input) {
        inntektPerioder.add(input);
    }

    /**
     * Legg til aktivitet for en angitt intervall
     *
     * @param datoIntervall
     *            - intervall
     * @param aktivitet
     *            - aktivitet
     */
    // TODO(OJR) fiks ved å endre testdekning
    public void leggTil(LocalDateInterval datoIntervall, Aktivitet aktivitet) {
        leggTil(new AktivitetPeriode(datoIntervall, aktivitet, AktivitetPeriode.VurderingsStatus.TIL_VURDERING));
    }

    public void leggTilRapportertInntekt(LocalDateInterval datoInterval, Aktivitet aktivitet, Long kronerInntekt) {
        InntektPeriode periodeInntekt = new InntektPeriode(datoInterval, aktivitet, kronerInntekt);
        inntektPerioder.add(periodeInntekt);
    }

    public void setMaksMellomliggendePeriodeForArbeidsforhold(Period maksMellomliggendePeriodeForArbeidsforhold) {
        this.maksMellomliggendePeriodeForArbeidsforhold = maksMellomliggendePeriodeForArbeidsforhold;
    }

    public void setMinForegåendeForMellomliggendePeriodeForArbeidsforhold(Period minForegåendeForMellomliggendePeriodeForArbeidsforhold) {
        this.minForegåendeForMellomliggendePeriodeForArbeidsforhold = minForegåendeForMellomliggendePeriodeForArbeidsforhold;
    }

    public void setMinsteAntallDagerForVent(int minsteAntallDagerForVent) {
        this.minsteAntallDagerForVent = minsteAntallDagerForVent;
    }

    public void setMinsteAntallDagerGodkjent(int minsteAntallDagerGodkjent) {
        this.minsteAntallDagerGodkjent = minsteAntallDagerGodkjent;
    }

    public void setMinsteAntallMånederForVent(int minsteAntallMånederForVent) {
        this.minsteAntallMånederForVent = minsteAntallMånederForVent;
    }

    public void setMinsteAntallMånederGodkjent(int minsteAntallMånederGodkjent) {
        this.minsteAntallMånederGodkjent = minsteAntallMånederGodkjent;
    }
}
