package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.impl.PeriodeCompareUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.AapenPeriode;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Aktoer;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektInformasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektMaaned;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsforholdFrilanser;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Arbeidsforholdstyper;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Avloenningstyper;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Inntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Loennsinntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Organisasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PensjonEllerTrygd;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PensjonEllerTrygdebeskrivelse;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PersonIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.YtelseFraOffentlige;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.YtelseFraOffentligeBeskrivelse;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumer;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumerProducer;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Alternative
@Priority(1)
public class InntektConsumerProducerMock extends InntektConsumerProducer {

    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "973093681";
    private static final String ORGNR_FRILANS_1 = "973093682";
    private static final String ORGNR_FRILANS_2 = "973093683";
    private static boolean returnerInntekt;
    // Brukes i Revurdering-test
    private static BigDecimal månedsinntekt;

    private static Set<String> SØKERER_MED_INNTEKTSMELDING = new HashSet<>();

    static {
        SØKERER_MED_INNTEKTSMELDING.addAll(ArbeidsforholdConsumerProducerMock.SØKERER_MED_100_PROSENT_STILLING);
        SØKERER_MED_INNTEKTSMELDING.addAll(ArbeidsforholdConsumerProducerMock.SØKERER_MED_100_PROSENT_LØPENDE_STILLING);
        SØKERER_MED_INNTEKTSMELDING.addAll(ArbeidsforholdConsumerProducerMock.SØKERER_MED_0_PROSENT_STILLING);
        SØKERER_MED_INNTEKTSMELDING.addAll(ArbeidsforholdConsumerProducerMock.SØKERER_MED_100_PROSENT_STILLING_2);
    }

    private RegisterKontekst registerKontekst;
    private Period antallMånederMedInnhenting;

    @Inject
    public InntektConsumerProducerMock(RegisterKontekst registerKontekst,
                                       @KonfigVerdi(value = "opplysningsperiode.lengde.etter") Period antallMånederMedInnhenting) {
        this.registerKontekst = registerKontekst;
        this.antallMånederMedInnhenting = antallMånederMedInnhenting;
    }

