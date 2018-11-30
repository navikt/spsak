package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.impl.InntektTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.HentInntektListeBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.inntekt.v3.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Aktoer;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektInformasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektMaaned;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Inntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Loennsinntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Organisasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PensjonEllerTrygd;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PensjonEllerTrygdebeskrivelse;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Sikkerhetsavvik;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.YtelseFraOffentlige;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.YtelseFraOffentligeBeskrivelse;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkResponse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumer;

public class InntektTjenesteImplTest {

    private static final AktørId FNR = new AktørId("01234567890");
    private static final YearMonth GJELDENDE_MÅNED = YearMonth.now();
    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "456";
    private static final String SIKKERHETSAVVIK1 = "Mangler rettighet 1";
    private static final String SIKKERHETSAVVIK2 = "Mangler rettighet 2";

    private InntektConsumer inntektConsumer = mock(InntektConsumer.class);
    private KodeverkRepository kodeverkRepository = mock(KodeverkRepository.class);
    private InntektTjeneste inntektTjeneste;

    @Before
    public void before() {
        inntektTjeneste = new InntektTjenesteImpl(inntektConsumer, kodeverkRepository, null);
    }

    @Test
    public void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        HentInntektListeBolkResponse response = opprettResponse();

        ArgumentCaptor<HentInntektListeBolkRequest> requestCaptor = ArgumentCaptor.forClass(HentInntektListeBolkRequest.class);
        when(inntektConsumer.hentInntektListeBolk(requestCaptor.capture())).thenReturn(response);

        Organisasjon arbeidsplassen = new Organisasjon();
        arbeidsplassen.setOrgnummer(ORGNR);

