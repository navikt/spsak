package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforholdstyper;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Person;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class ArbeidsforholdTestSett {

    private static final String FNR = "01234567891";
    public static final String MOCK_ORGNR = "973093681";
    public static final String MOCK_NAVN = "EPLEHAGEN AS";
    public static final String ARBEIDSFORHOLDID = "ARBEIDSFORHOLDID";

    static final Map<String, FinnArbeidsforholdPrArbeidstakerResponse> RESPONSE_MAP = new HashMap<>();

    public static FinnArbeidsforholdPrArbeidstakerResponse finnResponse(String ident) {
        return RESPONSE_MAP.getOrDefault(ident, new FinnArbeidsforholdPrArbeidstakerResponse());
    }

    public static void arbeidsforhold100prosent40timer(String fnr) { // SØKERE_MED_INNTEKTSMELDING
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        arbeidsforhold.setArbeidsforholdIDnav(1L);
        NorskIdent ident = new NorskIdent();
        ident.setIdent(FNR);
        Person person = new Person();
        person.setIdent(ident);
        Arbeidsforholdstyper arbeidsforholdstyper = new Arbeidsforholdstyper();
        arbeidsforholdstyper.setKodeRef("ordinaertArbeidsforhold");
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(MOCK_ORGNR);
        organisasjon.setNavn(MOCK_NAVN);
        arbeidsforhold.setAnsettelsesPeriode(lagAnsettelsesPeriode(false));
        arbeidsforhold.setArbeidstaker(person);
        arbeidsforhold.setArbeidsforholdstype(arbeidsforholdstyper);
        arbeidsforhold.setArbeidsgiver(organisasjon);
        arbeidsforhold.setArbeidsforholdID(ARBEIDSFORHOLDID);
        Arbeidsavtale avtale = new Arbeidsavtale();
        avtale.setStillingsprosent(BigDecimal.valueOf(100));
        avtale.setBeregnetAntallTimerPrUke(BigDecimal.valueOf(40));
        avtale.setAvtaltArbeidstimerPerUke(BigDecimal.valueOf(40));
        arbeidsforhold.getArbeidsavtale().add(avtale);
        response.getArbeidsforhold().add(arbeidsforhold);

        RESPONSE_MAP.put(fnr, response);
    }

    // Hva er forskjellen på denne og den ovenfor? Antakelig trengs bare én
    public static void arbeidsforhold100prosent40timerV2(String fnr) { //SØKERER_MED_100_PROSENT_STILLING
        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        BigDecimal beregnetAntallTimerPrUke = BigDecimal.valueOf(40);
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        Arbeidsforhold arbeidsforhold = getArbeidsforhold(stillingsprosent, beregnetAntallTimerPrUke, beregnetAntallTimerPrUke, ARBEIDSFORHOLDID, false);
        arbeidsforhold.setArbeidsforholdIDnav(10L);
        response.getArbeidsforhold().add(arbeidsforhold);

        RESPONSE_MAP.put(fnr, response);
    }

    public static void løpendeForhold100prosent40timer(String fnr) { //SØKERER_MED_100_PROSENT_LØPENDE_STILLING
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        boolean løpendeArbeidsforhold = true;
        Arbeidsforhold arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID, løpendeArbeidsforhold);
        response.getArbeidsforhold().add(arbeidsforhold);
        arbeidsforhold.setArbeidsforholdIDnav(20L);
        RESPONSE_MAP.put(fnr, response);
    }

    public static void stillingsprosent0(String fnr) { //SØKERER_MED_0_PROSENT_STILLING
        BigDecimal stillingsprosent = BigDecimal.valueOf(0);
        BigDecimal beregnetAntallTimerPrUke = BigDecimal.valueOf(0);
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        Arbeidsforhold arbeidsforhold = getArbeidsforhold(stillingsprosent, beregnetAntallTimerPrUke, beregnetAntallTimerPrUke, ARBEIDSFORHOLDID, false);
        response.getArbeidsforhold().add(arbeidsforhold);
        arbeidsforhold.setArbeidsforholdIDnav(30L);
        RESPONSE_MAP.put(fnr, response);
    }

    private static AnsettelsesPeriode lagAnsettelsesPeriode(boolean løpendeArbeidsforhold) {
        AnsettelsesPeriode periode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        try {
            XMLGregorianCalendar fra = DateUtil.convertToXMLGregorianCalendar(LocalDate.now().minusYears(3));
            gyldighetsperiode.setFom(fra);
            if (!løpendeArbeidsforhold) {
                XMLGregorianCalendar til = DateUtil.convertToXMLGregorianCalendar(LocalDate.now().plusYears(1));
                gyldighetsperiode.setTom(til);
            }
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        periode.setPeriode(gyldighetsperiode);
        return periode;
    }

    private static Arbeidsforhold getArbeidsforhold(BigDecimal stillingsprosent, BigDecimal beregnetAntallTimerPrUke, BigDecimal avtaltArbeidstimerPerUke, String arbeidsforholdid, boolean løpendeArbeidsforhold) {
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        NorskIdent ident = new NorskIdent();
        ident.setIdent(FNR);
        Person person = new Person();
        person.setIdent(ident);
        Arbeidsforholdstyper arbeidsforholdstyper = new Arbeidsforholdstyper();
        arbeidsforholdstyper.setKodeRef("ordinaertArbeidsforhold");
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(MOCK_ORGNR);
        organisasjon.setNavn(MOCK_NAVN);

        arbeidsforhold.setAnsettelsesPeriode(lagAnsettelsesPeriode(løpendeArbeidsforhold));
        arbeidsforhold.setArbeidstaker(person);
        arbeidsforhold.setArbeidsforholdstype(arbeidsforholdstyper);
        arbeidsforhold.setArbeidsgiver(organisasjon);
        arbeidsforhold.setArbeidsforholdID(arbeidsforholdid);
        Arbeidsavtale avtale = new Arbeidsavtale();
        avtale.setStillingsprosent(stillingsprosent);
        avtale.setBeregnetAntallTimerPrUke(beregnetAntallTimerPrUke);
        avtale.setAvtaltArbeidstimerPerUke(avtaltArbeidstimerPerUke);
        arbeidsforhold.getArbeidsavtale().add(avtale);
        return arbeidsforhold;
    }

    public static void nullstill() {
        RESPONSE_MAP.clear();
    }
}
