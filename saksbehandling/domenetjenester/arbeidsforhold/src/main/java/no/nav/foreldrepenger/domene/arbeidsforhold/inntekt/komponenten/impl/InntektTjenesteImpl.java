package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.impl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsFilter;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsFormål;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.AapenPeriode;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Ainntektsfilter;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Aktoer;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.AktoerId;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektInformasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsInntektMaaned;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ArbeidsforholdFrilanser;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Formaal;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Inntekt;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.ObjectFactory;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Organisasjon;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PensjonEllerTrygd;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PersonIdent;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Sikkerhetsavvik;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Uttrekksperiode;
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.YtelseFraOffentlige;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumer;

@Dependent
public class InntektTjenesteImpl implements InntektTjeneste {

    private ObjectFactory objectFactory = new ObjectFactory();

    private InntektConsumer inntektConsumer;
    private KodeverkRepository kodeverkRepository;
    private TpsTjeneste tpsTjeneste;
    private Map<InntektsKilde, Set<InntektsFilter>> kildeTilFilter;
    private Map<InntektsFilter, Set<InntektsFormål>> filterTilFormål;

    InntektTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public InntektTjenesteImpl(InntektConsumer inntektConsumer, KodeverkRepository kodeverkRepository, TpsTjeneste tpsTjeneste) {
        this.inntektConsumer = inntektConsumer;
        this.kodeverkRepository = kodeverkRepository;
        this.tpsTjeneste = tpsTjeneste;
        this.kildeTilFilter = kodeverkRepository.hentKodeRelasjonForKodeverk(InntektsKilde.class, InntektsFilter.class);
        this.filterTilFormål = kodeverkRepository.hentKodeRelasjonForKodeverk(InntektsFilter.class, InntektsFormål.class);
    }

