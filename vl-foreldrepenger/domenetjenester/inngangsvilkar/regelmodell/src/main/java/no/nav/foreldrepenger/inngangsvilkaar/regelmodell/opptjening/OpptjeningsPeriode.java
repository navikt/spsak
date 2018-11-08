package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening;

import java.time.LocalDate;

/** Resultat av vurdering av Opptjeningsvilkår */
public class OpptjeningsPeriode {

    private LocalDate opptjeningsperiodeFom;
    private LocalDate opptjeningsperiodeTom;

    public OpptjeningsPeriode() {
    }

    public OpptjeningsPeriode(LocalDate opptjeningsperiodeFom, LocalDate opptjeningsperiodeTom) {
        this.opptjeningsperiodeFom = opptjeningsperiodeFom;
        this.opptjeningsperiodeTom = opptjeningsperiodeTom;
    }

    public LocalDate getOpptjeningsperiodeFom() {
        return opptjeningsperiodeFom;
    }

    public LocalDate getOpptjeningsperiodeTom() {
        return opptjeningsperiodeTom;
    }

    public void setOpptjeningsperiodeFom(LocalDate opptjeningsperiodeFom) {
        this.opptjeningsperiodeFom = opptjeningsperiodeFom;
    }

    public void setOpptjeningsperiodeTom(LocalDate opptjeningsperiodeTom) {
        this.opptjeningsperiodeTom = opptjeningsperiodeTom;
    }
}
