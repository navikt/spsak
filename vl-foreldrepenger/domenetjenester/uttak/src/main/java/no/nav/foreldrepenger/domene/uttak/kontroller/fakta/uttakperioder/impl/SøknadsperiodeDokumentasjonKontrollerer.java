package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType.FEDREKVOTE;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType.FELLESPERIODE;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak.ARBEID;
import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak.FERIE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidPåHeltidTjeneste;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaData;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.domene.uttak.perioder.PerioderUtenHelgUtil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.jpa.tid.AbstractLocalDateInterval;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.IntervalUtils;

class SøknadsperiodeDokumentasjonKontrollerer {
    private final List<PeriodeUttakDokumentasjon> dokumentasjonPerioder;
    private final List<Inntektsmelding> inntektsmeldinger;
    private final ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste;

    private final LocalDate fødselsDatoTilTidligOppstart;
    private final boolean erArbeidstaker;

    SøknadsperiodeDokumentasjonKontrollerer(List<PeriodeUttakDokumentasjon> dokumentasjonPerioder,
                                            List<Inntektsmelding> inntektsmeldinger,
                                            ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste,
                                            LocalDate fødselsDatoTilTidligOppstart,
                                            boolean erArbeidstaker) {
        this.dokumentasjonPerioder = dokumentasjonPerioder;
        this.inntektsmeldinger = inntektsmeldinger;
        this.arbeidPåHeltidTjeneste = arbeidPåHeltidTjeneste;
        this.fødselsDatoTilTidligOppstart = fødselsDatoTilTidligOppstart;
        this.erArbeidstaker = erArbeidstaker;
    }

    static KontrollerFaktaData kontrollerPerioder(YtelseFordelingAggregat ytelseFordeling,
                                                  List<Inntektsmelding> inntektsmeldinger,
                                                  ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste,
                                                  LocalDate fødselsDatoTilTidligOppstart,
                                                  boolean erArbeidstaker) {
        List<PeriodeUttakDokumentasjon> dokumentasjonPerioder = hentDokumentasjonPerioder(ytelseFordeling);

        SøknadsperiodeDokumentasjonKontrollerer kontrollerer = new SøknadsperiodeDokumentasjonKontrollerer(dokumentasjonPerioder,
            inntektsmeldinger, arbeidPåHeltidTjeneste, fødselsDatoTilTidligOppstart, erArbeidstaker);
        return kontrollerer.kontrollerSøknadsperioder(ytelseFordeling.getGjeldendeSøknadsperioder().getOppgittePerioder());
    }

    private static List<PeriodeUttakDokumentasjon> hentDokumentasjonPerioder(YtelseFordelingAggregat ytelseFordeling) {
        return ytelseFordeling.getPerioderUttakDokumentasjon()
            .map(PerioderUttakDokumentasjon::getPerioder).orElse(Collections.emptyList());
    }

    private KontrollerFaktaData kontrollerSøknadsperioder(List<OppgittPeriode> søknadsperioder) {
        List<KontrollerFaktaPeriode> perioder = søknadsperioder.stream()
            .map(this::kontrollerSøknadsperiode)
            .collect(toList());

        return new KontrollerFaktaData(perioder);
    }

    KontrollerFaktaPeriode kontrollerSøknadsperiode(OppgittPeriode søknadsperiode) {
        List<PeriodeUttakDokumentasjon> eksisterendeDokumentasjon = finnDokumentasjon(søknadsperiode.getFom(), søknadsperiode.getTom());

        if (erPeriodenAvklartAvSaksbehandler(søknadsperiode)) {
            return KontrollerFaktaPeriode.manueltAvklart(søknadsperiode, eksisterendeDokumentasjon);
        }

        return kontrollerUavklartPeriode(søknadsperiode, eksisterendeDokumentasjon);
    }

