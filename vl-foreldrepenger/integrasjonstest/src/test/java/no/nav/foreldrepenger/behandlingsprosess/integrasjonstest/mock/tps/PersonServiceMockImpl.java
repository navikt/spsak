package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentEkteskapshistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentEkteskapshistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonerMedSammeAdresseIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonerMedSammeAdresseSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentSikkerhetstiltakPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentVergePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentVergeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.BostedsadressePeriode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Periode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonstatusPeriode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StatsborgerskapPeriode;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentEkteskapshistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentEkteskapshistorikkResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonerMedSammeAdresseRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonerMedSammeAdresseResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentSikkerhetstiltakResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentVergeRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentVergeResponse;
import no.nav.tjeneste.virksomhet.person.v3.metadata.Endringstyper;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.konfig.Tid;

public class PersonServiceMockImpl implements PersonV3 {

    private static final Logger LOG = LoggerFactory.getLogger(PersonServiceMockImpl.class);
    private static final TpsRepo TPS_REPO = TpsRepo.init();
    private static final String ENDRET_AV = "MOCK";

    private RegisterKontekst registerKontekst;

    public PersonServiceMockImpl(RegisterKontekst registerKontekst) {
        this.registerKontekst = registerKontekst;
    }

    @Override
    public HentPersonResponse hentPerson(@WebParam(name = "request", targetNamespace = "") HentPersonRequest request) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        Aktoer aktoer = request.getAktoer();
        if (!(aktoer instanceof PersonIdent)) {
            throw new IllegalArgumentException("Skal bare kalles med " + PersonIdent.class.getName());
        }
        String fnr = ((PersonIdent) aktoer).getIdent().getIdent();
        Person person;
        person = finnPerson(fnr);
        if (person == null) {
            throw new HentPersonPersonIkkeFunnet("Fant ikke aktuell bruker", new PersonIkkeFunnet());
        }
        HentPersonResponse response = new HentPersonResponse();
        response.setPerson(person);
        return response;
    }

    @Override
    public void ping() {
        LOG.info("Ping mottatt og besvart");
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest request) throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();

        GeografiskTilknytning kommune = new Kommune();
        kommune.setGeografiskTilknytning("NITTEDAL");
        response.setGeografiskTilknytning(kommune);

        Diskresjonskoder diskresjonskode = new Diskresjonskoder();
        diskresjonskode.setValue("1");
        response.setDiskresjonskode(diskresjonskode);

        return response;
    }

    @Override
    public HentPersonnavnBolkResponse hentPersonnavnBolk(@WebParam(name = "request", targetNamespace = "") HentPersonnavnBolkRequest var1) {
        throw new UnsupportedOperationException("Ikke implementert");
    }

    @Override
    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(@WebParam(name = "request", targetNamespace = "") HentSikkerhetstiltakRequest var1) throws HentSikkerhetstiltakPersonIkkeFunnet {
        throw new UnsupportedOperationException("Ikke implementert");
    }

    @Override
    public HentVergeResponse hentVerge(HentVergeRequest request) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        throw new UnsupportedOperationException("Ikke implementert");
    }

    @Override
    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest request)
            throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        throw new UnsupportedOperationException("Ikke implementert");
    }

    @Override
    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest request)
            throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        throw new UnsupportedOperationException("Ikke implementert");
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikk(HentPersonhistorikkRequest request)
            throws HentPersonhistorikkPersonIkkeFunnet, HentPersonhistorikkSikkerhetsbegrensning {
        final HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        Aktoer aktoer = request.getAktoer();
        String fnr;
        if (!(aktoer instanceof PersonIdent)) {
            fnr = finnIdent(new AktørId(((AktoerId) aktoer).getAktoerId()));
        } else {

            fnr = ((PersonIdent) aktoer).getIdent().getIdent();
        }
        Person person = finnPerson(fnr);
        if (person == null) {
            throw new HentPersonhistorikkPersonIkkeFunnet("Fant ikke aktuell bruker", new PersonIkkeFunnet());
        }

        response.setAktoer(request.getAktoer());
        try {
            response.withPersonstatusListe(hentPersonstatusPerioder(person));
            response.withBostedsadressePeriodeListe(hentBostedadressePerioder(person));
            response.withStatsborgerskapListe(hentStatsborgerskapPerioder(person));
        } catch (DatatypeConfigurationException e) {
            throw new HentPersonhistorikkPersonIkkeFunnet("STRING", null, e);
        }
        return response;
    }

    private Person finnPerson(String fnr) {
        if (registerKontekst.erInitalisert()) {
            return TpsTestSett.finnPerson(fnr);
        } else {
            return TPS_REPO.finnPerson(fnr);
        }
    }

    private String finnIdent(AktørId aktoer) {
        if (registerKontekst.erInitalisert()) {
            return TpsTestSett.finnIdent(aktoer);
        } else {
            return TPS_REPO.finnIdent(aktoer);
        }
    }



    private List<StatsborgerskapPeriode> hentStatsborgerskapPerioder(Person aktoer) throws DatatypeConfigurationException {
        List<StatsborgerskapPeriode> resultat = new ArrayList<>();

        StatsborgerskapPeriode periode1 = new StatsborgerskapPeriode();
        periode1.withEndretAv(ENDRET_AV);
        periode1.withEndringstidspunkt(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));
        periode1.withEndringstype(Endringstyper.NY);
        periode1.withPeriode(lagPeriode(fødselsdatoTilLocalDate(aktoer), dødsdatoTilLocalDate(aktoer)));
        periode1.withEndringstype(Endringstyper.NY);
        periode1.withStatsborgerskap(aktoer.getStatsborgerskap());

        resultat.add(periode1);
        return resultat;
    }

    private List<BostedsadressePeriode> hentBostedadressePerioder(Person aktoer) throws DatatypeConfigurationException {
        List<BostedsadressePeriode> resultat = new ArrayList<>();

        BostedsadressePeriode adr1 = new BostedsadressePeriode();
        adr1.withEndretAv(ENDRET_AV);
        adr1.withEndringstidspunkt(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));
        adr1.withEndringstype(Endringstyper.NY);
        adr1.withBostedsadresse(aktoer.getBostedsadresse());
        adr1.withPeriode(lagPeriode(fødselsdatoTilLocalDate(aktoer), dødsdatoTilLocalDate(aktoer)));

        resultat.add(adr1);
        return resultat;
    }

    private List<PersonstatusPeriode> hentPersonstatusPerioder(Person aktoer) throws DatatypeConfigurationException {
        List<PersonstatusPeriode> resultat = new ArrayList<>();

        PersonstatusPeriode personstatusPeriode = new PersonstatusPeriode();
        personstatusPeriode.withEndretAv(ENDRET_AV);
        personstatusPeriode.withEndringstidspunkt(DateUtil.convertToXMLGregorianCalendar(LocalDate.now()));
        personstatusPeriode.withEndringstype(Endringstyper.NY);
        personstatusPeriode.withPersonstatus(aktoer.getPersonstatus().getPersonstatus());
        personstatusPeriode.withPeriode(lagPeriode(fødselsdatoTilLocalDate(aktoer), dødsdatoTilLocalDate(aktoer)));

        resultat.add(personstatusPeriode);

        return resultat;
    }

    private LocalDate fødselsdatoTilLocalDate(Person aktoer) {
        return DateUtil.convertToLocalDate(aktoer.getFoedselsdato().getFoedselsdato());
    }

    private LocalDate dødsdatoTilLocalDate(Person aktoer) {
        if(aktoer.getDoedsdato() != null && aktoer.getDoedsdato().getDoedsdato() != null) {
            final XMLGregorianCalendar dato = aktoer.getDoedsdato().getDoedsdato();
            return DateUtil.convertToLocalDate(dato);
        }
        return Tid.TIDENES_ENDE;
    }

    private Periode lagPeriode(LocalDate fom, LocalDate tom) throws DatatypeConfigurationException {
        Periode periode = new Periode();
        periode.withFom(DateUtil.convertToXMLGregorianCalendar(fom));
        periode.withTom(DateUtil.convertToXMLGregorianCalendar(tom));
        return periode;
    }

}