    @Override
    public InntektConsumer inntektConsumer() {
        class InntektConsumerMock implements InntektConsumer {

            @Override
            public HentInntektListeBolkResponse hentInntektListeBolk(HentInntektListeBolkRequest request) {
                HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();

                Aktoer aktoer = request.getIdentListe().get(0);
                if (!(aktoer instanceof PersonIdent)) {
                    return response;
                }

                String personIdent = ((PersonIdent) aktoer).getPersonIdent();
                if (registerKontekst.erInitalisert()) {
                    return InntektTestSett.finnResponsFunksjon(personIdent, korreksjonAvTom(request)).apply(request);
                }
                initResponse(response, aktoer);
                Organisasjon arbeidsplassen = new Organisasjon();
                arbeidsplassen.setOrgnummer(ORGNR);
                LocalDate requestTom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedTom()).minus(korreksjonAvTom(request));
                YearMonth sisteMåned = YearMonth.of(requestTom.getYear(), requestTom.getMonth());
                //YearMonth
                if (SØKERER_MED_INNTEKTSMELDING.contains(personIdent)) { // Brukes for ytelsetype Foreldrepenger
                    lagInntektFor36MånederTilbake(response, arbeidsplassen, sisteMåned);
                    return response;
                }
                if (TpsRepo.KVINNE_KUN_FRILANS_FNR.equals(personIdent)) {
                    lagFrilansOppdrag(response, aktoer, sisteMåned);
                    return response;
                }
                if (returnerInntekt) { // Brukes for ytelsetype Engangsstønad, i testene for medlemskapsvilkår
                    // Tre måneder siden
                    ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd1 = new ArbeidsInntektInformasjon();
                    arbeidsInntektInformasjonMnd1.getInntektListe().add(
                        opprettInntekt(new BigDecimal(50), sisteMåned.minusMonths(9), YtelseFraOffentlige.class, null, SYKEPENGER));
                    ArbeidsInntektMaaned arbeidsInntektMaaned1 = new ArbeidsInntektMaaned();
                    arbeidsInntektMaaned1.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd1);
                    response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned1);

                    // To måneder siden
                    ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd2 = new ArbeidsInntektInformasjon();
                    arbeidsInntektInformasjonMnd2.getInntektListe().add(
                        opprettInntekt(new BigDecimal(100), sisteMåned.minusMonths(8), YtelseFraOffentlige.class, null, SYKEPENGER));
                    arbeidsInntektInformasjonMnd2.getInntektListe().add(
                        opprettInntekt(new BigDecimal(200), sisteMåned.minusMonths(2), Loennsinntekt.class, arbeidsplassen, null));
                    ArbeidsInntektMaaned arbeidsInntektMaaned2 = new ArbeidsInntektMaaned();
                    arbeidsInntektMaaned2.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd2);
                    response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned2);

                    // En måned siden
                    ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd3 = new ArbeidsInntektInformasjon();
                    arbeidsInntektInformasjonMnd3.getInntektListe().add(
                        opprettInntekt(new BigDecimal(400), sisteMåned.minusMonths(7), Loennsinntekt.class, arbeidsplassen, null));
                    ArbeidsInntektMaaned arbeidsInntektMaaned3 = new ArbeidsInntektMaaned();
                    arbeidsInntektMaaned3.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd3);
                    response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned3);

                    // Denne måneden
                    ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd4 = new ArbeidsInntektInformasjon();
                    arbeidsInntektInformasjonMnd4.getInntektListe().add(
                        opprettInntekt(new BigDecimal(405), sisteMåned.minusMonths(6), Loennsinntekt.class, arbeidsplassen, null));
                    ArbeidsInntektMaaned arbeidsInntektMaaned4 = new ArbeidsInntektMaaned();
                    arbeidsInntektMaaned4.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd4);
                    response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned4);
                    return response;
                }
                return response;
            }

            private void lagInntektFor36MånederTilbake(HentInntektListeBolkResponse response, Organisasjon arbeidsplassen, YearMonth sisteMåned) {
                List<ArbeidsInntektMaaned> arbeidsInntektMaaned = response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned();
                for (int i = 0; i < 36; i++) {
                    ArbeidsInntektInformasjon informasjon = new ArbeidsInntektInformasjon();
                    Inntekt inntekt;
                    try {
                        inntekt = opprettInntekt(månedsinntekt != null ? månedsinntekt : new BigDecimal(40000), sisteMåned.minusMonths(i), Loennsinntekt.class, arbeidsplassen, null);
                    } catch (Exception e) {
                        throw new IllegalStateException("Klarte ikke opprette testrespons for inntekt", e);
                    }
                    informasjon.getInntektListe().add(inntekt);
                    ArbeidsInntektMaaned inntektMaaned = new ArbeidsInntektMaaned();
                    inntektMaaned.setArbeidsInntektInformasjon(informasjon);
                    arbeidsInntektMaaned.add(inntektMaaned);
                }
            }

            private void lagFrilansOppdrag(HentInntektListeBolkResponse response, Aktoer aktoer, YearMonth requestTom) {
                ArbeidsInntektIdent arbeidsInntektIdent = response.getArbeidsInntektIdentListe().get(0);
                arbeidsInntektIdent.getArbeidsInntektMaaned().add(lagFreelanserInntektMaaned(aktoer, requestTom.minusMonths(15), ORGNR_FRILANS_1, "FRILANS-HONORAR-1", new BigDecimal(10000)));
                arbeidsInntektIdent.getArbeidsInntektMaaned().add(lagFreelanserInntektMaaned(aktoer, requestTom.minusMonths(14), ORGNR_FRILANS_2, "FRILANS-HONORAR-2", new BigDecimal(8000)));
            }
        }
        return new InntektConsumerMock();
    }

    private Period korreksjonAvTom(HentInntektListeBolkRequest request) {
        final LocalDate fom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedFom());
        final LocalDate tom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedTom());
        final Period between = Period.between(fom, tom);
        if (PeriodeCompareUtil.størreEnn(between, antallMånederMedInnhenting)) {
            return antallMånederMedInnhenting;
        }
        return Period.ZERO;
    }

    private void initResponse(HentInntektListeBolkResponse response, Aktoer aktoer) {
        ArbeidsInntektIdent arbeidsInntektIdent = new ArbeidsInntektIdent();
        arbeidsInntektIdent.setIdent(aktoer);
        response.getArbeidsInntektIdentListe().add(arbeidsInntektIdent);
    }

    private Inntekt opprettInntekt(BigDecimal beløp, YearMonth måned, Class<? extends Inntekt> inntektType, Aktoer virksomhet, String beskrivelse) {
        Inntekt inntekt;
        try {
            inntekt = inntektType.newInstance();
            inntekt.setBeloep(beløp);
            inntekt.setUtbetaltIPeriode(DateUtil.convertToXMLGregorianCalendar(måned.atDay(1)));
            inntekt.setVirksomhet(virksomhet);
            if (virksomhet != null) {
                inntekt.setArbeidsforholdREF(((Organisasjon) virksomhet).getOrgnummer());
            }

            if (inntekt instanceof YtelseFraOffentlige) {
                YtelseFraOffentligeBeskrivelse ytelseFraOffentligeBeskrivelse = new YtelseFraOffentligeBeskrivelse();
                ytelseFraOffentligeBeskrivelse.setValue(beskrivelse);
                ((YtelseFraOffentlige) inntekt).setBeskrivelse(ytelseFraOffentligeBeskrivelse);
            } else if (inntekt instanceof PensjonEllerTrygd) {
                PensjonEllerTrygdebeskrivelse pensjonEllerTrygdebeskrivelse = new PensjonEllerTrygdebeskrivelse();
                pensjonEllerTrygdebeskrivelse.setValue(beskrivelse);
                ((PensjonEllerTrygd) inntekt).setBeskrivelse(pensjonEllerTrygdebeskrivelse);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Klarte ikke opprette testrespons for inntekt", e);
        }
        return inntekt;
    }

    private ArbeidsInntektMaaned lagFreelanserInntektMaaned(Aktoer aktoer, YearMonth fom, String orgnr, String arbref, BigDecimal beløp) {
        try {
            Organisasjon arbeidsgiver = new Organisasjon();
            arbeidsgiver.setOrgnummer(orgnr);

            ArbeidsInntektMaaned freelanser = lagarbeidsInntektMaaned(fom, arbeidsgiver, arbref, beløp);

            ArbeidsforholdFrilanser arbeidsforholdFrilanser = new ArbeidsforholdFrilanser();

            arbeidsforholdFrilanser.setArbeidsforholdstype(new Arbeidsforholdstyper());
            arbeidsforholdFrilanser.getArbeidsforholdstype().setKodeRef("frilanserOppdragstakerHonorarPersonerMm");
            arbeidsforholdFrilanser.getArbeidsforholdstype().setValue("frilanserOppdragstakerHonorarPersonerMm");

            arbeidsforholdFrilanser.setAvloenningstype(new Avloenningstyper());
            arbeidsforholdFrilanser.getAvloenningstype().setKodeRef("honorar");
            arbeidsforholdFrilanser.getAvloenningstype().setValue("Honorar");
            arbeidsforholdFrilanser.setStillingsprosent(new BigDecimal("3"));

            arbeidsforholdFrilanser.setFrilansPeriode(new AapenPeriode());
            arbeidsforholdFrilanser.getFrilansPeriode().setFom(DateUtil.convertToXMLGregorianCalendar(fom.atDay(1)));
            arbeidsforholdFrilanser.getFrilansPeriode().setTom(DateUtil.convertToXMLGregorianCalendar(fom.atDay(1)));

            arbeidsforholdFrilanser.setArbeidsgiver(arbeidsgiver);

            arbeidsforholdFrilanser.setArbeidstaker(aktoer);
            arbeidsforholdFrilanser.setArbeidsforholdID(arbref);
            freelanser.getArbeidsInntektInformasjon().getArbeidsforholdListe().add(arbeidsforholdFrilanser);
            return freelanser;
        } catch (Exception e) {
            throw new IllegalStateException("Klarte ikke opprette testrespons for inntekt", e);
        }
    }

    private ArbeidsInntektMaaned lagarbeidsInntektMaaned(YearMonth fom, Aktoer orgnr, String arbref, BigDecimal beløp) {
        try {
            ArbeidsInntektMaaned arbeidsInntektMaaned = new ArbeidsInntektMaaned();
            arbeidsInntektMaaned.setAarMaaned(DateUtil.convertToXMLGregorianCalendar(fom.atDay(1)));
            arbeidsInntektMaaned.setArbeidsInntektInformasjon(new ArbeidsInntektInformasjon());
            arbeidsInntektMaaned.getArbeidsInntektInformasjon().getInntektListe().add(opprettInntekt(beløp, fom, Loennsinntekt.class, orgnr, arbref));
            return arbeidsInntektMaaned;
        } catch (Exception e) {
            throw new IllegalStateException("Klarte ikke opprette testrespons for inntekt", e);
        }
    }

    public void setMånedsinntekt(BigDecimal månedsinntekt) {
        InntektConsumerProducerMock.månedsinntekt = månedsinntekt;
    }


    public void setReturnerInntekt(boolean returnerInntekt) {
        InntektConsumerProducerMock.returnerInntekt = returnerInntekt;
    }
}
