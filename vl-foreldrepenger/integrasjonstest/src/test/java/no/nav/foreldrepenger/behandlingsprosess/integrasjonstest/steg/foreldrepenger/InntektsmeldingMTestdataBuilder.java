package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.NaturalytelseKodeliste;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.YtelseKodeliste;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.ÅrsakInnsendingKodeliste;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.ÅrsakUtsettelseKodeliste;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Arbeidsforhold;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Arbeidsgiver;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Avsendersystem;
import no.seres.xsd.nav.inntektsmelding_m._20180924.EndringIRefusjon;
import no.seres.xsd.nav.inntektsmelding_m._20180924.EndringIRefusjonsListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.GraderingIForeldrepenger;
import no.seres.xsd.nav.inntektsmelding_m._20180924.GraderingIForeldrepengerListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Inntekt;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Kontaktinformasjon;
import no.seres.xsd.nav.inntektsmelding_m._20180924.NaturalytelseDetaljer;
import no.seres.xsd.nav.inntektsmelding_m._20180924.ObjectFactory;
import no.seres.xsd.nav.inntektsmelding_m._20180924.OpphoerAvNaturalytelseListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Periode;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Refusjon;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Skjemainnhold;
import no.seres.xsd.nav.inntektsmelding_m._20180924.UtsettelseAvForeldrepenger;
import no.seres.xsd.nav.inntektsmelding_m._20180924.UtsettelseAvForeldrepengerListe;

public class InntektsmeldingMTestdataBuilder {


    private static final BigDecimal INNTEKTBELOEP_50000 = BigDecimal.valueOf(50000);
    private static final BigDecimal REFUSJON_PR_MAENED_10000 = BigDecimal.valueOf(10000);
    private static final LocalDate STARTDATO_FORELDREPENGER = LocalDate.now(FPDateUtil.getOffset()).minusMonths(3);

    private InntektsmeldingM inntektsmeldingM = new InntektsmeldingM();

    private PersonIdent personIdent;
    private BigDecimal inntektBeløp;
    private BigDecimal refusjonsbeløpPrMnd;
    private LocalDate startdatoForeldrepenger;
    private GraderingIForeldrepengerListe graderingIForeldrepengerListe;
    private String arbeidsforholdId;

    private EndringIRefusjonsListe endringIRefusjonsListe;

    /**
     * @deprecated Bruk #medPersonIdent i stedet.
     */
    @Deprecated
    public InntektsmeldingMTestdataBuilder inntektsmelding(String fnr) {
        return this
            .medPersonIdent(PersonIdent.fra(fnr));
    }

    public InntektsmeldingMTestdataBuilder inntektsmelding(PersonIdent personIdent) {
        return this
            .medPersonIdent(personIdent);
    }

    public static InntektsmeldingMTestdataBuilder inntektsmelding40000kr(PersonIdent personIdent) {
        return new InntektsmeldingMTestdataBuilder()
            .medPersonIdent(personIdent)
            .medInntekt(BigDecimal.valueOf(40000));
    }

    public static InntektsmeldingMTestdataBuilder inntektsmelding(PersonIdent personIdent, BigDecimal inntektBeløp) {
        return new InntektsmeldingMTestdataBuilder()
            .medPersonIdent(personIdent)
            .medInntekt(inntektBeløp);
    }


