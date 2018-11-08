package no.nav.foreldrepenger.domene.person.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.PoststedKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.FiktiveFnr;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadressetyper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postnummer;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StedsadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class PersoninfoAdapterMedTPSOversetterTest {

    private static final FiktiveFnr FIKTIVE_FNR = new FiktiveFnr();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private PersoninfoAdapterImpl adapterMedVanligOversetter; // objektet vi tester

    private static final AktørId AKTØR_ID__ADRESSE = new AktørId("666");
    private static final PersonIdent FNR_ADRESSE = PersonIdent.fra(FIKTIVE_FNR.nesteFnr());

    private static final String MIDLERTIDIG_POSTADRESSE_UTLAND = "MIDLERTIDIG_POSTADRESSE_UTLAND";

    private static final String MIDLERTIDIG_POSTADRESSE_NORGE = "MIDLERTIDIG_POSTADRESSE_NORGE";

    private static final String POSTADRESSE_UTLAND = "POSTADRESSE_UTLAND";

    private static final String POSTADRESSE = "POSTADRESSE";

    private static final String POSTBOKSADRESSE_NORGE = "BOSTEDSADRESSE";

    private static final String BOSTEDSADRESSE = "BOSTEDSADRESSE";

    @Mock
    private Personinfo mockPersoninfo;
    @Mock
    private TpsOversetter tpsOversetter;
    @Mock
    private TpsAdresseOversetter tpsAdresseOversetter;
    @Mock
    private AktørConsumerMedCache aktørConsumer;
    @Mock
    private PersonConsumer personConsumer;
    @Mock
    private TpsFamilieTjeneste tpsFamilieTjeneste;

    private PoststedKodeverkRepository poststedKodeverkRepository = new PoststedKodeverkRepositoryImpl(repoRule.getEntityManager());

    private TpsAdapter tpsAdapter;

    private Landkoder landkodeNor = new Landkoder();

    private final PersonIdent personIdent = PersonIdent.fra(FIKTIVE_FNR.nesteFnr());

    @Before
    public void setup() throws HentAktoerIdForIdentPersonIkkeFunnet, HentIdentForAktoerIdPersonIkkeFunnet {
        landkodeNor.setValue("NOR");

        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID__ADRESSE).map(AktørId::getId));
        when(aktørConsumer.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(FNR_ADRESSE.getIdent()));
        when(mockPersoninfo.getFødselsdato()).thenReturn(LocalDate.now()); // trenger bare en verdi
        when(tpsOversetter.tilBrukerInfo(Mockito.any(AktørId.class), any(Bruker.class))).thenReturn(mockPersoninfo);
        tpsAdresseOversetter = new TpsAdresseOversetter(null, poststedKodeverkRepository);
        tpsOversetter = new TpsOversetter(null, null, null, tpsAdresseOversetter);
        tpsAdapter = new TpsAdapterImpl(aktørConsumer, personConsumer, tpsOversetter);
        adapterMedVanligOversetter = new PersoninfoAdapterImpl(tpsAdapter, tpsFamilieTjeneste);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_midlertidig_utland() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForMidlertidigPostAdresseUtland());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoPostAdresseUtland(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_midlertidig_norge() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForMidlertidigPostAdresseNorge());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoMidlertidigPostAdresseNorge(), adresseinfo);
    }


    @Test
    public void innhente_adresseopplysninger_for_søker_med_kode_6() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForKode6());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoKode6(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_adresse_utland() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForPostAdresseUtland());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoPostAdresseUtland(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_adresse_norge() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForPostAdresseNorge());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoPostAdresseNorge(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_matrikkeladresse_norge() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForMatrikkelAdresseNorge());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoMatrikkelAdresseNorge(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_postboks_adresse_norge() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForPostboksAdresseNorge());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoPostboksAdresseNorge(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_steds_adresse_norge() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForStedsAdresseNorge());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoStedsAdresseNorge(), adresseinfo);
    }

    @Test
    public void innhente_adresseopplysninger_for_søker_bosteds_adresse_norge_med_tillegg() throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        when(personConsumer.hentPersonResponse(any())).thenReturn(lagHentPersonResponseForBostedsadresseMedTillegg());

        Adresseinfo adresseinfo = adapterMedVanligOversetter.innhentAdresseopplysningerForDokumentsending(AKTØR_ID__ADRESSE);
        assertNotNull(adresseinfo);
        assertAdresse(lagReferanseAdresseinfoStedsAdresseNorgeMedTillegg(), adresseinfo);
    }

    private Adresseinfo lagReferanseAdresseinfoMatrikkelAdresseNorge() {
        return builderWithDefault(AdresseType.BOSTEDSADRESSE)
            .medAdresselinje1("Bolignummer 1423 Spring")
            .medPostNr("0504")
            .medPoststed("OSLO")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoKode6() {
        return builderWithDefault(AdresseType.POSTADRESSE)
            .medLand("NOR")
            .medAdresselinje1("SOT6")
            .medAdresselinje2("POSTBOKS 8163 DEP")
            .medPostNr("0034")
            .medPoststed("OSLO")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoPostAdresseUtland() {
        return builderWithDefault(AdresseType.POSTADRESSE_UTLAND)
            .medAdresselinje1("linje1")
            .medAdresselinje2("linje2")
            .medAdresselinje3("linje3")
            .medLand("CHN")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoPostAdresseNorge() {
        return builderWithDefault(AdresseType.POSTADRESSE)
            .medAdresselinje1("Postboks 103")
            .medPostNr("1307")
            .medPoststed("FORNEBU")
            .medLand("NOR")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoMidlertidigPostAdresseNorge() {
        return builderWithDefault(AdresseType.POSTADRESSE)
            .medAdresselinje1("Boligveien 666 A")
            .medPostNr("0504")
            .medPoststed("OSLO")
            .medLand("NOR")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoPostboksAdresseNorge() {
        return builderWithDefault(AdresseType.POSTADRESSE)
            .medAdresselinje1("Postboks 145 PBOKSANlegg")
            .medPostNr("0504")
            .medPoststed("OSLO")
            .build();
    }

    private Adresseinfo.Builder builderWithDefault(AdresseType postadresse) {
        return new Adresseinfo.Builder(postadresse, personIdent, "Bingo Bango", PersonstatusType.BOSA);
    }

    private Adresseinfo lagReferanseAdresseinfoStedsAdresseNorge() {
        return builderWithDefault(AdresseType.BOSTEDSADRESSE)
            .medAdresselinje1("1423")
            .medPostNr("0504")
            .medPoststed("OSLO")
            .build();
    }

    private Adresseinfo lagReferanseAdresseinfoStedsAdresseNorgeMedTillegg() {
        return builderWithDefault(AdresseType.BOSTEDSADRESSE)
            .medAdresselinje1("C/O Jensen")
            .medAdresselinje2("Boligveien 666 A")
            .medPostNr("0504")
            .medPoststed("OSLO")
            .medLand("NOR")
            .build();
    }

    private void assertAdresse(Adresseinfo adresseinfo, Adresseinfo adresseinfo2) {
        assertEquals(adresseinfo.getMottakerNavn(), adresseinfo2.getMottakerNavn());
        assertEquals(adresseinfo.getAdresselinje1(), adresseinfo2.getAdresselinje1());
        assertEquals(adresseinfo.getAdresselinje2(), adresseinfo2.getAdresselinje2());
        assertEquals(adresseinfo.getAdresselinje3(), adresseinfo2.getAdresselinje3());
        assertEquals(adresseinfo.getAdresselinje4(), adresseinfo2.getAdresselinje4());
        assertEquals(adresseinfo.getPostNr(), adresseinfo2.getPostNr());
        assertEquals(adresseinfo.getPoststed(), adresseinfo2.getPoststed());
        assertEquals(adresseinfo.getLand(), adresseinfo2.getLand());
    }

    private HentPersonResponse lagHentPersonResponseForMatrikkelAdresseNorge() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(BOSTEDSADRESSE);
        Bostedsadresse bostedsadresse = new Bostedsadresse();
        bostedsadresse.setStrukturertAdresse(lagMatrikkeladresse());
        hentKjerneinformasjonResponse.getPerson().setBostedsadresse(bostedsadresse);
        return hentKjerneinformasjonResponse;
    }


    private HentPersonResponse lagHentPersonResponseForMidlertidigPostAdresseUtland() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(MIDLERTIDIG_POSTADRESSE_UTLAND);
        MidlertidigPostadresseUtland midlertidigPostadresseUtland = new MidlertidigPostadresseUtland();
        midlertidigPostadresseUtland.setUstrukturertAdresse(lagUstrukturertAdresseUtland());
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        gyldighetsperiode.setFom(lagXMLGregorianCalendar());
        midlertidigPostadresseUtland.setPostleveringsPeriode(gyldighetsperiode);
        ((Bruker) hentKjerneinformasjonResponse.getPerson()).setMidlertidigPostadresse(midlertidigPostadresseUtland);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForMidlertidigPostAdresseNorge() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(MIDLERTIDIG_POSTADRESSE_NORGE);
        MidlertidigPostadresseNorge midlertidigPostadresseNorge = new MidlertidigPostadresseNorge();
        midlertidigPostadresseNorge.setStrukturertAdresse(lagGateadresse());
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        gyldighetsperiode.setFom(lagXMLGregorianCalendar());
        midlertidigPostadresseNorge.setPostleveringsPeriode(gyldighetsperiode);
        ((Bruker) hentKjerneinformasjonResponse.getPerson()).setMidlertidigPostadresse(midlertidigPostadresseNorge);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForBostedsadresseMedTillegg() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(BOSTEDSADRESSE);
        Bostedsadresse bostedsadresse = new Bostedsadresse();
        bostedsadresse.setStrukturertAdresse(lagGateadresseMedTilleggsadresse());
        hentKjerneinformasjonResponse.getPerson().setBostedsadresse(bostedsadresse);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForPostAdresseUtland() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(POSTADRESSE_UTLAND);
        Postadresse postadresse = new Postadresse();
        postadresse.setUstrukturertAdresse(lagUstrukturertAdresseUtland());
        hentKjerneinformasjonResponse.getPerson().setPostadresse(postadresse);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForPostAdresseNorge() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(POSTADRESSE);
        Postadresse postadresse = new Postadresse();
        postadresse.setUstrukturertAdresse(lagUstrukturertAdresse());
        hentKjerneinformasjonResponse.getPerson().setPostadresse(postadresse);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForPostboksAdresseNorge() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(POSTBOKSADRESSE_NORGE);
        Bostedsadresse bostedsadresse = new Bostedsadresse();
        bostedsadresse.setStrukturertAdresse(lagPostboksAdresseNorsk());
        hentKjerneinformasjonResponse.getPerson().setBostedsadresse(bostedsadresse);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForStedsAdresseNorge() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(BOSTEDSADRESSE);
        Bostedsadresse bostedsadresse = new Bostedsadresse();
        bostedsadresse.setStrukturertAdresse(lagStedAdresseNorge());
        hentKjerneinformasjonResponse.getPerson().setBostedsadresse(bostedsadresse);
        return hentKjerneinformasjonResponse;
    }

    private HentPersonResponse lagHentPersonResponseForKode6() {
        HentPersonResponse hentKjerneinformasjonResponse = lagHentPersonResponseAdresse(POSTADRESSE);
        Postadresse postadresse = new Postadresse();
        postadresse.setUstrukturertAdresse(lagUstrukturertAdresseForKode6());
        hentKjerneinformasjonResponse.getPerson().setPostadresse(postadresse);
        return hentKjerneinformasjonResponse;
    }

    private UstrukturertAdresse lagUstrukturertAdresseForKode6() {
        UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
        ustrukturertAdresse.setLandkode(landkodeNor);
        ustrukturertAdresse.setAdresselinje1("SOT6");
        ustrukturertAdresse.setAdresselinje2("POSTBOKS 8163 DEP");
        ustrukturertAdresse.setAdresselinje4("0034 OSLO");
        return ustrukturertAdresse;
    }

    private HentPersonResponse lagHentPersonResponseAdresse(String postadressetype) {
        Bruker bruker = new Bruker();
        Personnavn personnavn = new Personnavn();
        personnavn.setSammensattNavn("Bingo Bango");
        bruker.setPersonnavn(personnavn);
        bruker.setAktoer(TpsUtil.lagPersonIdent("456"));
        HentPersonResponse kjerneinfoSøker = new HentPersonResponse();
        kjerneinfoSøker.setPerson(bruker);
        Postadressetyper type = new Postadressetyper();
        type.setValue(postadressetype);
        bruker.setGjeldendePostadressetype(type);
        return kjerneinfoSøker;
    }

    private UstrukturertAdresse lagUstrukturertAdresse() {
        UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
        ustrukturertAdresse.setAdresselinje1("Postboks 103");
        ustrukturertAdresse.setAdresselinje4("1307 FORNEBU");
        ustrukturertAdresse.setLandkode(landkodeNor);
        return ustrukturertAdresse;
    }

    private UstrukturertAdresse lagUstrukturertAdresseUtland() {
        UstrukturertAdresse ustrukturertAdresse = new UstrukturertAdresse();
        ustrukturertAdresse.setAdresselinje1("linje1");
        ustrukturertAdresse.setAdresselinje2("linje2");
        ustrukturertAdresse.setAdresselinje3("linje3");
        Landkoder chn = new Landkoder();
        chn.setValue("CHN");
        ustrukturertAdresse.setLandkode(chn);
        return ustrukturertAdresse;
    }


    private XMLGregorianCalendar lagXMLGregorianCalendar() {
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.add(Calendar.DATE, -100);
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (Exception e) {
            return null;
        }
    }

    private Gateadresse lagGateadresse() {
        Gateadresse gateadresse = new Gateadresse();
        gateadresse.setPoststed(lagPostnummer());
        gateadresse.setBolignummer("1423");
        gateadresse.setGatenavn("Boligveien");
        gateadresse.setHusnummer(666);
        gateadresse.setHusbokstav("A");
        gateadresse.setLandkode(landkodeNor);
        return gateadresse;
    }

    private Gateadresse lagGateadresseMedTilleggsadresse() {
        Gateadresse gateadresse = lagGateadresse();
        gateadresse.setTilleggsadresse("C/O Jensen");
        return gateadresse;
    }

    private Matrikkeladresse lagMatrikkeladresse() {
        Matrikkeladresse matrikkeladresse = new Matrikkeladresse();
        matrikkeladresse.setPoststed(lagPostnummer());
        matrikkeladresse.setEiendomsnavn("Spring");
        matrikkeladresse.setBolignummer("1423");
        return matrikkeladresse;
    }

    private StedsadresseNorge lagStedAdresseNorge() {
        StedsadresseNorge stedsadresseNorge = new StedsadresseNorge();
        stedsadresseNorge.setPoststed(lagPostnummer());
        stedsadresseNorge.setBolignummer("1423");
        return stedsadresseNorge;
    }

    private PostboksadresseNorsk lagPostboksAdresseNorsk() {
        PostboksadresseNorsk postboksadresseNorsk = new PostboksadresseNorsk();
        postboksadresseNorsk.setPoststed(lagPostnummer());
        postboksadresseNorsk.setPostboksanlegg("PBOKSANlegg");
        postboksadresseNorsk.setPostboksnummer("145");
        return postboksadresseNorsk;
    }

    private Postnummer lagPostnummer() {
        Postnummer postnummer = new Postnummer();
        postnummer.setValue("0504");
        return postnummer;
    }
}
