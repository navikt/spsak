package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Foedselsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoenn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kjoennstyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatus;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personstatuser;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Sivilstand;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Sivilstander;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Statsborgerskap;


public class PersonBygger {
    private String fnr;
    private final Kjønn kjønn;
    private String fornavn = "Fornavn";
    private String etternavn = "Etternavn";
    private LocalDate fødselsdato;
    private List<TpsRelasjon> tpsRelasjoner = new ArrayList<>();
    private String personstatus = "BOSA";
    private String statsborgerLand = "NOR";
    private String adresseLand = "NOR";

    public enum Kjønn {
        MANN("M"),
        KVINNE("K");

        String verdi;

        Kjønn(String verdi) {
            this.verdi = verdi;
        }
    }

    public PersonBygger(TpsPerson tpsPerson) {
        this.fnr = tpsPerson.fnr;
        this.fornavn = tpsPerson.fornavn;
        this.etternavn = tpsPerson.etternavn;

        if ("M".equals(tpsPerson.kjønn)) {
            this.kjønn = Kjønn.MANN;

        } else if ("K".equals(tpsPerson.kjønn)) {
            this.kjønn = Kjønn.KVINNE;

        } else {
            this.kjønn = null;
        }
    }

    public PersonBygger(String fnr, String fornavn, String etternavn, Kjønn kjønn, String personstatus, String adresseLand, String statsborgerLand) {
        Objects.requireNonNull(fnr, "Fødselsnummer er obligatorisk");
        Objects.requireNonNull(kjønn, "Kjønn er obligatorisk");
        Objects.requireNonNull(fornavn, "Fornavn er obligatorisk");
        Objects.requireNonNull(etternavn, "Etternavn er obligatorisk");
        Objects.requireNonNull(personstatus, "Personstatus er obligatorisk");
        Objects.requireNonNull(personstatus, "AdresseLand er obligatorisk");
        Objects.requireNonNull(statsborgerLand, "StatsborgerLand er obligatorisk");
        this.fnr = fnr;
        this.kjønn = kjønn;
        this.fornavn = fornavn;
        this.etternavn = etternavn;
        this.personstatus = personstatus;
        this.adresseLand = adresseLand;
        this.statsborgerLand = statsborgerLand;
    }

    public PersonBygger(String fnr, Kjønn kjønn) {
        Objects.requireNonNull(fnr, "Fødselsnummer er obligatorisk");
        Objects.requireNonNull(kjønn, "Kjønn er obligatorisk");
        this.fnr = fnr;
        this.kjønn = kjønn;
    }

    public String getFnr() {
        return fnr;
    }

