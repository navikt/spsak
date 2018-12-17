package no.nav.foreldrepenger.mottak.domene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.mottak.domene.v1.Søknad;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Overfoeringsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.v1.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class SøknadTest {

    private static String AKTØR_ID = "12341234";
    private static String SAKSNUMMER = "98765433";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    Soeknad søknad;
    Bruker bruker;
    KodeverkRepository kodeverkRepository;
    AktørConsumer aktørConsumer;
    MottakMeldingDataWrapper test;
    Søknad søknadXmlWrapper;

    @Before
    public void init() {
        søknad = new Soeknad();
        bruker = new Bruker();
        bruker.setAktoerId(AKTØR_ID);
        søknad.setSoeker(bruker);
        søknad.setMottattDato(opprettMottattDato(2018, 3, 8));
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        aktørConsumer = mock(AktørConsumer.class);
        test = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData("TEST"));
        test.setAktørId(AKTØR_ID);
        søknadXmlWrapper = (Søknad) MottattStrukturertDokument.toXmlWrapper(søknad);
    }

    private OmYtelse mapOmYtelse(Ytelse ytelse) {
        OmYtelse omYtelse = new OmYtelse();
        omYtelse.getAny().add(ytelse);
        return omYtelse;
    }

    @Test
    public void skal_sjekke_engangs_søknad_fødsel() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }
    
    
    @Test
    public void skal_sjekke_engangs_søknad_termin() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }

    @Test
    public void skal_sjekke_engangs_søknad_adopsjon() {
        final SoekersRelasjonTilBarnet soekersRelasjonTilBarnet = new Adopsjon();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_engangs_søknad_omsorgovertakelse() {
        final SoekersRelasjonTilBarnet soekersRelasjonTilBarnet = new Omsorgsovertakelse();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    public void skal_sjekke_engangs_søknad() {

        final Engangsstønad engangsstønad = new Engangsstønad();
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }


    @Test
    public void skal_sjekke_foreldrepenger_søknad_fødsel() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_termin() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_adopsjon() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Adopsjon();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_omsorgovertakelse() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_kaste_ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        
        assertThatThrownBy(() -> søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-404782");
    }

    @Test
    public void skal_sjekke_foreldrepenger_endringssøknad() {
        Fordeling fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        final Endringssoeknad endringssoeknad = new Endringssoeknad();
        endringssoeknad.setSaksnummer(SAKSNUMMER);
        endringssoeknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssoeknad));

        test.setSaksnummer(SAKSNUMMER);
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getSaksnummer()).hasValue(SAKSNUMMER);
    }

    @Test
    public void skal_teste_validering_brukerId() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setAktørId("95873742"); // simuler annen aktørId fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-502574");

        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
    }

    @Test
    public void skal_teste_validering_saksnummer() {
        Fordeling fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        final Endringssoeknad endringssøknad = new Endringssoeknad();
        endringssøknad.setSaksnummer(SAKSNUMMER);
        endringssøknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssøknad));

        test.setSaksnummer("857356"); // saksnummer fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-401245");

        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
    }

    @Test
    public void skal_sjekke_udefinert_søknad() {
        søknad.setOmYtelse(mapOmYtelse(null));

        test.setBehandlingTema(BehandlingTema.UDEFINERT);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }

    private XMLGregorianCalendar opprettMottattDato(int y, int m, int d) {
        LocalDate dato = LocalDate.of(y, m, d);
        GregorianCalendar gcal = GregorianCalendar.from(dato.atStartOfDay(ZoneId.systemDefault()));
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
