package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.ManuellBehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatDokRegelEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeSøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelResultatKonverterer;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.uttaksvilkår.FastsettePeriodeResultat;

@ApplicationScoped
public class FastsettePerioderRegelResultatKonvertererImpl implements FastsettePerioderRegelResultatKonverterer {

    private BehandlingRepositoryProvider repositoryProvider;

    FastsettePerioderRegelResultatKonvertererImpl() {
        // For CDI
    }

    @Inject
    public FastsettePerioderRegelResultatKonvertererImpl(BehandlingRepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public UttakResultatPerioderEntitet konverter(Behandling behandling,
                                                  List<AktivitetIdentifikator> aktiviteter,
                                                  List<FastsettePeriodeResultat> resultat) {
        OppgittFordeling oppgittFordeling = hentOppgittFordeling(behandling);
        LocalDate søknadMottattDato = hentSøknadMottattDato(behandling);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();

        List<PeriodeSøknad> periodeSøknader = lagPeriodeSøknader(oppgittFordeling, søknadMottattDato);
        List<UttakAktivitetEntitet> uttakAktiviteter = lagUttakAktiviteter(aktiviteter);
        List<FastsettePeriodeResultat> resultatSomSkalKonverteres = resultat.stream()
            .filter(periodeResultat -> !erOppholdperiodeForAnnenPart(periodeResultat))
            .sorted(Comparator.comparing(periodeRes -> periodeRes.getUttakPeriode().getFom()))
            .collect(Collectors.toList());

        for (FastsettePeriodeResultat fastsettePeriodeResultat : resultatSomSkalKonverteres) {
            UttakResultatPeriodeEntitet periode = lagUttakResultatPeriode(
                fastsettePeriodeResultat,
                periodeSomHarUtledetResultat(fastsettePeriodeResultat, periodeSøknader),
                uttakAktiviteter,
                aktiviteter
            );
            perioder.leggTilPeriode(periode);
        }
        if (behandling.erRevurdering()) {
            prependPerioderFraOriginalBehandling(behandling, perioder);
        }
        return perioder;
    }

    private void prependPerioderFraOriginalBehandling(Behandling behandling, UttakResultatPerioderEntitet perioder) {
        Behandling originalBehandling = FastsettePerioderRevurderingUtil.finnOriginalBehandling(behandling);
        Optional<UttakResultatEntitet> opprinneligUttak = repositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(originalBehandling);
        if (opprinneligUttak.isPresent()) {
            LocalDate endringsdato = FastsettePerioderRevurderingUtil.finnEndringsdatoRevurdering(behandling, repositoryProvider.getYtelsesFordelingRepository());
            List<UttakResultatPeriodeEntitet> perioderFørEndringsdato = FastsettePerioderRevurderingUtil.perioderFørEndringsdato(opprinneligUttak.get(), endringsdato);
            prependPerioder(perioderFørEndringsdato, perioder);
        }
    }

    private void prependPerioder(List<UttakResultatPeriodeEntitet> perioderFørEndringsdato, UttakResultatPerioderEntitet perioder) {
        for (UttakResultatPeriodeEntitet periodeFørEndringsdato : perioderFørEndringsdato) {
            perioder.leggTilPeriode(periodeFørEndringsdato);
        }
    }

    private boolean erOppholdperiodeForAnnenPart(FastsettePeriodeResultat periodeResultat) {
        UttakPeriode uttakPeriode = periodeResultat.getUttakPeriode();
        if (uttakPeriode instanceof OppholdPeriode) {
            OppholdPeriode oppholdPeriode = (OppholdPeriode) uttakPeriode;
            Oppholdårsaktype oppholdårsaktype = oppholdPeriode.getOppholdårsaktype();
            return Oppholdårsaktype.KVOTE_FELLESPERIODE_ANNEN_FORELDER.equals(oppholdårsaktype) || Oppholdårsaktype.KVOTE_ANNEN_FORELDER.equals(oppholdårsaktype);
        }
        return false;
    }

    private List<PeriodeSøknad> lagPeriodeSøknader(OppgittFordeling oppgittFordeling,
                                                   LocalDate søknadMottattDato) {
        return oppgittFordeling.getOppgittePerioder().stream()
            .map(oppgittPeriode -> lagPeriodeSøknad(oppgittPeriode, søknadMottattDato))
            .collect(Collectors.toList());
    }

    private List<UttakAktivitetEntitet> lagUttakAktiviteter(List<AktivitetIdentifikator> aktiviteter) {
        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        return aktiviteter.stream()
            .map(akt -> lagUttakAktivitet(akt, virksomhetRepository))
            .collect(Collectors.toList());
    }

    private UttakAktivitetEntitet riktigUttakAktivitet(AktivitetIdentifikator aktivitet,
                                                       List<UttakAktivitetEntitet> uttakAktiviteter) {
        return uttakAktiviteter.stream()
            .filter(uttakAktivitet -> Objects.equals(lagArbeidType(aktivitet), uttakAktivitet.getUttakArbeidType()) &&
                Objects.equals(aktivitet.getArbeidsforholdId(), uttakAktivitet.getArbeidsforholdId()) &&
                Objects.equals(aktivitet.getOrgNr(), uttakAktivitet.getArbeidsforholdOrgnr()))
            .findFirst().orElse(null);
    }

    private UttakResultatPeriodeEntitet lagUttakResultatPeriode(FastsettePeriodeResultat resultat,
                                                                UttakResultatPeriodeSøknadEntitet periodeSøknad,
                                                                List<UttakAktivitetEntitet> uttakAktiviteter,
                                                                List<AktivitetIdentifikator> aktiviteter) {
        UttakPeriode uttakPeriode = resultat.getUttakPeriode();

        UttakResultatDokRegelEntitet dokRegel = lagDokRegel(resultat);
        UttakResultatPeriodeEntitet periode = lagPeriode(uttakPeriode, dokRegel, periodeSøknad);

        aktiviteter.forEach(aktivitet -> {
            UttakResultatPeriodeAktivitetEntitet periodeAktivitet = lagPeriodeAktivitet(uttakAktiviteter, uttakPeriode, periode, aktivitet);
            periode.leggTilAktivitet(periodeAktivitet);
        });

        return periode;
    }

    private UttakResultatPeriodeAktivitetEntitet lagPeriodeAktivitet(List<UttakAktivitetEntitet> uttakAktiviteter,
                                                                     UttakPeriode uttakPeriode,
                                                                     UttakResultatPeriodeEntitet periode,
                                                                     AktivitetIdentifikator aktivitet) {
        UttakAktivitetEntitet uttakAktivitet = riktigUttakAktivitet(aktivitet, uttakAktiviteter);
        return UttakResultatPeriodeAktivitetEntitet.builder(periode, uttakAktivitet)
            .medTrekkonto(toStønadskontotype(uttakPeriode.getStønadskontotype()))
            .medTrekkdager(uttakPeriode.getTrekkdager(aktivitet))
            .medUtbetalingsprosent(uttakPeriode.getUtbetalingsgrad(aktivitet))
            .medArbeidsprosent(uttakPeriode.getProsentArbeid(aktivitet))
            .medErSøktGradering(erSøktOmGraderingForAktivitet(uttakPeriode, aktivitet))
            .build();
    }

    private boolean erSøktOmGraderingForAktivitet(UttakPeriode uttakPeriode, AktivitetIdentifikator aktivitet) {
        //Gradering på aktiviteten blir satt til null i regler, i tillegg til at en årsak blir satt
        return uttakPeriode.harGradering(aktivitet) || uttakPeriode.getGraderingIkkeInnvilgetÅrsak() != null;
    }

    private UttakResultatPeriodeSøknadEntitet periodeSomHarUtledetResultat(FastsettePeriodeResultat resultat,
                                                                           List<PeriodeSøknad> periodeSøknader) {

        return periodeSøknader.stream()
            .filter(søknad -> søknad.harUtledet(resultat.getUttakPeriode()))
            .map(søknad -> søknad.entitet)
            .findFirst().orElse(null);
    }


    private LocalDate hentSøknadMottattDato(Behandling behandling) {
        return repositoryProvider.getSøknadRepository().hentSøknad(behandling).getMottattDato();
    }

    private OppgittFordeling hentOppgittFordeling(Behandling behandling) {
        return repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling).getGjeldendeSøknadsperioder();
    }