    public PersonBygger medFødseldato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
        return this;
    }

    public PersonBygger medPersonstatus(PersonstatusType personstatusType) {
        this.personstatus = personstatusType.getKode();
        return this;
    }

    public PersonBygger leggTilRelasjon(String relasjonsType, String relasjonFnr, String fornavn, String etternavn) {
        TpsRelasjon tpsRelasjon = new TpsRelasjon();
        tpsRelasjon.relasjonsType = relasjonsType;
        tpsRelasjon.relasjonFnr = relasjonFnr;
        tpsRelasjon.fornavn = fornavn;
        tpsRelasjon.etternavn = etternavn;
        tpsRelasjoner.add(tpsRelasjon);
        return this;
    }

    public PersonBygger leggTilRelasjon(String relasjonsType, String relasjonFnr) {
        TpsRelasjon tpsRelasjon = new TpsRelasjon();
        tpsRelasjon.relasjonsType = relasjonsType;
        tpsRelasjon.relasjonFnr = relasjonFnr;
        tpsRelasjoner.add(tpsRelasjon);
        return this;
    }

    public Bruker bygg() {
        Bruker bruker = new Bruker();

        // Ident
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        Personidenter personidenter = new Personidenter();
        personidenter.setValue("fnr");
        norskIdent.setType(personidenter);

        PersonIdent pi = new PersonIdent();
        pi.setIdent(norskIdent);
        bruker.setAktoer(pi);

        // Kjønn
        Kjoenn kjonn = new Kjoenn();
        Kjoennstyper kjonnstype = new Kjoennstyper();
        kjonnstype.setValue(kjønn.verdi);
        kjonn.setKjoenn(kjonnstype);
        bruker.setKjoenn(kjonn);

        // Fødselsdato
        Foedselsdato fodselsdato = new Foedselsdato();
        XMLGregorianCalendar xcal;
        if (fødselsdato == null) {
            xcal = tilXmlGregorian(fnr);
        } else {
            xcal = tilXmlGregorian(fødselsdato);
        }
        fodselsdato.setFoedselsdato(xcal);
        bruker.setFoedselsdato(fodselsdato);

        // Navn
        Personnavn personnavn = new Personnavn();
        personnavn.setEtternavn(etternavn.toUpperCase());
        personnavn.setFornavn(fornavn.toUpperCase());
        personnavn.setSammensattNavn(etternavn.toUpperCase() + " " + fornavn.toUpperCase());
        bruker.setPersonnavn(personnavn);

        // Statsborgerskap
        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoderForStatsborger = new Landkoder();
        landkoderForStatsborger.setValue(this.statsborgerLand);
        statsborgerskap.setLand(landkoderForStatsborger);
        bruker.setStatsborgerskap(statsborgerskap);

        // Personstatus
        Personstatus personstatus = new Personstatus();
        Personstatuser personstatuser = new Personstatuser();
        personstatuser.setValue(this.personstatus);
        personstatus.setPersonstatus(personstatuser);
        bruker.setPersonstatus(personstatus);

        // Sivilstand
        Sivilstander sivilstander = new Sivilstander();
        sivilstander.setValue("UGIF");
        Sivilstand sivilstand = new Sivilstand();
        sivilstand.setSivilstand(sivilstander);
        bruker.setSivilstand(sivilstand);

        // Adresse
        Bostedsadresse bostedadresse = new Bostedsadresse();
        Gateadresse gateadresse = new Gateadresse();
        gateadresse.setGatenavn("Dummyadresse");
        gateadresse.setGatenummer(1);
        Landkoder landkoderForAdresse = new Landkoder();
        landkoderForAdresse.setValue(this.adresseLand);
        gateadresse.setLandkode(landkoderForAdresse);
        Postnummer poststed = new Postnummer();
        poststed.setValue("1234");
        gateadresse.setPoststed(poststed);
        bostedadresse.setStrukturertAdresse(gateadresse);
        bruker.setBostedsadresse(bostedadresse);
        Postadressetyper postadresseType = new Postadressetyper();
        postadresseType.setValue("BOSTEDSADRESSE");
        bruker.setGjeldendePostadressetype(postadresseType);

        // Relasjoner
        for (TpsRelasjon tpsRelasjon : tpsRelasjoner) {
            bruker = new RelasjonBygger(tpsRelasjon).byggFor(bruker);
        }
        return bruker;
    }

    private XMLGregorianCalendar tilXmlGregorian(LocalDate fødselsdato) {
        XMLGregorianCalendar xcal;
        try {
            GregorianCalendar gcal = GregorianCalendar.from(fødselsdato.atStartOfDay(ZoneId.systemDefault()));
            xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Kunne ikke konvertere dato", e);
        }
        return xcal;
    }

    static XMLGregorianCalendar tilXmlGregorian(String fødselsnummer) {
        XMLGregorianCalendar xcal;
        try {
            // Simpel algoritme for å konvertere fnr. Må utbedres ved behov.
            DateFormat format = new SimpleDateFormat("ddMMyy");
            Date dato = format.parse(fødselsnummer.substring(0, 6));
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(dato);
            xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException | ParseException e) {
            throw new IllegalStateException("Kunne ikke konvertere dato", e);
        }
        return xcal;
    }


}
