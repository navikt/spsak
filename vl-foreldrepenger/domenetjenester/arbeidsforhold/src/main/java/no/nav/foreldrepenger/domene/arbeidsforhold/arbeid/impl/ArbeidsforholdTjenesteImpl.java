package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl;

import static no.nav.foreldrepenger.behandlingslager.IntervallUtil.byggIntervall;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsavtale;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Permisjon;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Person;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class ArbeidsforholdTjenesteImpl implements ArbeidsforholdTjeneste {

    private static final String TJENESTE = "Arbeidsforhold";
    private ArbeidsforholdConsumer arbeidsforholdConsumer;
    private TpsTjeneste tpsTjeneste;
    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTjenesteImpl.class);

    public ArbeidsforholdTjenesteImpl() {
        // CDI
    }

    @Inject
    public ArbeidsforholdTjenesteImpl(ArbeidsforholdConsumer arbeidsforholdConsumer, TpsTjeneste tpsTjeneste) {
        this.arbeidsforholdConsumer = arbeidsforholdConsumer;
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent fnr, Interval interval) {
        final FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstakerResponse =
            finnArbeidsForhold(fnr, interval);

        // Tar bare de arbeidsforholdene som er løpende.
        return mapArbeidsforholdResponseToArbeidsforhold(finnArbeidsforholdPrArbeidstakerResponse, interval);
    }

    FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsForhold(PersonIdent fnr, Interval opplysningsPeriode) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        Periode periode = new Periode();
        NorskIdent ident = new NorskIdent();
        Regelverker regelverk = new Regelverker();

        try {
            periode.setFom(DateUtil.convertToXMLGregorianCalendar(LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault())));
            periode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDateTime.ofInstant(opplysningsPeriode.getEnd(), ZoneId.systemDefault())));
            request.setArbeidsforholdIPeriode(periode);

            ident.setIdent(fnr.getIdent());
            request.setIdent(ident);

            regelverk.setKodeRef("A_ORDNINGEN");
            regelverk.setValue("A_ORDNINGEN");
            request.setRapportertSomRegelverk(regelverk);
            return arbeidsforholdConsumer.finnArbeidsforholdPrArbeidstaker(request);
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (DatatypeConfigurationException e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.tekniskFeil(TJENESTE, e).toException();
        }
    }

    Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> mapArbeidsforholdResponseToArbeidsforhold(FinnArbeidsforholdPrArbeidstakerResponse response, Interval interval) {
        if (response != null) {
            return response.getArbeidsforhold().stream()
                .map(arbeidsforhold -> mapArbeidsforholdTilDto(arbeidsforhold, interval))
                .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
        }

        return Collections.emptyMap();
    }

    private Arbeidsforhold mapArbeidsforholdTilDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, Interval intervall) {
        Arbeidsforhold.Builder builder = new Arbeidsforhold.Builder()
            .medType(arbeidsforhold.getArbeidsforholdstype().getKodeRef())
            .medArbeidsforholdId(arbeidsforhold.getArbeidsforholdID());

        utledArbeidsgiver(arbeidsforhold, builder);

        AnsettelsesPeriode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode();
        builder.medArbeidFom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getFom()));
        if (ansettelsesPeriode.getPeriode().getTom() != null) {
            builder.medArbeidTom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getTom()));
        }

        builder.medArbeidsavtaler(hentHistoriskeArbeidsAvtaler(arbeidsforhold, intervall));
        builder.medAnsettelsesPeriode(byggAnsettelsesPeriode(arbeidsforhold));

        builder.medPermisjon(arbeidsforhold.getPermisjonOgPermittering().stream()
            .map(this::byggPermisjonDto)
            .collect(Collectors.toList()));

        return builder.build();
    }

    private void utledArbeidsgiver(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, Arbeidsforhold.Builder builder) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            Person arbeidsgiver = (Person) arbeidsforhold.getArbeidsgiver();
            no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Person person = new no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Person.Builder()
                .medAktørId(hentAktørIdForIdent(arbeidsgiver).orElseThrow(() -> new IllegalStateException("Får inn inntekt fra ")))
                .build();
            builder.medArbeidsgiver(person);

        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            Organisasjon arbeidsgiver = (Organisasjon) arbeidsforhold.getArbeidsgiver();
            no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon organisasjon = new no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon.Builder()
                .medOrgNummer(arbeidsgiver.getOrgnummer())
                .build();
            builder.medArbeidsgiver(organisasjon);
        }
    }

    private Arbeidsavtale byggAnsettelsesPeriode(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder();

        final AnsettelsesPeriode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode();
        builder.medArbeidsavtaleFom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getFom()));
        if (ansettelsesPeriode.getPeriode().getTom() != null) {
            builder.medArbeidsavtaleTom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getTom()));
        }
        builder.erAnsettelsesPerioden();
        return builder.build();
    }

    private Optional<AktørId> hentAktørIdForIdent(Person arbeidsgiver) {
        return tpsTjeneste.hentAktørForFnr(PersonIdent.fra(arbeidsgiver.getIdent().getIdent()));
    }

    private List<Arbeidsavtale> hentHistoriskeArbeidsAvtaler(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold,
                                                             Interval intervall) {
        final HentArbeidsforholdHistorikkRequest request = new HentArbeidsforholdHistorikkRequest();
        request.setArbeidsforholdId(arbeidsforhold.getArbeidsforholdIDnav());
        try {
            final HentArbeidsforholdHistorikkResponse response = arbeidsforholdConsumer.hentArbeidsforholdHistorikk(request);
            if (response.getArbeidsforhold() == null) {
                return Collections.emptyList();
            }
            return response.getArbeidsforhold().getArbeidsavtale().stream().map(aa -> byggArbeidsavtaleDto(aa, arbeidsforhold))
                .filter(av -> overlapperMedIntervall(av, intervall))
                .collect(Collectors.toList());
        } catch (HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (HentArbeidsforholdHistorikkSikkerhetsbegrensning e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        }
    }

    private boolean overlapperMedIntervall(Arbeidsavtale av, Interval interval) {
        final Interval interval1 = byggIntervall(av.getArbeidsavtaleFom(), av.getArbeidsavtaleTom() != null ? av.getArbeidsavtaleTom() : Tid.TIDENES_ENDE);
        return interval.overlaps(interval1);
    }

    private Arbeidsavtale byggArbeidsavtaleDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale arbeidsavtale,
                                               no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder()
            .medStillingsprosent(arbeidsavtale.getStillingsprosent())
            .medBeregnetAntallTimerPrUke(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medAvtaltArbeidstimerPerUke(arbeidsavtale.getAvtaltArbeidstimerPerUke())
            .medSisteLønnsendringsdato(DateUtil.convertToLocalDate(arbeidsavtale.getSisteLoennsendringsdato()));

        Gyldighetsperiode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        LocalDate arbeidsavtaleFom = DateUtil.convertToLocalDate(arbeidsavtale.getFomGyldighetsperiode());
        LocalDate arbeidsavtaleTom = DateUtil.convertToLocalDate(arbeidsavtale.getTomGyldighetsperiode());
        builder.medArbeidsavtaleFom(arbeidsavtaleFom);
        builder.medArbeidsavtaleTom(arbeidsavtaleTom);

        Interval ansettelsesIntervall = byggIntervall(
            DateUtil.convertToLocalDate(ansettelsesPeriode.getFom()),
            DateUtil.convertToLocalDate(ansettelsesPeriode.getTom()));

        if (!ansettelsesIntervall.contains(arbeidsavtaleFom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) {
            LOGGER.info("Arbeidsavtale fom={} ligger utenfor ansettelsesPeriode={}", arbeidsavtaleFom, ansettelsesIntervall);
        }
        return builder.build();
    }

    private Permisjon byggPermisjonDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.PermisjonOgPermittering permisjonOgPermittering) {
        return new Permisjon.Builder()
            .medPermisjonFom(DateUtil.convertToLocalDate(permisjonOgPermittering.getPermisjonsPeriode().getFom()))
            .medPermisjonTom(DateUtil.convertToLocalDate(permisjonOgPermittering.getPermisjonsPeriode().getTom()))
            .medPermisjonsprosent(permisjonOgPermittering.getPermisjonsprosent())
            .medPermisjonsÅrsak(permisjonOgPermittering.getPermisjonOgPermittering().getValue())
            .build();
    }

}
