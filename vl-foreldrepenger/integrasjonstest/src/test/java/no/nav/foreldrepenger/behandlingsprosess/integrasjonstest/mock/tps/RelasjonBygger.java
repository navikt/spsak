package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjoner;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;


public class RelasjonBygger {

    private TpsRelasjon tpsRelasjon;

    public RelasjonBygger(TpsRelasjon tpsRelasjon) {
        this.tpsRelasjon = tpsRelasjon;
    }


    public Bruker byggFor(Bruker bruker) {
        Familierelasjon familierelasjon = new Familierelasjon();
        familierelasjon.setHarSammeBosted(true);
        Familierelasjoner familierelasjoner = new Familierelasjoner();
        familierelasjoner.setValue(tpsRelasjon.relasjonsType);
        familierelasjon.setTilRolle(familierelasjoner);

        Person relatertPersjon = new Person();
        // Relasjonens ident
        NorskIdent relasjonIdent = new NorskIdent();
        relasjonIdent.setIdent(tpsRelasjon.relasjonFnr);
        Personidenter relasjonIdenter = new Personidenter();
        relasjonIdenter.setValue("FNR");
        relasjonIdent.setType(relasjonIdenter);

        PersonIdent pi = new PersonIdent();
        pi.setIdent(relasjonIdent);
        relatertPersjon.setAktoer(pi);

        // Relasjonens fødselsdato
        if (tpsRelasjon.relasjonFnr != null) {
            Foedselsdato relasjonFodselsdato = new Foedselsdato();
            relasjonFodselsdato.setFoedselsdato(PersonBygger.tilXmlGregorian(tpsRelasjon.relasjonFnr));
            relatertPersjon.setFoedselsdato(relasjonFodselsdato);
        }

        // Relasjonens navn
        Personnavn relasjonPersonnavn = new Personnavn();
        relasjonPersonnavn.setEtternavn(tpsRelasjon.etternavn.toUpperCase());
        relasjonPersonnavn.setFornavn(tpsRelasjon.fornavn.toUpperCase());
        relasjonPersonnavn.setSammensattNavn(tpsRelasjon.etternavn.toUpperCase() + " "
                + tpsRelasjon.fornavn.toUpperCase());
        relatertPersjon.setPersonnavn(relasjonPersonnavn);

        // Relasjon settes på personen
        familierelasjon.setTilPerson(relatertPersjon);
        bruker.getHarFraRolleI().add(familierelasjon);

        return bruker;
    }

}