        // Tre måneder siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd1 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd1.getInntektListe().add(
            opprettInntekt(new BigDecimal(50), GJELDENDE_MÅNED.minusMonths(3), YtelseFraOffentlige.class, null, SYKEPENGER));
        ArbeidsInntektMaaned arbeidsInntektMaaned1 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned1.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd1);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned1);

        // To måneder siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd2 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd2.getInntektListe().add(
            opprettInntekt(new BigDecimal(100), GJELDENDE_MÅNED.minusMonths(2), YtelseFraOffentlige.class, null, SYKEPENGER));
        arbeidsInntektInformasjonMnd2.getInntektListe().add(
            opprettInntekt(new BigDecimal(200), GJELDENDE_MÅNED.minusMonths(2), Loennsinntekt.class, arbeidsplassen, null));
        ArbeidsInntektMaaned arbeidsInntektMaaned2 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned2.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd2);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned2);

        // En måned siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd3 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd3.getInntektListe().add(
            opprettInntekt(new BigDecimal(400), GJELDENDE_MÅNED.minusMonths(1), Loennsinntekt.class, arbeidsplassen, null));
        ArbeidsInntektMaaned arbeidsInntektMaaned3 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned3.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd3);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned3);

        // Denne måneden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd4 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd4.getInntektListe().add(
            opprettInntekt(new BigDecimal(405), GJELDENDE_MÅNED, Loennsinntekt.class, arbeidsplassen, null));
        ArbeidsInntektMaaned arbeidsInntektMaaned4 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned4.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd4);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned4);

        FinnInntektRequest finnInntektRequest = new FinnInntektRequest(FNR, GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED, 1L, 10L);

        // Act
        final InntektsInformasjon inntektsInformasjon = inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);

        // Assert
        HentInntektListeBolkRequest request = requestCaptor.getValue();
        assertThat(request.getIdentListe().size()).isEqualTo(1);
        assertThat(request.getUttrekksperiode().getMaanedFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(GJELDENDE_MÅNED.minusMonths(3).atDay(1)));
        assertThat(request.getUttrekksperiode().getMaanedTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(GJELDENDE_MÅNED.atEndOfMonth()));

        final List<Månedsinntekt> månedsinntekter = inntektsInformasjon.getMånedsinntekter();
        assertThat(månedsinntekter.size()).isEqualTo(5);
        assertThat(månedsinntekter.get(0).getBeløp()).isEqualTo(new BigDecimal(50));
        assertThat(månedsinntekter.get(0).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(månedsinntekter.get(0).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).getBeløp()).isEqualTo(new BigDecimal(200));
        assertThat(månedsinntekter.get(2).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(2));
        assertThat(månedsinntekter.get(2).getArbeidsgiver()).isEqualTo(ORGNR);
        assertThat(månedsinntekter.get(2).isYtelse()).isFalse();
    }

    @Test
    public void skal_oppdage_sikkerhetsavvik_i_response_og_kaste_exception() throws Exception {
        // Arrange
        HentInntektListeBolkResponse response = opprettResponse();
        Sikkerhetsavvik sikkerhetsavvik1 = new Sikkerhetsavvik();
        sikkerhetsavvik1.setTekst(SIKKERHETSAVVIK1);
        response.getSikkerhetsavvikListe().add(sikkerhetsavvik1);
        Sikkerhetsavvik sikkerhetsavvik2 = new Sikkerhetsavvik();
        sikkerhetsavvik2.setTekst(SIKKERHETSAVVIK2);
        response.getSikkerhetsavvikListe().add(sikkerhetsavvik2);
        when(inntektConsumer.hentInntektListeBolk(any(HentInntektListeBolkRequest.class))).thenReturn(response);

        FinnInntektRequest finnInntektRequest = new FinnInntektRequest(FNR, GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED, 1L, 10L);

        try {
            // Act
            inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);
            fail("Forventet VLException");
        } catch (VLException e) {
            // Assert
            assertThat(e.getMessage()).contains(SIKKERHETSAVVIK1 + ", " + SIKKERHETSAVVIK2);
        }
    }

    @Test
    public void skal_håndtere_exceptions_fra_consumer() throws Exception {
        // Arrange
        doThrow(new HentInntektListeBolkUgyldigInput("Feil", new UgyldigInput())).when(inntektConsumer).hentInntektListeBolk(any(HentInntektListeBolkRequest.class));
        FinnInntektRequest finnInntektRequest = new FinnInntektRequest(FNR, GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED, 1L, 10L);

        try {
            // Act
            inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);
            fail("Forventet VLException");
        } catch (VLException e) {
            // Assert
            assertThat(e.getMessage()).contains("Feil");
            assertThat(e.getCause()).isInstanceOf(HentInntektListeBolkUgyldigInput.class);
        }
    }

    private HentInntektListeBolkResponse opprettResponse() throws Exception {
        HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
        ArbeidsInntektIdent arbeidsInntektIdent = new ArbeidsInntektIdent();
        response.getArbeidsInntektIdentListe().add(arbeidsInntektIdent);
        return response;
    }

    private Inntekt opprettInntekt(BigDecimal beløp, YearMonth måned, Class<? extends Inntekt> inntektType, Aktoer virksomhet, String beskrivelse) throws Exception {
        Inntekt inntekt = inntektType.newInstance();
        inntekt.setBeloep(beløp);
        inntekt.setUtbetaltIPeriode(DateUtil.convertToXMLGregorianCalendar(måned.atDay(1)));
        inntekt.setVirksomhet(virksomhet);

        if (inntekt instanceof YtelseFraOffentlige) {
            YtelseFraOffentligeBeskrivelse ytelseFraOffentligeBeskrivelse = new YtelseFraOffentligeBeskrivelse();
            ytelseFraOffentligeBeskrivelse.setValue(beskrivelse);
            ((YtelseFraOffentlige) inntekt).setBeskrivelse(ytelseFraOffentligeBeskrivelse);
        } else if (inntekt instanceof PensjonEllerTrygd) {
            PensjonEllerTrygdebeskrivelse pensjonEllerTrygdebeskrivelse = new PensjonEllerTrygdebeskrivelse();
            pensjonEllerTrygdebeskrivelse.setValue(beskrivelse);
            ((PensjonEllerTrygd) inntekt).setBeskrivelse(pensjonEllerTrygdebeskrivelse);
        }

        return inntekt;
    }
}
