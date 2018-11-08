package no.nav.foreldrepenger.kodeverk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodelisteRelasjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeverk;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkSynkroniseringRepository;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.EnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Kode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Node;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Periode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.SammensattKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Term;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.FinnKodeverkListeResponse;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.kodeverk.KodeverkConsumer;

public class KodeverkSynkroniseringTest {

    private LocalDate MAX_DATE = LocalDate.of(9999, 12,31);

    private KodeverkSynkroniseringRepository kodeverkSynkroniseringRepository = mock(KodeverkSynkroniseringRepository.class);
    private KodeverkSynkronisering kodeverkSynkronisering;
    private KodeverkConsumer kodeverkConsumer = mock(KodeverkConsumer.class);

    private ArgumentMatcher<HentKodeverkRequest> kodeverkRequestArgumentMatcher = new ArgumentMatcher<HentKodeverkRequest>() {
        @Override
        public boolean matches(HentKodeverkRequest o) {
            return o.getNavn().compareTo("Postnummer") == 0;
        }
    };

    private ArgumentMatcher<HentKodeverkRequest> sammensattKodeverkRequestArgumentMatcher = new ArgumentMatcher<HentKodeverkRequest>() {
        @Override
        public boolean matches(HentKodeverkRequest o) {
            return o.getNavn().compareTo("Geografi") == 0;
        }
    };

    @Before
    public void setup() throws Exception {
        kodeverkSynkronisering = new KodeverkSynkronisering(kodeverkSynkroniseringRepository, new KodeverkTjenesteImpl(kodeverkConsumer));
    }

