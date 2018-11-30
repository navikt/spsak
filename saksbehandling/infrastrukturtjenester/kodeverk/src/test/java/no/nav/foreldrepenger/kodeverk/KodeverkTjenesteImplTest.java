package no.nav.foreldrepenger.kodeverk;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.kodeverk.api.KodeverkKode;
import no.nav.foreldrepenger.kodeverk.api.KodeverkTjeneste;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.feil.KodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.EnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Kode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Periode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Term;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.kodeverk.KodeverkConsumer;

public class KodeverkTjenesteImplTest {

    private KodeverkConsumer kodeverkConsumer = mock(KodeverkConsumer.class);
    private KodeverkTjeneste kodeverkTjeneste;

    private LocalDate MAX_DATE = LocalDate.of(9999, 12,31);

    @Before
    public void before() {
        kodeverkTjeneste = new KodeverkTjenesteImpl(kodeverkConsumer);
    }

    @Test
    public void skal_hente_kodeverk() throws Exception {
        // Arrange
        HentKodeverkResponse response = opprettResponse();
        when(kodeverkConsumer.hentKodeverk(any(HentKodeverkRequest.class))).thenReturn(response);

        // Act
        Map<String, KodeverkKode> kodeverkKodeMap = kodeverkTjeneste.hentKodeverk("Postnummer", "6", "NB");

        // Assert
        assertThat(kodeverkKodeMap.size()).isEqualTo(4);

        KodeverkKode kodeverkKode1 = new KodeverkKode.Builder()
            .medKodeverk("Postnummer")
            .medKode("7808")
            .medNavn("NAMSOS")
            .medGyldigFom(LocalDate.of(2003, 12, 3))
            .medGyldigTom(MAX_DATE)
            .build();
        KodeverkKode kodeverkKode2 = new KodeverkKode.Builder()
            .medKodeverk("Postnummer")
            .medKode("7810")
            .medNavn("NAMSOS")
            .medGyldigFom(LocalDate.of(2012, 9, 14))
            .medGyldigTom(MAX_DATE)
            .build();
        KodeverkKode kodeverkKode3 = new KodeverkKode.Builder()
            .medKodeverk("Postnummer")
            .medKode("7817")
            .medNavn("SALSNES")
            .medGyldigFom(LocalDate.of(1900, 1, 1))
            .medGyldigTom(MAX_DATE)
            .build();
        KodeverkKode kodeverkKode4 = new KodeverkKode.Builder()
            .medKodeverk("Postnummer")
            .medKode("7818")
            .medNavn("LUND")
            .medGyldigFom(LocalDate.of(1900, 1, 1))
            .medGyldigTom(MAX_DATE)
            .build();
        assertThat(kodeverkKodeMap.values()).containsExactlyInAnyOrder(kodeverkKode1, kodeverkKode2, kodeverkKode3, kodeverkKode4);
    }

    @Test
    public void skal_handtere_kodeverkikkefunnet_exception_fra_consumer()  {
        try {
            // Arrange
            HentKodeverkHentKodeverkKodeverkIkkeFunnet kodeverkIkkeFunnet = new HentKodeverkHentKodeverkKodeverkIkkeFunnet("Feil", new KodeverkIkkeFunnet());
            doThrow(kodeverkIkkeFunnet).when(kodeverkConsumer).hentKodeverk(any(HentKodeverkRequest.class));

            // Act
            kodeverkTjeneste.hentKodeverk("Dummy", null, null);
            fail("Forventet VLException");
        } catch (Exception e) {
            // Assert
            assertThat(e).isInstanceOf(IntegrasjonException.class);
            assertThat(e.getCause()).isInstanceOf(HentKodeverkHentKodeverkKodeverkIkkeFunnet.class);
            assertThat(e.getCause().getMessage()).contains("Feil");
        }
    }

    private HentKodeverkResponse opprettResponse() throws Exception {
        EnkeltKodeverk enkeltKodeverk = new EnkeltKodeverk();
        enkeltKodeverk.setNavn("Postnummer");
        enkeltKodeverk.setVersjonsnummer("6");
        enkeltKodeverk.getKode().add(lagKode("7808", "NAMSOS",
            LocalDate.of(2003, 12, 3), MAX_DATE));
        enkeltKodeverk.getKode().add(lagKode("7810", "NAMSOS",
            LocalDate.of(2012, 9, 14), MAX_DATE));
        enkeltKodeverk.getKode().add(lagKode("7817", "SALSNES",
            LocalDate.of(1900, 1, 1), MAX_DATE));
        enkeltKodeverk.getKode().add(lagKode("7818", "LUND",
            LocalDate.of(1900, 1, 1), MAX_DATE));
        HentKodeverkResponse response = new HentKodeverkResponse();
        response.setKodeverk(enkeltKodeverk);
        return response;
    }

    private Kode lagKode(String kodeNavn, String termNavn, LocalDate fom, LocalDate tom) throws DatatypeConfigurationException {
        Periode periode = new Periode();
        periode.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
        periode.setTom(DateUtil.convertToXMLGregorianCalendar(tom));
        Kode kode = new Kode();
        kode.setNavn(kodeNavn);
        kode.getGyldighetsperiode().add(periode);
        Term term = new Term();
        term.setNavn(termNavn);
        term.setSpraak("nb");
        term.getGyldighetsperiode().add(periode);
        kode.getTerm().add(term);
        return kode;
    }
}
