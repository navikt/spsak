package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class FinnMedlemRequest {

    private PersonIdent personIdent;
    private LocalDate fom;
    private LocalDate tom;

    public FinnMedlemRequest(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        this.personIdent = personIdent;
        this.fom = fom;
        this.tom = tom;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getFnr() {
        return personIdent.getIdent();
    }

}