    public InntektsmeldingM build() { // NOSONAR "Split it into smaller methods" - ingenting å hente på det her

        ObjectFactory objectFactory = new ObjectFactory();
        Skjemainnhold skjemaInnhold = objectFactory.createSkjemainnhold();

        skjemaInnhold.setYtelse(YtelseKodeliste.FORELDREPENGER.value());
        skjemaInnhold.setAarsakTilInnsending(ÅrsakInnsendingKodeliste.NY.value());

        // Arbeidsgiver
        Arbeidsgiver arbeidsgiver = objectFactory.createArbeidsgiver();
        arbeidsgiver.setVirksomhetsnummer("973093681");
        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjon.setKontaktinformasjonNavn("Fru Corporate");
        kontaktinformasjon.setTelefonnummer("99009900");
        arbeidsgiver.setKontaktinformasjon(kontaktinformasjon);
        skjemaInnhold.setArbeidsgiver(arbeidsgiver);

        skjemaInnhold.setArbeidstakerFnr(personIdent.getIdent());
        skjemaInnhold.setNaerRelasjon(false);

        // Arbeidsforhold - beregnetInntekt
        Arbeidsforhold arbeidsforhold = objectFactory.createArbeidsforhold();
        JAXBElement<String> arbeidsforholdIdElement = objectFactory.createArbeidsforholdArbeidsforholdId(arbeidsforholdId == null ? "ARBEIDSFORHOLDID" : arbeidsforholdId);
        arbeidsforhold.setArbeidsforholdId(arbeidsforholdIdElement);
        JAXBElement<Arbeidsforhold> elementArbeidsforhold = objectFactory.createSkjemainnholdArbeidsforhold(arbeidsforhold);

        Inntekt inntekt = new Inntekt();
        BigDecimal inntektBeloep = Optional.ofNullable(inntektBeløp).orElse(INNTEKTBELOEP_50000);
        JAXBElement<BigDecimal> elementInntektBeloep = objectFactory.createInntektBeloep(inntektBeloep);
        inntekt.setBeloep(elementInntektBeloep);
        JAXBElement<Inntekt> elementInntekt = objectFactory.createArbeidsforholdBeregnetInntekt(inntekt);
        arbeidsforhold.setBeregnetInntekt(elementInntekt);

        // Arbeidsforhold - utsettelseAvForeldrepengerListe
        UtsettelseAvForeldrepengerListe utsettelseAvForeldrepengerListe = objectFactory.createUtsettelseAvForeldrepengerListe();
        UtsettelseAvForeldrepenger utsettelseAvForeldrepenger = objectFactory.createUtsettelseAvForeldrepenger();
        no.seres.xsd.nav.inntektsmelding_m._20180924.Periode periode1 = objectFactory.createPeriode();
        periode1.setFom(objectFactory.createPeriodeFom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).minusMonths(12))));
        periode1.setTom(objectFactory.createPeriodeFom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).minusMonths(1))));
        JAXBElement<no.seres.xsd.nav.inntektsmelding_m._20180924.Periode> elementPeriode1 = objectFactory.createUtsettelseAvForeldrepengerPeriode(periode1);
        utsettelseAvForeldrepenger.setPeriode(elementPeriode1);
        JAXBElement<String> aarsakTilUtsettelse = objectFactory.createUtsettelseAvForeldrepengerAarsakTilUtsettelse(ÅrsakUtsettelseKodeliste.ARBEID.value());
        utsettelseAvForeldrepenger.setAarsakTilUtsettelse(aarsakTilUtsettelse);
        JAXBElement<UtsettelseAvForeldrepengerListe> elementUtsettelseAvForeldrepengerListe = objectFactory.createArbeidsforholdUtsettelseAvForeldrepengerListe(utsettelseAvForeldrepengerListe);
        arbeidsforhold.setUtsettelseAvForeldrepengerListe(elementUtsettelseAvForeldrepengerListe);

        // Arbeidsforhold - graderingIForeldrepengerListe
        if (graderingIForeldrepengerListe == null) {
            graderingIForeldrepengerListe = objectFactory.createGraderingIForeldrepengerListe();
        }
        JAXBElement<GraderingIForeldrepengerListe> graderingIForeldrepengerListeJAXBElement = objectFactory.createArbeidsforholdGraderingIForeldrepengerListe(graderingIForeldrepengerListe);
        arbeidsforhold.setGraderingIForeldrepengerListe(graderingIForeldrepengerListeJAXBElement);

        skjemaInnhold.setArbeidsforhold(elementArbeidsforhold);

        // Refusjon
        Refusjon refusjon = objectFactory.createRefusjon();
        BigDecimal refusjonsbeloepPrMnd = Optional.ofNullable(refusjonsbeløpPrMnd).orElse(REFUSJON_PR_MAENED_10000);
        refusjon.setRefusjonsbeloepPrMnd(objectFactory.createRefusjonRefusjonsbeloepPrMnd(refusjonsbeloepPrMnd));

        //Endringer i refusjon
        if (endringIRefusjonsListe == null) {
            endringIRefusjonsListe = objectFactory.createEndringIRefusjonsListe();
        }
        JAXBElement<EndringIRefusjonsListe> refusjonEndringIRefusjonListe = objectFactory.createRefusjonEndringIRefusjonListe(endringIRefusjonsListe);
        refusjon.setEndringIRefusjonListe(refusjonEndringIRefusjonListe);


        JAXBElement<Refusjon> elementRefusjon = objectFactory.createSkjemainnholdRefusjon(refusjon);
        skjemaInnhold.setRefusjon(elementRefusjon);

        LocalDate startdato = Optional.ofNullable(startdatoForeldrepenger).orElse(STARTDATO_FORELDREPENGER);
        skjemaInnhold.setStartdatoForeldrepengeperiode(objectFactory.createSkjemainnholdStartdatoForeldrepengeperiode(konverterDatoUtenTidssone(startdato)));

        // Opphør av naturalytelse
        OpphoerAvNaturalytelseListe opphoerAvNaturalytelseListe = objectFactory.createOpphoerAvNaturalytelseListe();
        NaturalytelseDetaljer detaljer = objectFactory.createNaturalytelseDetaljer();
        detaljer.setFom(objectFactory.createPeriodeFom(konverterDatoUtenTidssone(LocalDate.now(FPDateUtil.getOffset()).minusMonths(4))));
        detaljer.setBeloepPrMnd(objectFactory.createNaturalytelseDetaljerBeloepPrMnd(BigDecimal.valueOf(100)));
        detaljer.setNaturalytelseType(objectFactory.createNaturalytelseDetaljerNaturalytelseType(NaturalytelseKodeliste.ELEKTRONISK_KOMMUNIKASJON.value()));
        opphoerAvNaturalytelseListe.getOpphoerAvNaturalytelse().add(detaljer);
        JAXBElement<OpphoerAvNaturalytelseListe> opphørAvNaturytelse = objectFactory.createSkjemainnholdOpphoerAvNaturalytelseListe(opphoerAvNaturalytelseListe);
        skjemaInnhold.setOpphoerAvNaturalytelseListe(opphørAvNaturytelse);

        // Avsendersystem
        Avsendersystem avsendersystem = objectFactory.createAvsendersystem();
        avsendersystem.setSystemnavn("FS32");
        avsendersystem.setSystemversjon("1.0");
        skjemaInnhold.setAvsendersystem(avsendersystem);

        inntektsmeldingM.setSkjemainnhold(skjemaInnhold);
        return inntektsmeldingM;
    }

    public InntektsmeldingMTestdataBuilder medGradering(LocalDate fom, LocalDate tom, BigDecimal prosent) {
        ObjectFactory objectFactory = new ObjectFactory();
        if (graderingIForeldrepengerListe == null) {
            graderingIForeldrepengerListe = objectFactory.createGraderingIForeldrepengerListe();
        }

        GraderingIForeldrepenger graderingIForeldrepenger = objectFactory.createGraderingIForeldrepenger();
        Periode graderingPeriode = objectFactory.createPeriode();
        graderingPeriode.setFom(objectFactory.createPeriodeFom(konverterDatoUtenTidssone(fom)));
        graderingPeriode.setTom(objectFactory.createPeriodeTom(konverterDatoUtenTidssone(tom)));
        graderingIForeldrepenger.setPeriode(objectFactory.createGraderingIForeldrepengerPeriode(graderingPeriode));
        graderingIForeldrepenger.setArbeidstidprosent(objectFactory.createGraderingIForeldrepengerArbeidstidprosent(BigInteger.valueOf(prosent.longValue())));

        graderingIForeldrepengerListe.getGraderingIForeldrepenger().add(graderingIForeldrepenger);
        return this;
    }

    InntektsmeldingMTestdataBuilder medEndringRefusjon(BigDecimal beløp, LocalDate fom) {
        ObjectFactory objectFactory = new ObjectFactory();
        if (endringIRefusjonsListe == null) {
            endringIRefusjonsListe = objectFactory.createEndringIRefusjonsListe();
        }

        EndringIRefusjon endringIRefusjon = objectFactory.createEndringIRefusjon();
        endringIRefusjon.setRefusjonsbeloepPrMnd(objectFactory.createEndringIRefusjonRefusjonsbeloepPrMnd(beløp));
        endringIRefusjon.setEndringsdato(objectFactory.createEndringIRefusjonEndringsdato(konverterDatoUtenTidssone(fom)));
        endringIRefusjonsListe.getEndringIRefusjon().add(endringIRefusjon);
        return this;
    }

    private InntektsmeldingMTestdataBuilder medPersonIdent(PersonIdent personIdent) {
        Objects.requireNonNull(personIdent);
        this.personIdent = personIdent;
        return this;
    }

    public InntektsmeldingMTestdataBuilder medInntekt(BigDecimal inntektBeløp) {
        this.inntektBeløp = inntektBeløp;
        return this;
    }

    public InntektsmeldingMTestdataBuilder medArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
        return this;
    }

    public InntektsmeldingMTestdataBuilder medRefusjonsbeloepPrMnd(BigDecimal refusjonsbeloepPrMnd) {
        this.refusjonsbeløpPrMnd = refusjonsbeloepPrMnd;
        return this;
    }

    public InntektsmeldingMTestdataBuilder medStartdatoForeldrepenger(LocalDate startdatoForeldrepenger) {
        this.startdatoForeldrepenger = startdatoForeldrepenger;
        return this;
    }

    private static XMLGregorianCalendar konverterDato(LocalDate dato) {
        try {
            return DateUtil.convertToXMLGregorianCalendar(dato);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static XMLGregorianCalendar konverterDatoUtenTidssone(LocalDate dato) {
        try {
            return DateUtil.convertToXMLGregorianCalendarRemoveTimezone(dato);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
