package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforholdstyper;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Person;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumerProducer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@Alternative
@Priority(1)
@Dependent
public class ArbeidsforholdConsumerProducerMock extends ArbeidsforholdConsumerProducer {

    public static final String MOCK_ORGNR = "973093681";
    public static final String MOCK_NAVN = "EPLEHAGEN AS";
    public static final String ARBEIDSFORHOLDID = "ARBEIDSFORHOLDID";
    public static final String ARBEIDSFORHOLDID1 = ARBEIDSFORHOLDID + "1";
    public static final String ARBEIDSFORHOLDID2 = ARBEIDSFORHOLDID + "2";
    public static final LocalDate LØNNSENDRING_DATO = LocalDate.now().minusMonths(2);
    static final Set<String> SØKERER_MED_100_PROSENT_STILLING = new HashSet<>(asList(
        TpsRepo.KVINNE_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR,
        TpsRepo.KVINNE_MEDL_UAVKL_PERIODE_FNR,
        TpsRepo.MANN_MED_INNTEKT_40k_OG_ARBEIDSFORHOLD_FNR
    ));
    static final Set<String> SØKERER_MED_100_PROSENT_STILLING_2 = new HashSet<>(asList(
        TpsRepo.KVINNE_MED_INNTEKT_40k_OG_2_ARBEIDSFORHOLD_FNR
    ));
    static final Set<String> SØKERER_MED_100_PROSENT_LØPENDE_STILLING = new HashSet<>(asList(
        TpsRepo.KVINNE_MED_INNTEKT_40k_OG_LØPENDE_ARBEIDSFORHOLD_FNR
    ));
    static final Set<String> SØKERER_MED_0_PROSENT_STILLING = new HashSet<>(asList(
        TpsRepo.KVINNE_MED_INNTEKT_OG_0_PROSENT_ARBEIDSFORHOLD_FNR,
        TpsRepo.MEDMOR_MED_INNTEKT_FNR
    ));
    private static final String FNR = "01234567891";

    @Override
    public ArbeidsforholdConsumer arbeidsforholdConsumer() {
        class ArbeidsforholdConsumerMock implements ArbeidsforholdConsumer {

            @Override
            public FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstaker(FinnArbeidsforholdPrArbeidstakerRequest var1) {
                return ArbeidsforholdTestSett.finnResponse(var1.getIdent().getIdent());
            }

            @Override
            public HentArbeidsforholdHistorikkResponse hentArbeidsforholdHistorikk(HentArbeidsforholdHistorikkRequest hentArbeidsforholdHistorikkRequest) throws HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet, HentArbeidsforholdHistorikkSikkerhetsbegrensning {
                final HentArbeidsforholdHistorikkResponse response = new HentArbeidsforholdHistorikkResponse();
                boolean løpendeArbeidsforhold;
                final long arbeidsforholdId = hentArbeidsforholdHistorikkRequest.getArbeidsforholdId();
                if (arbeidsforholdId < 10) {
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
                    avtale.setFomGyldighetsperiode(arbeidsforhold.getAnsettelsesPeriode().getPeriode().getFom());
                    avtale.setTomGyldighetsperiode(arbeidsforhold.getAnsettelsesPeriode().getPeriode().getTom());
                    arbeidsforhold.getArbeidsavtale().add(avtale);
                    response.setArbeidsforhold(arbeidsforhold);
                } else if (arbeidsforholdId < 20) {
                    Arbeidsforhold arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID, false, false);
                    arbeidsforhold.setArbeidsforholdIDnav(10L);
                    response.setArbeidsforhold(arbeidsforhold);
                } else if (arbeidsforholdId < 30) {
                    løpendeArbeidsforhold = true;
                    Arbeidsforhold arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID, løpendeArbeidsforhold, false);
                    arbeidsforhold.setArbeidsforholdIDnav(20L);
                    response.setArbeidsforhold(arbeidsforhold);
                } else if (arbeidsforholdId < 40) {
                    Arbeidsforhold arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), ARBEIDSFORHOLDID, false, false);
                    arbeidsforhold.setArbeidsforholdIDnav(30L);
                    response.setArbeidsforhold(arbeidsforhold);
                } else if (arbeidsforholdId < 50) {
                    Arbeidsforhold arbeidsforhold;
                    if (arbeidsforholdId == 40) {
                        arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID1, false, false);
                        arbeidsforhold.setArbeidsforholdIDnav(40L);
                    }
                    if (arbeidsforholdId == 41) {
                        arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID2, false, false);
                        arbeidsforhold.setArbeidsforholdIDnav(41L);
                    }
                } else if (arbeidsforholdId < 60) {
                    Arbeidsforhold arbeidsforhold = getArbeidsforhold(BigDecimal.valueOf(100), BigDecimal.valueOf(40), BigDecimal.valueOf(40), ARBEIDSFORHOLDID, true, true);
                    arbeidsforhold.setArbeidsforholdIDnav(60L);
                    response.setArbeidsforhold(arbeidsforhold);
                }
                return response;
            }
        }
        return new ArbeidsforholdConsumerMock();
    }

    private Arbeidsforhold getArbeidsforhold(BigDecimal stillingsprosent, BigDecimal beregnetAntallTimerPrUke, BigDecimal avtaltArbeidstimerPerUke, String arbeidsforholdid, boolean løpendeArbeidsforhold, boolean nyligLønnsendring) {
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
        avtale.setFomGyldighetsperiode(arbeidsforhold.getAnsettelsesPeriode().getPeriode().getFom());
        avtale.setTomGyldighetsperiode(arbeidsforhold.getAnsettelsesPeriode().getPeriode().getTom());
        avtale.setSisteLoennsendringsdato(nyligLønnsendring ? getXMLGregorianCalendarFromLocalDate(LØNNSENDRING_DATO)
            : arbeidsforhold.getAnsettelsesPeriode().getPeriode().getFom());
        arbeidsforhold.getArbeidsavtale().add(avtale);
        return arbeidsforhold;
    }

    private AnsettelsesPeriode lagAnsettelsesPeriode(boolean løpendeArbeidsforhold) {
        AnsettelsesPeriode periode = new AnsettelsesPeriode();
        Gyldighetsperiode gyldighetsperiode = new Gyldighetsperiode();
        XMLGregorianCalendar fra = getXMLGregorianCalendarFromLocalDate(LocalDate.now().minusYears(3));
        gyldighetsperiode.setFom(fra);
        if (!løpendeArbeidsforhold) {
            XMLGregorianCalendar til = getXMLGregorianCalendarFromLocalDate(LocalDate.now().plusYears(1));
            gyldighetsperiode.setTom(til);
        }
        periode.setPeriode(gyldighetsperiode);
        return periode;
    }

    private XMLGregorianCalendar getXMLGregorianCalendarFromLocalDate(LocalDate date) {
        try {
            return DateUtil.convertToXMLGregorianCalendar(date);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException();
        }
    }

}
