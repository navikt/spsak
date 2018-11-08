package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Map;

import no.nav.fpsak.tidsserie.LocalDateTimeline;

/** Output container benyttet i evaluering av Opptjeningsvilkår. */
public class OpptjeningsvilkårResultat {

    /** Resultatstruktur - Mellomliggende perioder */
    private Map<Aktivitet, LocalDateTimeline<Boolean>> akseptertMellomliggendePerioder;

    /** Resultatstruktur - antatt godkjente perioder med Arbeid (der vi ikke har fått inn inntekter ennå, men arbeidsforhold løper. */
    private Map<Aktivitet, LocalDateTimeline<Boolean>> antattGodkjente;

    /**
     * Resultatstruktur - perioder med aktivitet, avkortet eventuelt for underkjente perioder og gruppert etter
     * aktivitet.
     */
    private Map<Aktivitet, LocalDateTimeline<Boolean>> bekreftetGodkjentAktiviteter;

    /** Resultatstruktur - periode beregnet samlet, uttrykt i ISO8601 Period format (P5M24D = 5 måneder, 24 dager). */
    private OpptjentTidslinje resultat;

    /** Resultatstruktur - perioder underkjent i vurderingen, gruppert etter aktivitet. */
    private Map<Aktivitet, LocalDateTimeline<Boolean>> underkjentePerioder;

    /** Frist for å vente på opptjeningsopplysninger, hvis nødvendig. */
    private LocalDate frist;

    public OpptjeningsvilkårResultat() {
    }

    public Map<Aktivitet, LocalDateTimeline<Boolean>> getAkseptertMellomliggendePerioder() {
        return akseptertMellomliggendePerioder;
    }

    public Map<Aktivitet, LocalDateTimeline<Boolean>> getAntattGodkjentePerioder() {
        return Collections.unmodifiableMap(antattGodkjente);
    }

    public Map<Aktivitet, LocalDateTimeline<Boolean>> getBekreftetGodkjentePerioder() {
        return Collections.unmodifiableMap(bekreftetGodkjentAktiviteter);
    }

    public LocalDateTimeline<Boolean> getResultatTidslinje() {
        return resultat == null ? null : resultat.getTidslinje();
    }

    public Period getResultatOpptjent() {
        return resultat == null ? null : resultat.getOpptjentPeriode();
    }

    public Map<Aktivitet, LocalDateTimeline<Boolean>> getUnderkjentePerioder() {
        return Collections.unmodifiableMap(underkjentePerioder);
    }

    public void setAkseptertMellomliggendePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> mellomliggendePerioder) {
        this.akseptertMellomliggendePerioder = mellomliggendePerioder;
    }

    public void setAntattGodkjentePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> antattGodkjentePerioder) {
        this.antattGodkjente = antattGodkjentePerioder;
    }

    /** Set aktivitet bekreftet godkjent (uten mellomliggende peridoer, antatt godkjent eller bekreftet avviste perioder). */
    public void setBekreftetGodkjentAktivitet(Map<Aktivitet, LocalDateTimeline<Boolean>> aktivitetPerioder) {
        this.bekreftetGodkjentAktiviteter = aktivitetPerioder;
    }

    public void setTotalOpptjening(OpptjentTidslinje opptjent) {
        this.resultat = opptjent;
    }

    public void setUnderkjentePerioder(Map<Aktivitet, LocalDateTimeline<Boolean>> underkjentePerioder) {
        this.underkjentePerioder = underkjentePerioder;
    }

    public void setFrist(LocalDate frist) {
        this.frist = frist;
    }

    public LocalDate getFrist() {
        return frist;
    }

}
