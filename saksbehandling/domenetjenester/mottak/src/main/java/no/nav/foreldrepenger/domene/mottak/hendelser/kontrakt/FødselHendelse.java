package no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt;

import java.time.LocalDate;
import java.util.List;

public class FødselHendelse extends Hendelse {

    private List<String> aktørIdListe;
    private LocalDate fødselsdato;

    public FødselHendelse() {
        super("FØDSEL");
        // Jackson
    }

    public FødselHendelse(List<String> aktørIdListe, LocalDate fødselsdato) {
        super("FØDSEL");
        this.aktørIdListe = aktørIdListe;
        this.fødselsdato = fødselsdato;
    }

    public List<String> getAktørIdListe() {
        return aktørIdListe;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }
}