    @Override
    public InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) {
        HentInntektListeBolkResponse response;
        try {
            HentInntektListeBolkRequest request = opprettRequest(finnInntektRequest, kilde);

            response = inntektConsumer.hentInntektListeBolk(request);
        } catch (Exception e) {
            throw InntektFeil.FACTORY.feilVedKallTilInntekt(e).toException();
        }
        return oversettResponse(response, kilde);
    }

    private HentInntektListeBolkRequest opprettRequest(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) throws DatatypeConfigurationException {
        HentInntektListeBolkRequest request = new HentInntektListeBolkRequest();

        AktoerId personIdent = objectFactory.createAktoerId();
        personIdent.setAktoerId(finnInntektRequest.getAktørId().getId());
        request.getIdentListe().add(personIdent);

        Ainntektsfilter ainntektsfilter = objectFactory.createAinntektsfilter();
        InntektsFilter filter = getFilter(kilde);
        ainntektsfilter.setValue(filter.getOffisiellKode());
        request.setAinntektsfilter(ainntektsfilter);

        Uttrekksperiode uttrekksperiode = objectFactory.createUttrekksperiode();
        uttrekksperiode.setMaanedFom(DateUtil.convertToXMLGregorianCalendar(finnInntektRequest.getFom().atDay(1)));
        uttrekksperiode.setMaanedTom(DateUtil.convertToXMLGregorianCalendar(finnInntektRequest.getTom().atEndOfMonth()));
        request.setUttrekksperiode(uttrekksperiode);

        Formaal formaal = objectFactory.createFormaal();
        formaal.setValue(getFormål(filter).getOffisiellKode());
        formaal.setKodeRef(getFormål(filter).getOffisiellKode());
        request.setFormaal(formaal);

        return request;
    }

    private InntektsFilter getFilter(InntektsKilde kilde) {
        // Skal bare få en verdi.
        return kildeTilFilter.getOrDefault(kilde, Collections.emptySet()).stream().findFirst().orElse(InntektsFilter.UDEFINERT);
    }

    private InntektsFormål getFormål(InntektsFilter filter) {
        // Skal bare få en verdi.
        return filterTilFormål.getOrDefault(filter, Collections.emptySet()).stream().findFirst().orElse(InntektsFormål.UDEFINERT);
    }

    private InntektsInformasjon oversettResponse(HentInntektListeBolkResponse response, InntektsKilde kilde) {
        if (response.getSikkerhetsavvikListe() != null && !response.getSikkerhetsavvikListe().isEmpty()) {
            throw InntektFeil.FACTORY.fikkSikkerhetsavvikFraInntekt(byggSikkerhetsavvikString(response)).toException();
        }

        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();

        List<ArbeidsInntektIdent> arbeidsInntektIdentListe = response.getArbeidsInntektIdentListe();
        for (ArbeidsInntektIdent arbeidsInntektIdent : arbeidsInntektIdentListe) {
            for (ArbeidsInntektMaaned arbeidsInntektMaaned : arbeidsInntektIdent.getArbeidsInntektMaaned()) {
                final ArbeidsInntektInformasjon arbeidsInntektInformasjon = oversettInntekter(månedsinntekter, arbeidsInntektMaaned);

                oversettArbeidsforhold(arbeidsforhold, arbeidsInntektInformasjon);
            }
        }

        return new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
    }

    private ArbeidsInntektInformasjon oversettInntekter(List<Månedsinntekt> månedsinntekter, ArbeidsInntektMaaned arbeidsInntektMaaned) {
        final ArbeidsInntektInformasjon arbeidsInntektInformasjon = arbeidsInntektMaaned.getArbeidsInntektInformasjon();
        for (Inntekt inntekt : arbeidsInntektInformasjon.getInntektListe()) {
            Månedsinntekt.Builder månedsinntekt = new Månedsinntekt.Builder()
                .medBeløp(inntekt.getBeloep());

            if (inntekt.getUtbetaltIPeriode() != null) {
                månedsinntekt.medMåned(YearMonth.from(DateUtil.convertToLocalDate(inntekt.getUtbetaltIPeriode())));
            }
            utledOgSettUtbetalerOgYtelse(inntekt, månedsinntekt);

            månedsinntekter.add(månedsinntekt.build());
        }
        return arbeidsInntektInformasjon;
    }

    private void oversettArbeidsforhold(List<FrilansArbeidsforhold> arbeidsforhold, ArbeidsInntektInformasjon arbeidsInntektInformasjon) {
        for (ArbeidsforholdFrilanser arbeidsforholdFrilanser : arbeidsInntektInformasjon.getArbeidsforholdListe()) {
            final FrilansArbeidsforhold.Builder builder = FrilansArbeidsforhold.builder();
            final AapenPeriode frilansPeriode = arbeidsforholdFrilanser.getFrilansPeriode();
            builder.medArbeidsforholdId(arbeidsforholdFrilanser.getArbeidsforholdID())
                .medType(kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforholdFrilanser.getArbeidsforholdstype().getValue()))
                .medSisteEndringIStillingsprosent(DateUtil.convertToLocalDate(arbeidsforholdFrilanser.getSisteDatoForStillingsprosentendring()))
                .medSisteEndringILønn(DateUtil.convertToLocalDate(arbeidsforholdFrilanser.getSisteLoennsendring()))
                .medStillingsprosent(arbeidsforholdFrilanser.getStillingsprosent());

            if (arbeidsforholdFrilanser.getAntallTimerPerUkeSomEnFullStillingTilsvarer() != null) {
                builder.medBeregnetAntallTimerPerUke(BigDecimal.valueOf(arbeidsforholdFrilanser.getAntallTimerPerUkeSomEnFullStillingTilsvarer()));
            }

            oversettArbeidsgiver(arbeidsforholdFrilanser, builder);

            if (frilansPeriode != null) {
                builder.medFom(DateUtil.convertToLocalDate(frilansPeriode.getFom()))
                    .medTom(DateUtil.convertToLocalDate(frilansPeriode.getTom()));
            }

            arbeidsforhold.add(builder.build());
        }
    }

    private void oversettArbeidsgiver(ArbeidsforholdFrilanser arbeidsforholdFrilanser, FrilansArbeidsforhold.Builder builder) {
        final Aktoer arbeidsgiver = arbeidsforholdFrilanser.getArbeidsgiver();
        if (arbeidsgiver instanceof AktoerId) {
            builder.medArbeidsgiverAktørId(new AktørId(((AktoerId) arbeidsgiver).getAktoerId()));
        } else if (arbeidsgiver instanceof Organisasjon) {
            builder.medArbeidsgiverOrgnr(((Organisasjon) arbeidsgiver).getOrgnummer());
        } else if (arbeidsgiver instanceof PersonIdent) {
            final AktørId aktørId = tpsTjeneste
                .hentAktørForFnr(no.nav.foreldrepenger.domene.typer.PersonIdent.fra(((PersonIdent) arbeidsgiver).getPersonIdent()))
                .orElse(null);
            builder.medArbeidsgiverAktørId(aktørId);
        }
    }

    private String hentUtIdFraAktoer(Aktoer aktoer) {
        if (aktoer != null) {
            if (aktoer instanceof AktoerId) {
                return ((AktoerId) aktoer).getAktoerId();
            } else if (aktoer instanceof Organisasjon) {
                return ((Organisasjon) aktoer).getOrgnummer();
            } else if (aktoer instanceof PersonIdent) {
                return hentAktørIdForIdent((PersonIdent) aktoer)
                    .orElseThrow(() -> new IllegalStateException("Finner ikke aktørId for person " + ((PersonIdent) aktoer).getPersonIdent())).getId();
            }
        }
        return null;
    }

    private Optional<AktørId> hentAktørIdForIdent(PersonIdent arbeidsgiver) {
        return tpsTjeneste.hentAktørForFnr(no.nav.foreldrepenger.domene.typer.PersonIdent.fra(arbeidsgiver.getPersonIdent()));
    }

    private void utledOgSettUtbetalerOgYtelse(Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        if (erYtelse(inntekt)) {
            final YtelseFraOffentlige ytelseFraOffentlige = (YtelseFraOffentlige) inntekt; // NOSONAR
            månedsinntekt.medYtelse(true)
                .medYtelseKode(ytelseFraOffentlige.getBeskrivelse().getValue());
        } else if (erPensjonEllerTrygd(inntekt)) {
            final PensjonEllerTrygd pensjonEllerTrygd = (PensjonEllerTrygd) inntekt; // NOSONAR
            månedsinntekt.medYtelse(true)
                .medPensjonEllerTrygdKode(pensjonEllerTrygd.getBeskrivelse().getValue());
        } else {
            månedsinntekt.medYtelse(false);
            månedsinntekt.medArbeidsgiver(hentUtIdFraAktoer(inntekt.getVirksomhet()));
            månedsinntekt.medArbeidsforholdRef(inntekt.getArbeidsforholdREF());
        }
    }

    private boolean erYtelse(Inntekt inntekt) {
        return inntekt instanceof YtelseFraOffentlige;
    }

    private boolean erPensjonEllerTrygd(Inntekt inntekt) {
        return inntekt instanceof PensjonEllerTrygd;
    }

    private String byggSikkerhetsavvikString(HentInntektListeBolkResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Sikkerhetsavvik> sikkerhetsavvikListe = response.getSikkerhetsavvikListe();
        if (!sikkerhetsavvikListe.isEmpty()) {
            stringBuilder.append(sikkerhetsavvikListe.get(0).getTekst());
            for (int i = 1; i < sikkerhetsavvikListe.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(sikkerhetsavvikListe.get(i).getTekst());
            }
        }
        return stringBuilder.toString();
    }
}
