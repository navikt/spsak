package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;


public class TpsPerson {

    AktørId aktørId;
    String fnr;
    public String kjønn;
    public String fornavn;
    public String etternavn;

    public Bruker person;

    public TpsPerson(AktørId aktørId, PersonBygger personBygger) {
        this.aktørId = aktørId;
        this.fnr = personBygger.getFnr();
        this.person = personBygger.bygg();
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public String getFnr() {
        return fnr;
    }

    public PersonIdent getPersonIdent() {
        return new PersonIdent(fnr);
    }

    @Override
    public String toString() {
        return "TpsPerson{" +
                ", aktørId=" + aktørId +
                ", fnr='" + fnr + '\'' +
                ", person=" + person +
                '}';
    }
}