    @Test
    public void skal_synke_kodeverk_ved_ny_versjon() throws Exception {
        // Arrange
        HentKodeverkResponse kodeverkResponse = opprettEnkeltKodeverkResponse();
        when(kodeverkConsumer.hentKodeverk(argThat(kodeverkRequestArgumentMatcher))).thenReturn(kodeverkResponse);
        Kodeverk postnummerKodeverk = new Kodeverk("POSTNUMMER", "Kodeverkforvaltning", "6",
            "Postnummer", true, true, false);
        List<Kodeverk> kodeverkList = new ArrayList<>();
        kodeverkList.add(postnummerKodeverk);
        when(kodeverkSynkroniseringRepository.hentKodeverkForSynkronisering()).thenReturn(kodeverkList);
        Map<String, String> eierNavnMap = new HashMap<>();
        eierNavnMap.put("Postnummer", "POSTNUMMER");
        when(kodeverkSynkroniseringRepository.hentKodeverkEierNavnMap()).thenReturn(eierNavnMap);
        List<Kodeliste> kodeliste = new ArrayList<>();
        Kodeliste postnummer1 = new Kodeliste("7818", "POSTNUMMER", "7818",
            LocalDate.of(2000, 1, 1), MAX_DATE) {
        };
        kodeliste.add(postnummer1);
        when(kodeverkSynkroniseringRepository.hentKodeliste(anyString())).thenReturn(kodeliste);
        when(kodeverkConsumer.finnKodeverkListe(any())).thenReturn(opprettEnkeltKodeverkListeResponse("Postnummer", "7"));

        // Act
        kodeverkSynkronisering.synkroniserAlleKodeverk();

        // Assert
        verify(kodeverkSynkroniseringRepository, times(1)).opprettNyKode(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
        verify(kodeverkSynkroniseringRepository, times(1)).oppdaterEksisterendeKode(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    public void skal_ikke_synke_kodeverk_ved_samme_versjon() throws Exception {
        // Arrange
        HentKodeverkResponse kodeverkResponse = opprettEnkeltKodeverkResponse();
        when(kodeverkConsumer.hentKodeverk(argThat(kodeverkRequestArgumentMatcher))).thenReturn(kodeverkResponse);
        Kodeverk postnummerKodeverk = new Kodeverk("POSTNUMMER", "Kodeverkforvaltning", "6",
            "Postnummer", true, true, false);
        List<Kodeverk> kodeverkList = new ArrayList<>();
        kodeverkList.add(postnummerKodeverk);
        when(kodeverkSynkroniseringRepository.hentKodeverkForSynkronisering()).thenReturn(kodeverkList);
        Map<String, String> eierNavnMap = new HashMap<>();
        eierNavnMap.put("Postnummer", "POSTNUMMER");
        when(kodeverkSynkroniseringRepository.hentKodeverkEierNavnMap()).thenReturn(eierNavnMap);
        List<Kodeliste> kodeliste = new ArrayList<>();
        Kodeliste postnummer1 = new Kodeliste("7818", "POSTNUMMER", "7818",
            LocalDate.of(2000, 1, 1), MAX_DATE) {
        };
        kodeliste.add(postnummer1);
        when(kodeverkSynkroniseringRepository.hentKodeliste(anyString())).thenReturn(kodeliste);
        when(kodeverkConsumer.finnKodeverkListe(any())).thenReturn(opprettEnkeltKodeverkListeResponse("Postnummer", "6"));

        // Act
        kodeverkSynkronisering.synkroniserAlleKodeverk();

        // Assert
        verify(kodeverkSynkroniseringRepository, times(0)).opprettNyKode(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
        verify(kodeverkSynkroniseringRepository, times(0)).oppdaterEksisterendeKode(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }


    @Test
    public void skal_synke_sammensatt_kodeverk() throws Exception {
        // Arrange
        HentKodeverkResponse kodeverkResponse = opprettSammensattKodeverkResponse();
        when(kodeverkConsumer.hentKodeverk(argThat(sammensattKodeverkRequestArgumentMatcher))).thenReturn(kodeverkResponse);
        Kodeverk geografiKodeverk = new Kodeverk("GEOGRAFI", "Kodeverkforvaltning", "4",
            "Geografi", true, true, true);
        List<Kodeverk> kodeverkList = new ArrayList<>();
        kodeverkList.add(geografiKodeverk);
        when(kodeverkSynkroniseringRepository.hentKodeverkForSynkronisering()).thenReturn(kodeverkList);
        Map<String, String> eierNavnMap = new HashMap<>();
        eierNavnMap.put("Geografi", "GEOGRAFI");
        eierNavnMap.put("Fylker", "FYLKER");
        eierNavnMap.put("Kommuner", "KOMMUNER");
        when(kodeverkSynkroniseringRepository.hentKodeverkEierNavnMap()).thenReturn(eierNavnMap);
        List<KodelisteRelasjon> kodelisteRelasjoner = new ArrayList<>();
        KodelisteRelasjon relasjon1 = new KodelisteRelasjon("FYLKER", "01", "KOMMUNER", "0101",
            LocalDate.of(2000, 1, 1), MAX_DATE) {
        }; // Denne har ulik gyldig fom og skal føre til en oppdatering
        kodelisteRelasjoner.add(relasjon1);
        KodelisteRelasjon relasjon2 = new KodelisteRelasjon("FYLKER", "01", "KOMMUNER", "0211",
            LocalDate.of(2000, 1, 1), MAX_DATE) {
        }; // Denne relasjon eksisterer ikke i mottatte data og skal settes ugyldig
        kodelisteRelasjoner.add(relasjon2);
        when(kodeverkSynkroniseringRepository.hentKodelisteRelasjoner(anyString(), anyString())).thenReturn(kodelisteRelasjoner);
        when(kodeverkSynkroniseringRepository.eksistererKode(anyString(), anyString())).thenReturn(true);
        when(kodeverkSynkroniseringRepository.eksistererKode(eq("KOMMUNER"), eq("0710"))).thenReturn(false);
        when(kodeverkConsumer.finnKodeverkListe(any())).thenReturn(opprettEnkeltKodeverkListeResponse("Geografi", "5"));

        // Act
        kodeverkSynkronisering.synkroniserAlleKodeverk();

        // Assert
        verify(kodeverkSynkroniseringRepository, times(1)).opprettNyKodeRelasjon(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
        verify(kodeverkSynkroniseringRepository, times(2)).oppdaterEksisterendeKodeRelasjon(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    private HentKodeverkResponse opprettSammensattKodeverkResponse() throws DatatypeConfigurationException {
        SammensattKodeverk sammensattKodeverk = new SammensattKodeverk();
        sammensattKodeverk.setNavn("Geografi");
        EnkeltKodeverk kodeverkFylker = new EnkeltKodeverk();
        kodeverkFylker.setNavn("Fylker");
        sammensattKodeverk.getBrukerKodeverk().add(kodeverkFylker);
        EnkeltKodeverk kodeverkKommuner = new EnkeltKodeverk();
        kodeverkKommuner.setNavn("Kommuner");
        sammensattKodeverk.getBrukerKodeverk().add(kodeverkKommuner);

        Node nodeFylke = lagNode("Fylker","01","Østfold","http://nav.no/kodeverk/Node/Geografi/Fylker/01?v=4",
            LocalDate.of(1900, 1, 1), MAX_DATE);
        Node nodeKommune1 = lagNode("Kommuner", "0101", "Halden","http://nav.no/kodeverk/Node/Geografi/Kommuner/0101?v=4",
            LocalDate.of(1900, 1, 1), MAX_DATE);
        nodeFylke.getUndernode().add(nodeKommune1);
        Node nodeKommune2 = lagNode("Kommuner", "0104", "Moss","http://nav.no/kodeverk/Node/Geografi/Kommuner/0104?v=4",
            LocalDate.of(1900, 1, 1), MAX_DATE);
        nodeFylke.getUndernode().add(nodeKommune2);
        Node nodeKommune3 = lagNode("Kommuner", "0710", "Sandefjord","http://nav.no/kodeverk/Node/Geografi/Kommuner/0104?v=4",
            LocalDate.of(1900, 1, 1), MAX_DATE);
        nodeFylke.getUndernode().add(nodeKommune3);

        sammensattKodeverk.getInneholderNode().add(nodeFylke);
        HentKodeverkResponse response = new HentKodeverkResponse();
        response.setKodeverk(sammensattKodeverk);
        return response;
    }

    private Node lagNode(String nodeNavn, String kodeNavn, String termNavn, String kodeUri, LocalDate fom, LocalDate tom) throws DatatypeConfigurationException{
        Node noden = new Node();
        noden.setNavn(nodeNavn);
        noden.setInneholderKode(lagKode(kodeNavn, termNavn, kodeUri, fom, tom));
        return noden;
    }

    private FinnKodeverkListeResponse opprettEnkeltKodeverkListeResponse(String navn, String versjon) throws DatatypeConfigurationException {
        no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.finnkodeverkliste.Kodeverk element = new no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.finnkodeverkliste.Kodeverk();
        element.setEier("Koderverksforvaltning");
        element.setNavn(navn);
        element.setVersjonsnummer(versjon);
        element.setUri("http://nav.no/kodeverk/Kodeverk/" + navn + "?v=" + versjon);
        element.setVersjoneringsdato(DateUtil.convertToXMLGregorianCalendar(LocalDate.now().minusDays(1)));
        FinnKodeverkListeResponse response = new FinnKodeverkListeResponse();
        response.getKodeverkListe().add(element);
        return response;
    }

    private HentKodeverkResponse opprettEnkeltKodeverkResponse() throws Exception {
        EnkeltKodeverk enkeltKodeverk = new EnkeltKodeverk();
        enkeltKodeverk.setNavn("Postnummer");
        enkeltKodeverk.setVersjonsnummer("6");
        enkeltKodeverk.getKode().add(lagKode("7818", "LUND", null,
            LocalDate.of(1900, 1, 1), MAX_DATE));
        enkeltKodeverk.getKode().add(lagKode("8888", "DALSTROKA INNAFOR", null,
            LocalDate.of(2017, 1, 1), MAX_DATE));
        HentKodeverkResponse response = new HentKodeverkResponse();
        response.setKodeverk(enkeltKodeverk);
        return response;
    }

    private Kode lagKode(String kodeNavn, String termNavn, String uri, LocalDate fom, LocalDate tom) throws DatatypeConfigurationException {
        Periode periode = new Periode();
        periode.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
        periode.setTom(DateUtil.convertToXMLGregorianCalendar(tom));
        Kode kode = new Kode();
        kode.setNavn(kodeNavn);
        kode.getGyldighetsperiode().add(periode);
        kode.setUri(uri);
        Term term = new Term();
        term.setNavn(termNavn);
        term.setSpraak("nb");
        term.getGyldighetsperiode().add(periode);
        kode.getTerm().add(term);
        return kode;
    }
}
