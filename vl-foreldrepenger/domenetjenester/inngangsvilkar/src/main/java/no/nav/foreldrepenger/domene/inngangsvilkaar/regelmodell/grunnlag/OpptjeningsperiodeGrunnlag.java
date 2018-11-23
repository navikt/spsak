package no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;
import java.time.Period;

public class OpptjeningsperiodeGrunnlag implements VilkårGrunnlag {

    // Input til regel
    private LocalDate førsteDagIArbeidsgiverPerioden;
    private LocalDate førsteDagISøknad;
    private LocalDate førsteDagISykemelding;

    private Period periodeLengde;

    // Settes i løpet av regelevaluering

    private LocalDate skjæringsdatoOpptjening;
    private LocalDate opptjeningsperiodeFom;
    private LocalDate opptjeningsperiodeTom;

    public OpptjeningsperiodeGrunnlag() {
    }

    public OpptjeningsperiodeGrunnlag(LocalDate førsteDagIArbeidsgiverPerioden,
                                      LocalDate førsteDagISøknad,
                                      LocalDate førsteDagISykemelding) {
        this.førsteDagIArbeidsgiverPerioden = førsteDagIArbeidsgiverPerioden;
        this.førsteDagISøknad = førsteDagISøknad;
        this.førsteDagISykemelding = førsteDagISykemelding;
    }

    public LocalDate getFørsteDagIArbeidsgiverPerioden() {
        return førsteDagIArbeidsgiverPerioden;
    }

    public LocalDate getFørsteDagISøknad() {
        return førsteDagISøknad;
    }

    public LocalDate getFørsteDagISykemelding() {
        return førsteDagISykemelding;
    }

    public Period getPeriodeLengde() {
        return periodeLengde;
    }

    public void setPeriodeLengde(Period periodeLengde) {
        this.periodeLengde = periodeLengde;
    }

    public LocalDate getSkjæringsdatoOpptjening() {
        return skjæringsdatoOpptjening;
    }

    public void setSkjæringsdatoOpptjening(LocalDate skjæringsdatoOpptjening) {
        this.skjæringsdatoOpptjening = skjæringsdatoOpptjening;
    }

    public LocalDate getOpptjeningsperiodeFom() {
        return opptjeningsperiodeFom;
    }

    public void setOpptjeningsperiodeFom(LocalDate opptjeningsperiodeFom) {
        this.opptjeningsperiodeFom = opptjeningsperiodeFom;
    }

    public LocalDate getOpptjeningsperiodeTom() {
        return opptjeningsperiodeTom;
    }

    public void setOpptjeningsperiodeTom(LocalDate opptjeningsperiodeTom) {
        this.opptjeningsperiodeTom = opptjeningsperiodeTom;
    }

    @Override
    public String toString() {
        return "OpptjeningsperiodeGrunnlag{" +
            ", førsteDagIArbeidsgiverPerioden=" + førsteDagIArbeidsgiverPerioden +
            ", førsteDagISøknad=" + førsteDagISøknad +
            ", førsteDagISykemelding=" + førsteDagISykemelding +
            ", periodeLengde=" + periodeLengde +
            '}';
    }
}


