package no.nav.foreldrepenger.domene.familiehendelse.fødsel;


import static no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType.FØDSEL;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.hendelser.Forretningshendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class FødselForretningshendelse extends Forretningshendelse {

    private List<AktørId> aktørIdListe;
    private LocalDate fødselsdato;

    public FødselForretningshendelse(List<AktørId> aktørIdListe, LocalDate fødselsdato) {
        super(FØDSEL);
        this.aktørIdListe = aktørIdListe;
        this.fødselsdato = fødselsdato;
    }

    public List<AktørId> getAktørIdListe() {
        return aktørIdListe;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }
}