    private UttakResultatDokRegelEntitet lagDokRegel(FastsettePeriodeResultat resultat) {
        ManuellBehandlingÅrsak manuellBehandlingÅrsak = tilKodeverk(resultat.getUttakPeriode().getManuellbehandlingårsak());
        UttakResultatDokRegelEntitet.Builder builder = resultat.isManuellBehandling() ?
            UttakResultatDokRegelEntitet.medManuellBehandling(manuellBehandlingÅrsak) :
            UttakResultatDokRegelEntitet.utenManuellBehandling();
        return builder
            .medRegelEvaluering(resultat.getEvalueringResultat())
            .medRegelInput(resultat.getInnsendtGrunnlag())
            .build();
    }

    private UttakAktivitetEntitet lagUttakAktivitet(AktivitetIdentifikator aktivitetIdentifikator, VirksomhetRepository virksomhetRepository) {
        UttakAktivitetEntitet.Builder builder = new UttakAktivitetEntitet.Builder();
        if (aktivitetIdentifikator.getOrgNr() != null) {
            Optional<Virksomhet> virksomhet = virksomhetRepository.hent(aktivitetIdentifikator.getOrgNr());
            if (virksomhet.isPresent()) {
                builder.medArbeidsforhold((VirksomhetEntitet) virksomhet.get(),
                    aktivitetIdentifikator.getArbeidsforholdId() == null ? null : ArbeidsforholdRef.ref(aktivitetIdentifikator.getArbeidsforholdId()));
            }
        }
        return builder
            .medUttakArbeidType(lagArbeidType(aktivitetIdentifikator))
            .build();
    }

