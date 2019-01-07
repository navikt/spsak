package no.nav.foreldrepenger.domene.medlem.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapKildeType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderingsÅrsak;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class UtledVurderingsdatoerForMedlemskapTjenesteImpl implements UtledVurderingsdatoerForMedlemskapTjeneste {

    private GrunnlagRepositoryProvider provider;
    private MedlemEndringssjekkerProvider endringssjekkerProvider;
    private MedlemskapRepository medlemskapRepository;
    private MedlemEndringssjekker endringssjekker;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    @Inject
    public UtledVurderingsdatoerForMedlemskapTjenesteImpl(GrunnlagRepositoryProvider provider,
                                                          MedlemEndringssjekkerProvider endringssjekkerProvider,
                                                          PersonopplysningTjeneste personopplysningTjeneste,
                                                          SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.provider = provider;
        this.endringssjekkerProvider = endringssjekkerProvider;
        this.medlemskapRepository = provider.getMedlemskapRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    }

    UtledVurderingsdatoerForMedlemskapTjenesteImpl() {
        //CDI
    }

    @Override
    public Set<LocalDate> finnVurderingsdatoer(Long behandlingId) {
        Behandling revurdering = provider.getBehandlingRepository().hentBehandling(behandlingId);
        endringssjekker = endringssjekkerProvider.getEndringssjekker(revurdering);
        Set<LocalDate> datoer = new HashSet<>();

        datoer.addAll(utledVurderingsdatoerForTPS(revurdering).keySet());
        datoer.addAll(utledVurderingsdatoerForBortfallAvInntekt(revurdering).keySet());
        datoer.addAll(utledVurderingsdatoerForMedlemskap(revurdering).keySet());
        return datoer;
    }


    @Override
    public Map<LocalDate, Set<VurderingsÅrsak>> finnVurderingsdatoerMedÅrsak(Long behandlingId) {
        Behandling revurdering = provider.getBehandlingRepository().hentBehandling(behandlingId);
        endringssjekker = endringssjekkerProvider.getEndringssjekker(revurdering);
        Map<LocalDate, Set<VurderingsÅrsak>> datoer = new HashMap<>();

        datoer.putAll(utledVurderingsdatoerForTPS(revurdering));
        datoer.putAll(utledVurderingsdatoerForBortfallAvInntekt(revurdering));
        datoer.putAll(utledVurderingsdatoerForMedlemskap(revurdering));
        return datoer;
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> utledVurderingsdatoerForTPS(Behandling revurdering) {
        final Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat = new HashMap<>();
        LocalDate localDate = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(revurdering);
        DatoIntervallEntitet relevantPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(localDate, localDate.plusYears(3L));

        Optional<PersonopplysningerAggregat> personopplysningerOpt = personopplysningTjeneste.hentGjeldendePersoninformasjonForPeriodeHvisEksisterer(revurdering, relevantPeriode);
        if (personopplysningerOpt.isPresent()) {
            PersonopplysningerAggregat personopplysningerAggregat = personopplysningerOpt.get();

            utledetResultat.putAll(hentEndringForStatsborgerskap(personopplysningerAggregat, revurdering));
            mergeResultat(utledetResultat, hentEndringForPersonstatus(personopplysningerAggregat, revurdering));
            mergeResultat(utledetResultat, hentEndringForAdresse(personopplysningerAggregat, revurdering));
        }
        return utledetResultat;
    }

    private void mergeResultat(Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat, Map<LocalDate, Set<VurderingsÅrsak>> nyeEndringer) {
        for (Map.Entry<LocalDate, Set<VurderingsÅrsak>> localDateSetEntry : nyeEndringer.entrySet()) {
            utledetResultat.merge(localDateSetEntry.getKey(), localDateSetEntry.getValue(), this::slåSammenSet);
        }
    }

    private Set<VurderingsÅrsak> slåSammenSet(Set<VurderingsÅrsak> value1, Set<VurderingsÅrsak> value2) {
        Set<VurderingsÅrsak> vurderingsÅrsaks = new HashSet<>(value1);
        vurderingsÅrsaks.addAll(value2);
        return vurderingsÅrsaks;
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> utledVurderingsdatoerForBortfallAvInntekt(Behandling revurdering) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(revurdering);
        InntektArbeidYtelseGrunnlag førsteVersjonInntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentFørsteVersjon(revurdering, skjæringstidspunkt);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseRepository.hentAggregat(revurdering, skjæringstidspunkt);

        LocalDate opplysningsPeriode = LocalDate.now().isBefore(skjæringstidspunkt) ? LocalDate.now() : skjæringstidspunkt;
        Interval periode = IntervallUtil.byggIntervall(opplysningsPeriode.minusMonths(3), opplysningsPeriode);

        AktørId søker = revurdering.getAktørId();
        LocalDateTimeline<BigDecimal> førsteVersjon = lagTidsserieFor(førsteVersjonInntektArbeidYtelseGrunnlag, periode, søker);
        LocalDateTimeline<BigDecimal> sisteVersjon = lagTidsserieFor(inntektArbeidYtelseGrunnlag, periode, søker);

        Set<LocalDate> resultat = førsteVersjon.combine(sisteVersjon, this::sjekkForBortfallAvInntekt, LocalDateTimeline.JoinStyle.CROSS_JOIN)
            .getDatoIntervaller().stream().map(LocalDateInterval::getFomDato).collect(Collectors.toSet());

        final Map<LocalDate, Set<VurderingsÅrsak>> map = new HashMap<>();
        for (LocalDate localDate : resultat) {
            map.put(localDate, Set.of(VurderingsÅrsak.BORTFALL_INNTEKT));
        }
        return map;
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> utledVurderingsdatoerForMedlemskap(Behandling revurdering) {
        Optional<MedlemskapAggregat> førsteVersjon = medlemskapRepository.hentFørsteVersjonAvMedlemskap(revurdering);
        Optional<MedlemskapAggregat> sisteVersjon = medlemskapRepository.hentMedlemskap(revurdering);

        Set<RegistrertMedlemskapPerioder> første = førsteVersjon.map(MedlemskapAggregat::getRegistrertMedlemskapPerioder).orElse(Collections.emptySet());
        Set<RegistrertMedlemskapPerioder> siste = sisteVersjon.map(MedlemskapAggregat::getRegistrertMedlemskapPerioder).orElse(Collections.emptySet());

        List<LocalDateSegment<RegistrertMedlemskapPerioder>> førsteListe = første.stream().map(r -> new LocalDateSegment<>(r.getFom(), r.getTom(), r)).collect(Collectors.toList());
        List<LocalDateSegment<RegistrertMedlemskapPerioder>> sisteListe = siste.stream().map(r -> new LocalDateSegment<>(r.getFom(), r.getTom(), r)).collect(Collectors.toList());

        LocalDateTimeline<RegistrertMedlemskapPerioder> førsteTidsserie = new LocalDateTimeline<>(førsteListe, this::slåSammenMedlemskapPerioder);
        LocalDateTimeline<RegistrertMedlemskapPerioder> andreTidsserie = new LocalDateTimeline<>(sisteListe, this::slåSammenMedlemskapPerioder);

        LocalDateTimeline<RegistrertMedlemskapPerioder> resultat = førsteTidsserie.combine(andreTidsserie, this::sjekkForEndringIMedl, LocalDateTimeline.JoinStyle.CROSS_JOIN);

        return utledResultat(resultat);
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> utledResultat(LocalDateTimeline<RegistrertMedlemskapPerioder> resultat) {
        final Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat = new HashMap<>();
        NavigableSet<LocalDateInterval> datoIntervaller = resultat.getDatoIntervaller();
        for (LocalDateInterval localDateInterval : datoIntervaller) {
            LocalDateSegment<RegistrertMedlemskapPerioder> perioden = resultat.getSegment(localDateInterval);

            utledetResultat.put(perioden.getFom(), Set.of(VurderingsÅrsak.MEDL_PERIODE));
            // må sjekke både tom og fom ved overlapp! null betyr overlapp..
            if (perioden.getValue().getKildeType() == null) {
                utledetResultat.put(perioden.getTom(), Set.of(VurderingsÅrsak.MEDL_PERIODE));
            }
        }
        return utledetResultat;
    }

    private LocalDateTimeline<BigDecimal> lagTidsserieFor(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, Interval periode, AktørId aktørId) {
        List<LocalDateSegment<BigDecimal>> collect = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(aktørId)
            .map(AktørInntekt::getInntektPensjonsgivende).orElse(Collections.emptyList())
            .stream()
            .map(Inntekt::getInntektspost)
            .flatMap(Collection::stream)
            .filter(inntekt -> periode.overlaps(IntervallUtil.byggIntervall(inntekt.getFraOgMed(), inntekt.getTilOgMed())))
            .sorted(Comparator.comparing(Inntektspost::getFraOgMed))
            .map(inntekt -> new LocalDateSegment<>(inntekt.getFraOgMed(), inntekt.getTilOgMed().minusDays(1), inntekt.getBeløp().getVerdi()))
            .collect(Collectors.toList());

        return new LocalDateTimeline<>(collect, StandardCombinators::sum);
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> hentEndringForAdresse(PersonopplysningerAggregat personopplysningerAggregat, Behandling revurdering) {
        List<PersonAdresse> adresser = personopplysningerAggregat.getAdresserFor(revurdering.getAktørId())
            .stream().sorted(Comparator.comparing(s -> s.getPeriode().getFomDato()))
            .collect(Collectors.toList());
        final Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat = new HashMap<>();
        IntStream.range(0, adresser.size() - 1).forEach(i -> {
            if (i != adresser.size() - 1) { // sjekker om det er siste element
                PersonAdresse førsteElement = adresser.get(i);
                PersonAdresse nesteElement = adresser.get(i + 1);
                if (!førsteElement.getAdresseType().equals(nesteElement.getAdresseType())) {
                    utledetResultat.put(nesteElement.getPeriode().getFomDato(), Set.of(VurderingsÅrsak.ADRESSE));
                }
            }
        });
        return utledetResultat;
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> hentEndringForPersonstatus(PersonopplysningerAggregat personopplysningerAggregat, Behandling revurdering) {
        List<Personstatus> personstatus = personopplysningerAggregat.getPersonstatuserFor(revurdering.getAktørId())
            .stream().sorted(Comparator.comparing(s -> s.getPeriode().getFomDato()))
            .collect(Collectors.toList());
        final Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat = new HashMap<>();
        IntStream.range(0, personstatus.size() - 1).forEach(i -> {
            if (i != personstatus.size() - 1) { // sjekker om det er siste element
                Personstatus førsteElement = personstatus.get(i);
                Personstatus nesteElement = personstatus.get(i + 1);
                if (!førsteElement.getPersonstatus().equals(nesteElement.getPersonstatus())) {
                    utledetResultat.put(nesteElement.getPeriode().getFomDato(), Set.of(VurderingsÅrsak.PERSONSTATUS));
                }
            }
        });
        return utledetResultat;
    }

    private Map<LocalDate, Set<VurderingsÅrsak>> hentEndringForStatsborgerskap(PersonopplysningerAggregat personopplysningerAggregat, Behandling revurdering) {
        List<Statsborgerskap> statsborgerskap = personopplysningerAggregat.getStatsborgerskapFor(revurdering.getAktørId())
            .stream().sorted(Comparator.comparing(s -> s.getPeriode().getFomDato()))
            .collect(Collectors.toList());
        final Map<LocalDate, Set<VurderingsÅrsak>> utledetResultat = new HashMap<>();
        IntStream.range(0, statsborgerskap.size() - 1).forEach(i -> {
            if (i != statsborgerskap.size() - 1) { // sjekker om det er siste element
                Statsborgerskap førsteElement = statsborgerskap.get(i);
                Statsborgerskap nesteElement = statsborgerskap.get(i + 1);
                if (!førsteElement.getStatsborgerskap().equals(nesteElement.getStatsborgerskap())) {
                    utledetResultat.put(nesteElement.getPeriode().getFomDato(), Set.of(VurderingsÅrsak.STATSBORGERSKAP));
                }
            }
        });
        return utledetResultat;
    }

    private LocalDateSegment<RegistrertMedlemskapPerioder> sjekkForEndringIMedl(LocalDateInterval di,
                                                                                LocalDateSegment<RegistrertMedlemskapPerioder> førsteVersjon,
                                                                                LocalDateSegment<RegistrertMedlemskapPerioder> sisteVersjon) {

        // må alltid sjekke datoer med overlapp
        if (førsteVersjon != null && førsteVersjon.getValue().getKildeType() == null) {
            return førsteVersjon;
        }

        // må alltid sjekke datoer med overlapp
        if (sisteVersjon != null && sisteVersjon.getValue().getKildeType() == null) {
            return sisteVersjon;
        }

        // ny periode registrert
        if (førsteVersjon == null) {
            return sisteVersjon;
        }
        if (sisteVersjon != null) {
            // sjekker om gammel periode har endret verdier
            if (endringssjekker.erEndring(førsteVersjon.getValue(), sisteVersjon.getValue())) {
                return sisteVersjon;
            } else {
                return null;
            }
        }
        // gammel periode fjernet
        return førsteVersjon;
    }

    // Ligger her som en guard mot dårlig datakvalitet i medl.. Skal nemlig aldri inntreffe
    private LocalDateSegment<RegistrertMedlemskapPerioder> slåSammenMedlemskapPerioder(LocalDateInterval di,
                                                                                       LocalDateSegment<RegistrertMedlemskapPerioder> førsteVersjon,
                                                                                       LocalDateSegment<RegistrertMedlemskapPerioder> sisteVersjon) {
        if (førsteVersjon == null) {
            return sisteVersjon;
        } else if (sisteVersjon == null) {
            return førsteVersjon;
        }

        MedlemskapPerioderBuilder builder = new MedlemskapPerioderBuilder(førsteVersjon.getValue());
        builder.medPeriode(di.getFomDato(), di.getTomDato());
        builder.medKildeType(MedlemskapKildeType.UDEFINERT);

        return new LocalDateSegment<>(di.getFomDato(), di.getTomDato(), builder.build());
    }

    private LocalDateSegment<BigDecimal> sjekkForBortfallAvInntekt(LocalDateInterval di,
                                                                   LocalDateSegment<BigDecimal> førsteVersjon,
                                                                   LocalDateSegment<BigDecimal> sisteVersjon) {
        // har fått inntekt
        if (førsteVersjon == null) {
            return null;
        }
        // inntekt blitt justert til 0
        if (!BigDecimal.ZERO.equals(førsteVersjon.getValue()) && sisteVersjon != null && BigDecimal.ZERO.equals(sisteVersjon.getValue())) {
            return sisteVersjon;
        }

        // inntekt falt helt ut
        if (!BigDecimal.ZERO.equals(førsteVersjon.getValue()) && sisteVersjon == null) {
            return førsteVersjon;
        }
        return null;
    }
}