    private KontrollerFaktaPeriode kontrollerUavklartPeriode(OppgittPeriode søknadsperiode,
                                                             List<PeriodeUttakDokumentasjon> eksisterendeDokumentasjon) {
        if (!eksisterendeDokumentasjon.isEmpty()) {
            throw KontrollerFaktaUttakFeil.FACTORY.dokumentertUtenBegrunnelse().toException();
        }

        if (erUtsettelse(søknadsperiode)) {
            return kontrollerUtsettelse(søknadsperiode);
        }
        if (erOverføring(søknadsperiode)) {
            return kontrollerOverføring(søknadsperiode);
        }
        if (erGyldigGrunnForTidligOppstart(søknadsperiode)) {
            return KontrollerFaktaPeriode.ubekreftetTidligOppstart(søknadsperiode);
        }
        if (erGradering(søknadsperiode)) {
            return kontrollerGradering(søknadsperiode);
        }
        //Ikke søkt om noe spesielt, men inntektsmeldigene kan si noe annet
        return kontrollerSøknadUtenSpesiellePerioder(søknadsperiode);

    }

    private KontrollerFaktaPeriode kontrollerSøknadUtenSpesiellePerioder(OppgittPeriode søknadsperiode) {
        List<Gradering> graderingerIPeriode = hentAlleGraderingPerioderFraInntektsmeldingerISøknadsperiode(søknadsperiode);
        if (!graderingerIPeriode.isEmpty()) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }

