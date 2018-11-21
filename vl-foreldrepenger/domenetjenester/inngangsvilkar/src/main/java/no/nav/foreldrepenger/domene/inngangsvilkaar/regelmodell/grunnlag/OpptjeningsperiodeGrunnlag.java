package no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;
import java.time.Period;

import no.nav.foreldrepenger.domene.inngangsvilkaar.FagsakÅrsak;

public class OpptjeningsperiodeGrunnlag implements VilkårGrunnlag {

    // Input til regel
    private FagsakÅrsak fagsakÅrsak;
    private LocalDate førsteUttaksDato;
    private LocalDate hendelsesDato;

    private Period periodeLengde;

    private Period tidligsteUttakFørFødselPeriode;
    // Settes i løpet av regelevaluering

    private LocalDate skjæringsdatoOpptjening;
    private LocalDate opptjeningsperiodeFom;
    private LocalDate opptjeningsperiodeTom;
    public OpptjeningsperiodeGrunnlag() {
    }

    public OpptjeningsperiodeGrunnlag(FagsakÅrsak fagsakÅrsak, LocalDate førsteUttaksDato, LocalDate hendelsesDato) {
        this.fagsakÅrsak = fagsakÅrsak;
        this.førsteUttaksDato = førsteUttaksDato;
        this.hendelsesDato = hendelsesDato;
    }

    public FagsakÅrsak getFagsakÅrsak() {
        return fagsakÅrsak;
    }

    public LocalDate getFørsteUttaksDato() {
        return førsteUttaksDato;
    }

    public LocalDate getHendelsesDato() {
        return hendelsesDato;
    }

    public Period getPeriodeLengde() { return periodeLengde; }

    public void setFagsakÅrsak(FagsakÅrsak fagsakÅrsak) {
        this.fagsakÅrsak = fagsakÅrsak;
    }

    public void setFørsteUttaksDato(LocalDate førsteUttaksDato) {
        this.førsteUttaksDato = førsteUttaksDato;
    }

    public void setHendelsesDato(LocalDate hendelsesDato) {
        this.hendelsesDato = hendelsesDato;
    }

    public void setPeriodeLengde(Period periodeLengde) {
        this.periodeLengde = periodeLengde;
    }

    public LocalDate getSkjæringsdatoOpptjening() {
        return skjæringsdatoOpptjening;
    }

    public LocalDate getOpptjeningsperiodeFom() {
        return opptjeningsperiodeFom;
    }

    public LocalDate getOpptjeningsperiodeTom() {
        return opptjeningsperiodeTom;
    }

    public void setSkjæringsdatoOpptjening(LocalDate skjæringsdatoOpptjening) {
        this.skjæringsdatoOpptjening = skjæringsdatoOpptjening;
    }

    public void setOpptjeningsperiodeFom(LocalDate opptjeningsperiodeFom) {
        this.opptjeningsperiodeFom = opptjeningsperiodeFom;
    }

    public void setOpptjeningsperiodeTom(LocalDate opptjeningsperiodeTom) {
        this.opptjeningsperiodeTom = opptjeningsperiodeTom;
    }

    public Period getTidligsteUttakFørFødselPeriode() {
        return tidligsteUttakFørFødselPeriode;
    }

    public void setTidligsteUttakFørFødselPeriode(Period tidligsteUttakFørFødselPeriode) {
        this.tidligsteUttakFørFødselPeriode = tidligsteUttakFørFødselPeriode;
    }

}


