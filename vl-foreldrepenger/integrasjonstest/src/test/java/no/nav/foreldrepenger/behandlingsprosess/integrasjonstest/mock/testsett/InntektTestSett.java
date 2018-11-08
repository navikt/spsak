package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

public class InntektTestSett {

    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "973093681";
    private static final String ORGNR_FRILANS_1 = "973093682";
    private static final String ORGNR_FRILANS_2 = "973093683";
    //private static boolean returnerInntekt;
    // Brukes i Revurdering-test
    private static BigDecimal månedsinntekt;

    private static final Map<String, Function<HentInntektListeBolkRequest,HentInntektListeBolkResponse>> RESPONSE_MAP = new HashMap<>();
    private static Period antallMånederMedInnhenting;

    public static void nullstill() {
        RESPONSE_MAP.clear();
    }

    public static void inntekt36mnd40000kr(no.nav.foreldrepenger.domene.typer.PersonIdent personIdent) {
        RESPONSE_MAP.put(personIdent.getIdent(), request -> hentinntekt36mnd(request));
    }

    public static void inntektKunFrilans(no.nav.foreldrepenger.domene.typer.PersonIdent personIdent) {
        RESPONSE_MAP.put(personIdent.getIdent(), request -> hentinntektKunFrilans(request));
    }

    public static Function<HentInntektListeBolkRequest, HentInntektListeBolkResponse> finnResponsFunksjon(String aktørId, Period antallMånederMedInnhenting) {
        InntektTestSett.antallMånederMedInnhenting = antallMånederMedInnhenting;
        return RESPONSE_MAP.getOrDefault(aktørId, request -> new HentInntektListeBolkResponse());
    }

    private static HentInntektListeBolkResponse hentinntekt36mnd(HentInntektListeBolkRequest request) {
        HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
        Aktoer aktoer = request.getIdentListe().get(0);
        if (aktoer instanceof PersonIdent == false) {
            return response;
        }

        leggTilIdent(aktoer, response);

        LocalDate requestTom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedTom()).minus(antallMånederMedInnhenting);
        YearMonth sisteMåned =  YearMonth.of(requestTom.getYear(), requestTom.getMonth());
        lagInntektFor36MånederTilbake(response, opprettOrganisasjon(ORGNR), sisteMåned);

        return response;
    }

    private static HentInntektListeBolkResponse hentinntektFrilans(HentInntektListeBolkRequest request) {
        HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
        Aktoer aktoer = request.getIdentListe().get(0);
        if (aktoer instanceof PersonIdent == false) {
            return response;
        }

        leggTilIdent(aktoer, response);

        LocalDate requestTom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedTom()).minus(antallMånederMedInnhenting);
        YearMonth sisteMåned =  YearMonth.of(requestTom.getYear(), requestTom.getMonth());
        lagInntektFor36MånederTilbake(response, opprettOrganisasjon(ORGNR), sisteMåned);
        lagFrilansOppdrag(response, aktoer, sisteMåned);

        return response;
    }

    private static HentInntektListeBolkResponse hentinntektKunFrilans(HentInntektListeBolkRequest request) {
        HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
        Aktoer aktoer = request.getIdentListe().get(0);
        if (aktoer instanceof PersonIdent == false) {
            return response;
        }

        leggTilIdent(aktoer, response);

        LocalDate requestTom = DateUtil.convertToLocalDate(request.getUttrekksperiode().getMaanedTom()).minus(antallMånederMedInnhenting);
        YearMonth sisteMåned =  YearMonth.of(requestTom.getYear(), requestTom.getMonth());
        lagFrilansOppdrag(response, aktoer, sisteMåned);

        return response;
    }

    private static Organisasjon opprettOrganisasjon(String orgnr) {
        Organisasjon arbeidsplassen = new Organisasjon();
        arbeidsplassen.setOrgnummer(orgnr);
        return arbeidsplassen;
    }

    private static void leggTilIdent(Aktoer aktoer, HentInntektListeBolkResponse response) {
        ArbeidsInntektIdent arbeidsInntektIdent = new ArbeidsInntektIdent();
        arbeidsInntektIdent.setIdent(aktoer);
        response.getArbeidsInntektIdentListe().add(arbeidsInntektIdent);
    }

    private static void lagInntektFor36MånederTilbake(HentInntektListeBolkResponse response, Organisasjon arbeidsplassen, YearMonth sisteMåned)  {
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

    private static void lagFrilansOppdrag(HentInntektListeBolkResponse response, Aktoer aktoer, YearMonth requestTom) {
        ArbeidsInntektIdent arbeidsInntektIdent = response.getArbeidsInntektIdentListe().get(0);
        arbeidsInntektIdent.getArbeidsInntektMaaned().add(lagFreelanserInntektMaaned(aktoer, requestTom.minusMonths(8), ORGNR_FRILANS_1, "FRILANS-HONORAR-1", new BigDecimal(10000)));
        arbeidsInntektIdent.getArbeidsInntektMaaned().add(lagFreelanserInntektMaaned(aktoer, requestTom.minusMonths(9), ORGNR_FRILANS_2, "FRILANS-HONORAR-2", new BigDecimal(8000)));
    }

    private static Inntekt opprettInntekt(BigDecimal beløp, YearMonth måned, Class<? extends Inntekt> inntektType, Aktoer virksomhet, String beskrivelse) {
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

    private static ArbeidsInntektMaaned lagFreelanserInntektMaaned(Aktoer aktoer, YearMonth fom, String orgnr, String arbref, BigDecimal beløp) {
        try {
            Organisasjon arbeidsgiver = opprettOrganisasjon(orgnr);

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

    private static ArbeidsInntektMaaned lagarbeidsInntektMaaned(YearMonth fom, Aktoer orgnr, String arbref, BigDecimal beløp) {
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
}