        List<UtsettelsePeriode> utsettelserIPeriode = hentUtsettelsePeriodeFraInntektsmeldingISøknadsperiode(søknadsperiode);
        if (!utsettelserIPeriode.isEmpty()) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }
        return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
    }

    private boolean erPeriodenAvklartAvSaksbehandler(OppgittPeriode søknadsperiode) {
        return !PERIODE_IKKE_VURDERT.equals(søknadsperiode.getPeriodeVurderingType());
    }

    private List<PeriodeUttakDokumentasjon> finnDokumentasjon(LocalDate fom, LocalDate tom) {
        IntervalUtils søknadPeriode = new IntervalUtils(fom, tom);
        List<PeriodeUttakDokumentasjon> resultat = new ArrayList<>();

        for (PeriodeUttakDokumentasjon dokumentasjon : dokumentasjonPerioder) {
            DatoIntervallEntitet dokumentasjonPeriode = dokumentasjon.getPeriode();
            if (søknadPeriode.overlapper(dokumentasjonPeriode)) {
                resultat.add(dokumentasjon);
            }
        }
        return resultat;
    }

    private KontrollerFaktaPeriode kontrollerGradering(OppgittPeriode søknadsperiode) {
        if (!erArbeidstaker) {
            return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
        }

        validerSøknadsperiodeGraderingOrdinærtArbeidsforhold(søknadsperiode);

        List<Gradering> graderingerForArbeidsforhold = hentGraderingPerioderFraInntektsmeldingerForArbeidsforholdSomDekkerSøknadsperiode(søknadsperiode);
        List<Gradering> alleGraderingIPeriode = hentAlleGraderingPerioderFraInntektsmeldingerSomDekkerSøknadsperiode(søknadsperiode);

        if (graderingerForArbeidsforhold.size() != alleGraderingIPeriode.size()) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }

        BigDecimal arbeidstidsProsentFraSøknad = søknadsperiode.getArbeidsprosent();

        if (graderingerForArbeidsforhold.isEmpty()) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }

        for (Gradering gradering : graderingerForArbeidsforhold) {
            if (arbeidstidsProsentFraSøknad.compareTo(gradering.getArbeidstidProsent()) != 0) {
                return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
            }
        }

        return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
    }

    private void validerSøknadsperiodeGraderingOrdinærtArbeidsforhold(OppgittPeriode søknadsperiode) {
        if (søknadsperiode.getVirksomhet() == null && søknadsperiode.getErArbeidstaker()) {
            throw FeilFactory.create(KontrollerFaktaUttakFeil.class).søktGraderingUtenVirksomhet(søknadsperiode.getPeriodeType().getNavn(),
                søknadsperiode.getFom(), søknadsperiode.getTom()).toException();
        }
    }

    private boolean erGradering(OppgittPeriode søknadsperiode) {
        return søknadsperiode.getArbeidsprosent() != null && søknadsperiode.getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean erUtsettelse(OppgittPeriode søknadsperiode) {
        Årsak årsak = søknadsperiode.getÅrsak();
        return årsak instanceof UtsettelseÅrsak && !UtsettelseÅrsak.UDEFINERT.equals(årsak);
    }

    private boolean erOverføring(OppgittPeriode søknadsPeriode) {
        Årsak årsak = søknadsPeriode.getÅrsak();
        return årsak instanceof OverføringÅrsak && !OverføringÅrsak.UDEFINERT.equals(årsak);
    }

    private KontrollerFaktaPeriode kontrollerOverføring(OppgittPeriode søknadsperiode) {
        if (OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE.equals(søknadsperiode.getÅrsak()) ||
            OverføringÅrsak.SYKDOM_ANNEN_FORELDER.equals(søknadsperiode.getÅrsak())) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }
        return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
    }

    private boolean erGyldigGrunnForTidligOppstart(OppgittPeriode søknadsperiode) {
        // Søker far/medmor uttak av Fellesperiode eller fedrekvote før uke 7 ved fødsel?
        if (fødselsDatoTilTidligOppstart != null && søknadsperiode.getFom().isBefore(fødselsDatoTilTidligOppstart.plusWeeks(6L))) {
            return FELLESPERIODE.equals(søknadsperiode.getPeriodeType()) || FEDREKVOTE.equals(søknadsperiode.getPeriodeType());
        }
        return false;
    }

    private KontrollerFaktaPeriode kontrollerUtsettelse(OppgittPeriode søknadsperiode) {
        Årsak utsettelseÅrsak = søknadsperiode.getÅrsak();
        if (ARBEID.equals(utsettelseÅrsak)) {
            return kontrollerUtsettelseArbeid(søknadsperiode);
        }
        if (FERIE.equals(utsettelseÅrsak)) {
            return kontrollerUtsettelseFerie(søknadsperiode);
        }
        return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
    }

    private KontrollerFaktaPeriode kontrollerUtsettelseFerie(OppgittPeriode søknadsperiode) {
        if (!erArbeidstaker || utsettelsePeriodeFinnesPåInntektsmelding(søknadsperiode, UtsettelseÅrsak.FERIE)) {
            return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
        }
        return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
    }

    private KontrollerFaktaPeriode kontrollerUtsettelseArbeid(OppgittPeriode søknadsperiode) {
        if (!erArbeidstaker) {
            return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
        }

        if (arbeidPåHeltidTjeneste.jobberFulltid(søknadsperiode) && !utsettelsePeriodeFinnesPåInntektsmelding(søknadsperiode, UtsettelseÅrsak.ARBEID)) {
            return KontrollerFaktaPeriode.ubekreftet(søknadsperiode);
        }
        return KontrollerFaktaPeriode.automatiskBekreftet(søknadsperiode);
    }

    /**
     * Liste pga at man kan ha flere graderte arbeidsforhold i perioden så lenge det er samme arbeidsgiver
     */
    private List<Gradering> hentGraderingPerioderFraInntektsmeldingerForArbeidsforholdSomDekkerSøknadsperiode(OppgittPeriode søknadsperiode) {
        List<Inntektsmelding> aktuelleInntektsmeldinger = hentInntektsmeldingerForArbeidsforhold(søknadsperiode.getVirksomhet());
        return aktuelleInntektsmeldinger.stream().flatMap(inntektsmelding -> inntektsmelding.getGraderinger()
            .stream())
            .filter(g -> periodeOmslutterSøknadsperiode(g.getPeriode(), søknadsperiode))
            .collect(Collectors.toList());
    }

    /**
     * Liste pga at man kan ha flere graderte arbeidsforhold i perioden så lenge det er samme arbeidsgiver
     */
    private List<Gradering> hentAlleGraderingPerioderFraInntektsmeldingerSomDekkerSøknadsperiode(OppgittPeriode søknadsperiode) {
        return inntektsmeldinger.stream().flatMap(inntektsmelding -> inntektsmelding.getGraderinger()
            .stream())
            .filter(g -> periodeOmslutterSøknadsperiode(g.getPeriode(), søknadsperiode))
            .collect(Collectors.toList());
    }

    private List<Inntektsmelding> hentInntektsmeldingerForArbeidsforhold(Virksomhet virksomhet) {
        if (virksomhet == null) {
            return Collections.emptyList();
        }
        List<Inntektsmelding> inntektsmeldingerIPeriode = new ArrayList<>();
        for (Inntektsmelding inntektsmelding : this.inntektsmeldinger) {
            if (Objects.equals(inntektsmelding.getVirksomhet().getOrgnr(), virksomhet.getOrgnr())) {
                inntektsmeldingerIPeriode.add(inntektsmelding);
            }
        }
        return inntektsmeldingerIPeriode;
    }

    private boolean utsettelsePeriodeFinnesPåInntektsmelding(OppgittPeriode søknadsperiode, UtsettelseÅrsak årsak) {
        if (inntektsmeldinger.isEmpty()) {
            return false;
        }

        for (Inntektsmelding inntektsmelding : inntektsmeldinger) {
            Optional<UtsettelsePeriode> utsettelsePeriode = hentUtsettelsePeriodeFraInntektsmeldingSomDekkerSøknadsperiode(søknadsperiode, inntektsmelding, årsak);
            if (!utsettelsePeriode.isPresent()) {
                return false;
            }
        }
        return true;
    }

    private Optional<UtsettelsePeriode> hentUtsettelsePeriodeFraInntektsmeldingSomDekkerSøknadsperiode(OppgittPeriode søknadsperiode,
                                                                                                       Inntektsmelding inntektsmelding,
                                                                                                       UtsettelseÅrsak årsak) {
        return inntektsmelding.getUtsettelsePerioder().stream()
            .filter(u -> periodeOmslutterSøknadsperiode(u.getPeriode(), søknadsperiode))
            .filter(u -> Objects.equals(årsak, u.getÅrsak()))
            .findFirst();
    }

    private boolean periodeOmslutterSøknadsperiode(AbstractLocalDateInterval periode, OppgittPeriode søknadsperiode) {
        return PerioderUtenHelgUtil.periodeUtenHelgOmslutter(periode.getFomDato(), periode.getTomDato(), søknadsperiode.getFom(), søknadsperiode.getTom());
    }

    private List<UtsettelsePeriode> hentUtsettelsePeriodeFraInntektsmeldingISøknadsperiode(OppgittPeriode søknadsperiode) {
        return inntektsmeldinger.stream()
            .flatMap(inntektsmelding -> inntektsmelding.getUtsettelsePerioder().stream())
            .filter(utsettelse -> utsettelse.getPeriode().overlapper(DatoIntervallEntitet.fraOgMedTilOgMed(søknadsperiode.getFom(), søknadsperiode.getTom())))
            .collect(Collectors.toList());
    }

    private List<Gradering> hentAlleGraderingPerioderFraInntektsmeldingerISøknadsperiode(OppgittPeriode søknadsperiode) {
        return inntektsmeldinger.stream()
            .flatMap(inntektsmelding -> inntektsmelding.getGraderinger().stream())
            .filter(gradering -> gradering.getPeriode().overlapper(DatoIntervallEntitet.fraOgMedTilOgMed(søknadsperiode.getFom(), søknadsperiode.getTom())))
            .collect(Collectors.toList());
    }
}