    static UttakArbeidType lagArbeidType(AktivitetIdentifikator aktivitetIdentifikator) {
        AktivitetType aktivitetType = aktivitetIdentifikator.getAktivitetType();
        if (AktivitetType.ARBEID.equals(aktivitetType)) {
            return UttakArbeidType.ORDINÆRT_ARBEID;
        }
        if (AktivitetType.FRILANS.equals(aktivitetType)) {
            return UttakArbeidType.FRILANS;
        }
        if (AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(aktivitetType)) {
            return UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE;
        }
        return UttakArbeidType.ANNET;
    }

    private UttakResultatPeriodeEntitet lagPeriode(UttakPeriode uttakPeriode,
                                                   UttakResultatDokRegelEntitet dokRegel,
                                                   UttakResultatPeriodeSøknadEntitet periodeSøknad) {
        PeriodeResultatType periodeResultatType = toUttakPeriodeResultatType(uttakPeriode.getPerioderesultattype());
        // BIXBITE fjerne denne når vi ikke har den i periodeSøknad lenger
        BigDecimal samtidigUttaksprosent = null;
        if (periodeSøknad != null) {
            samtidigUttaksprosent = periodeSøknad.getSamtidigUttaksprosent();
        }

        return new UttakResultatPeriodeEntitet.Builder(uttakPeriode.getFom(), uttakPeriode.getTom())
            .medPeriodeResultat(periodeResultatType, tilKodeverk(periodeResultatType, uttakPeriode.getÅrsak()))
            .medDokRegel(dokRegel)
            .medGraderingInnvilget(uttakPeriode.harGradering())
            .medUtsettelseType(toUtsettelseType(uttakPeriode))
            .medGraderingAvslagÅrsak(tilKodeverk(uttakPeriode.getGraderingIkkeInnvilgetÅrsak()))
            .medPeriodeSoknad(periodeSøknad)
            .medSamtidigUttak(uttakPeriode.isSamtidigUttak())
            .medSamtidigUttaksprosent(samtidigUttaksprosent)
            .medFlerbarnsdager(uttakPeriode.isFlerbarnsdager())
            .build();
    }

    private UttakUtsettelseType toUtsettelseType(UttakPeriode uttakPeriode) {
        if (uttakPeriode instanceof UtsettelsePeriode) {
            UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
            return toUttakUtsettelseType(utsettelsePeriode.getUtsettelseårsaktype());
        }
        return UttakUtsettelseType.UDEFINERT;
    }

    private PeriodeSøknad lagPeriodeSøknad(OppgittPeriode oppgittPeriode, LocalDate søknadMottattDato) {
        UttakResultatPeriodeSøknadEntitet.Builder builder = new UttakResultatPeriodeSøknadEntitet.Builder()
            .medGraderingArbeidsprosent(oppgittPeriode.getArbeidsprosent())
            .medUttakPeriodeType(oppgittPeriode.getPeriodeType())
            .medMottattDato(søknadMottattDato)
            .medMorsAktivitet(oppgittPeriode.getMorsAktivitet())
            .medSamtidigUttak(oppgittPeriode.isSamtidigUttak())
            .medSamtidigUttaksprosent(oppgittPeriode.getSamtidigUttaksprosent());
        UttakResultatPeriodeSøknadEntitet entitet = builder.build();

        return new PeriodeSøknad(entitet, oppgittPeriode.getFom(), oppgittPeriode.getTom());
    }

    private static UttakUtsettelseType toUttakUtsettelseType(Utsettelseårsaktype utsettelseårsaktype) {
        switch (utsettelseårsaktype) {
            case ARBEID:
                return UttakUtsettelseType.ARBEID;
            case FERIE:
                return UttakUtsettelseType.FERIE;
            case INNLAGT_BARN:
                return UttakUtsettelseType.BARN_INNLAGT;
            case SYKDOM_SKADE:
                return UttakUtsettelseType.SYKDOM_SKADE;
            case INNLAGT_HELSEINSTITUSJON:
                return UttakUtsettelseType.SØKER_INNLAGT;
            default:
                throw new IllegalArgumentException("Utvikler-feil: Kom ut av regel med perioderesultattype " + utsettelseårsaktype);
        }
    }

    private static PeriodeResultatType toUttakPeriodeResultatType(Perioderesultattype perioderesultatType) {
        switch (perioderesultatType) {
            case INNVILGET:
                return PeriodeResultatType.INNVILGET;
            case AVSLÅTT:
                return PeriodeResultatType.AVSLÅTT;
            case MANUELL_BEHANDLING:
                return PeriodeResultatType.MANUELL_BEHANDLING;
            default:
                throw new IllegalArgumentException("Utvikler-feil: Kom ut av regel med perioderesultattype " + perioderesultatType);
        }
    }

    private static no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType toStønadskontotype(Stønadskontotype stønadskontotype) {
        switch (stønadskontotype) {
            case FEDREKVOTE:
                return StønadskontoType.FEDREKVOTE;
            case MØDREKVOTE:
                return StønadskontoType.MØDREKVOTE;
            case FELLESPERIODE:
                return StønadskontoType.FELLESPERIODE;
            case FORELDREPENGER:
                return StønadskontoType.FORELDREPENGER;
            case FORELDREPENGER_FØR_FØDSEL:
                return StønadskontoType.FORELDREPENGER_FØR_FØDSEL;
            case FLERBARNSDAGER:
                return StønadskontoType.FLERBARNSDAGER;
            case UKJENT:
                return StønadskontoType.UDEFINERT;
        }
        throw new IllegalArgumentException("Har ikke mapping for Stønadskontotype " + stønadskontotype);
    }

    private ManuellBehandlingÅrsak tilKodeverk(Manuellbehandlingårsak input) {
        if (input == null) {
            return ManuellBehandlingÅrsak.UKJENT;
        }
        return repositoryProvider.getKodeverkRepository().finn(ManuellBehandlingÅrsak.class, String.valueOf(input.getId()));
    }

    private PeriodeResultatÅrsak tilKodeverk(PeriodeResultatType periodeResultatType, Årsak årsak) {
        if (årsak == null) {
            return PeriodeResultatÅrsak.UKJENT;
        }

        if (PeriodeResultatType.INNVILGET.equals(periodeResultatType)) {
            //TODO BIXBITE Fjerne når brev støtter flere innvilgesårsaker PFP-355
            InnvilgetÅrsak innvilgetÅrsak = repositoryProvider.getKodeverkRepository().finn(InnvilgetÅrsak.class, String.valueOf(årsak.getId()));
            if (innvilgetÅrsak.getGyldigTilOgMed().isBefore(LocalDate.now())) {
                return InnvilgetÅrsak.UTTAK_OPPFYLT;
            }
            return innvilgetÅrsak;
        }
        return repositoryProvider.getKodeverkRepository().finn(IkkeOppfyltÅrsak.class, String.valueOf(årsak.getId()));
    }

    private GraderingAvslagÅrsak tilKodeverk(GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        if (graderingIkkeInnvilgetÅrsak == null) {
            return GraderingAvslagÅrsak.UKJENT;
        }
        return repositoryProvider.getKodeverkRepository().finn(GraderingAvslagÅrsak.class, String.valueOf(graderingIkkeInnvilgetÅrsak.getId()));
    }

    private static class PeriodeSøknad {
        private final UttakResultatPeriodeSøknadEntitet entitet;
        private final LocalDate fom;
        private final LocalDate tom;

        private PeriodeSøknad(UttakResultatPeriodeSøknadEntitet entitet, LocalDate fom, LocalDate tom) {
            this.entitet = entitet;
            this.fom = fom;
            this.tom = tom;
        }

        boolean harUtledet(UttakPeriode uttakPeriode) {
            return (uttakPeriode.getFom().isEqual(fom) || uttakPeriode.getFom().isAfter(fom))
                && (uttakPeriode.getTom().isEqual(tom) || uttakPeriode.getTom().isBefore(tom));
        }
    }
}









