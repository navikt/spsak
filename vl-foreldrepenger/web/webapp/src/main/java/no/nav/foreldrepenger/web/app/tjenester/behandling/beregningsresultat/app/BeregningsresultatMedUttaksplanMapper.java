package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.UttakDto;
import no.nav.vedtak.util.Tuple;

class BeregningsresultatMedUttaksplanMapper {
    private BeregningsresultatMedUttaksplanMapper() {
        // for å hindre instanser
    }

    static BeregningsresultatMedUttaksplanDto lagBeregningsresultatMedUttaksplan(Behandling behandling, UttakResultatEntitet uttakResultat,
                                                                                 BeregningsresultatFP beregningsresultatFP) {
        return BeregningsresultatMedUttaksplanDto.build()
            .medSokerErMor(getSøkerErMor(behandling))
            .medOpphoersdato(getOpphørsdato(uttakResultat).orElse(null))
            .medPerioder(lagPerioder(uttakResultat, beregningsresultatFP))
            .create();
    }

    private static Optional<LocalDate> getOpphørsdato(UttakResultatEntitet uttakResultat) {
        if (uttakResultat == null || uttakResultat.getGjeldendePerioder().getPerioder().isEmpty()) {
            return Optional.empty();
        }
        Set<PeriodeResultatÅrsak> opphørsAvslagÅrsaker = IkkeOppfyltÅrsak.opphørsAvslagÅrsaker();
        List<UttakResultatPeriodeEntitet> perioder = uttakResultat.getGjeldendePerioder().getPerioder()
            .stream()
            .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom).reversed())
            .collect(Collectors.toList());
        // Sjekker om siste periode er avslått med en opphørsårsak
        UttakResultatPeriodeEntitet sistePeriode = perioder.remove(0);
        if (!opphørsAvslagÅrsaker.contains(sistePeriode.getPeriodeResultatÅrsak())) {
            return Optional.empty();
        }
        LocalDate opphørsdato = sistePeriode.getFom();
        for (UttakResultatPeriodeEntitet periode : perioder) {
            if (opphørsAvslagÅrsaker.contains(periode.getPeriodeResultatÅrsak()) && periode.getFom().isBefore(opphørsdato)) {
                opphørsdato = periode.getFom();
            } else {
                return Optional.ofNullable(opphørsdato);
            }
        }
        return Optional.of(opphørsdato);
    }

    private static boolean getSøkerErMor(Behandling behandling) {
        return RelasjonsRolleType.MORA.equals(behandling.getFagsak().getRelasjonsRolleType());
    }

    static List<BeregningsresultatPeriodeDto> lagPerioder(UttakResultatEntitet uttakResultat, BeregningsresultatFP beregningsresultatFP) {
        List<BeregningsresultatPeriode> beregningsresultatPerioder = beregningsresultatFP.getBeregningsresultatPerioder();
        Map<Tuple<AktivitetStatus, Optional<String>>, Optional<LocalDate>> andelTilSisteUtbetalingsdatoMap = finnSisteUtbetalingdatoForAlleAndeler(beregningsresultatPerioder);
        return beregningsresultatPerioder.stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(beregningsresultatPeriode -> BeregningsresultatPeriodeDto.build()
                .medFom(beregningsresultatPeriode.getBeregningsresultatPeriodeFom())
                .medTom(beregningsresultatPeriode.getBeregningsresultatPeriodeTom())
                .medDagsats(beregnDagsats(beregningsresultatPeriode))
                .medAndeler(lagAndeler(beregningsresultatPeriode, uttakResultat, andelTilSisteUtbetalingsdatoMap))
                .create())
            .collect(Collectors.toList());
    }

    static List<BeregningsresultatPeriodeAndelDto> lagAndeler(BeregningsresultatPeriode beregningsresultatPeriode, UttakResultatEntitet uttakResultat, Map<Tuple<AktivitetStatus,
        Optional<String>>, Optional<LocalDate>> andelTilSisteUtbetalingsdatoMap) {

        List<BeregningsresultatAndel> beregningsresultatAndelList = beregningsresultatPeriode.getBeregningsresultatAndelList();

        // grupper alle andeler som har samme aktivitetstatus og arbeidsforholdId og legg dem i en tuple med hendholdsvis brukers og arbeidsgivers andel
        List<Tuple<BeregningsresultatAndel, Optional<BeregningsresultatAndel>>> andelListe = genererAndelListe(beregningsresultatAndelList);
        return andelListe.stream()
            .map(andelPar -> {
                BeregningsresultatAndel brukersAndel = andelPar.getElement1();
                Optional<BeregningsresultatAndel> arbeidsgiversAndel = andelPar.getElement2();
                Virksomhet virksomhet = brukersAndel.getVirksomhet();
                return BeregningsresultatPeriodeAndelDto.build()
                    .medArbeidsgiverNavn(virksomhet == null ? null : virksomhet.getNavn())
                    .medArbeidsgiverOrgnr(virksomhet == null ? null : virksomhet.getOrgnr())
                    .medRefusjon(arbeidsgiversAndel.map(BeregningsresultatAndel::getDagsats).orElse(0))
                    .medTilSøker(brukersAndel.getDagsats())
                    .medUtbetalingsgrad(brukersAndel.getUtbetalingsgrad())
                    .medSisteUtbetalingsdato(andelTilSisteUtbetalingsdatoMap.getOrDefault(genererAndelKey(brukersAndel), Optional.empty()).orElse(null))
                    .medAktivitetstatus(brukersAndel.getAktivitetStatus())
                    .medArbeidsforholdId(brukersAndel.getArbeidsforholdRef() != null
                        ? brukersAndel.getArbeidsforholdRef().getReferanse() : null)
                    .medArbeidsforholdType(brukersAndel.getArbeidsforholdType())
                    .medUttak(lagUttak(uttakResultat, beregningsresultatPeriode, brukersAndel))
                    .create();
            })
            .collect(Collectors.toList());
    }

    private static Map<Tuple<AktivitetStatus, Optional<String>>, Optional<LocalDate>> finnSisteUtbetalingdatoForAlleAndeler(List<BeregningsresultatPeriode> beregningsresultatPerioder) {
        Collector<BeregningsresultatAndel, ?, Optional<LocalDate>> maxTomDatoCollector = Collectors.mapping(andel -> andel.getBeregningsresultatPeriode().getBeregningsresultatPeriodeTom(),
            Collectors.maxBy(Comparator.naturalOrder()));
        return beregningsresultatPerioder.stream()
            .flatMap(brp -> brp.getBeregningsresultatAndelList().stream())
            .filter(andel -> andel.getDagsats() > 0)
            .collect(Collectors.groupingBy(BeregningsresultatMedUttaksplanMapper::genererAndelKey, maxTomDatoCollector));
    }

    private static Tuple<AktivitetStatus, Optional<String>> genererAndelKey(BeregningsresultatAndel andel) {
        return new Tuple<>(andel.getAktivitetStatus(), finnSekundærIdentifikator(andel));
    }

    private static List<Tuple<BeregningsresultatAndel, Optional<BeregningsresultatAndel>>> genererAndelListe(List<BeregningsresultatAndel> beregningsresultatAndelList) {
        Map<Tuple<AktivitetStatus, Optional<String>>, List<BeregningsresultatAndel>> collect = beregningsresultatAndelList.stream()
            .collect(Collectors.groupingBy(BeregningsresultatMedUttaksplanMapper::genererAndelKey));

        return collect.values().stream().map(andeler -> {
            BeregningsresultatAndel brukerAndel = andeler.stream()
                .filter(BeregningsresultatAndel::erBrukerMottaker)
                .reduce(BeregningsresultatMedUttaksplanMapper::slåSammenAndeler)
                .orElseThrow(() -> new IllegalStateException("Utvilkerfeil: Mangler andel for bruker, men skal alltid ha andel for bruker her."));

            Optional<BeregningsresultatAndel> arbeidsgiverAndel = andeler.stream()
                .filter(a -> !a.erBrukerMottaker())
                .reduce(BeregningsresultatMedUttaksplanMapper::slåSammenAndeler);

            return new Tuple<>(brukerAndel, arbeidsgiverAndel);
        })
            .collect(Collectors.toList());
    }

    private static Optional<String> finnSekundærIdentifikator(BeregningsresultatAndel andel) {
        // Denne metoden finner sekundæridentifikator for andelen, etter aktivitetstatus.
        // Mulige identifikatorer i prioritert rekkefølge:
        // 1. arbeidsforholdId
        // 2. orgNr
        if (andel.getArbeidsforholdRef() != null && andel.getArbeidsforholdRef().getReferanse() != null) {
            return Optional.of(andel.getArbeidsforholdRef().getReferanse());
        } else return Optional.ofNullable(andel.getArbeidsforholdOrgnr());
    }

    private static UttakDto lagUttak(UttakResultatEntitet uttakResultat,
                                     BeregningsresultatPeriode beregningsresultatPeriode,
                                     BeregningsresultatAndel brukersAndel) {
        List<UttakResultatPeriodeEntitet> perioder = uttakResultat.getGjeldendePerioder().getPerioder();

        return perioder.stream()
            .findAny()
            .map(uttakResultatPerArbeidsforhold -> finnTilhørendeUttakPeriodeAktivitet(perioder, beregningsresultatPeriode))
            .map(uttakResultatPeriode -> lagUttakDto(uttakResultatPeriode, brukersAndel))
            .orElseThrow(() -> new IllegalArgumentException("UttakResultatEntitet inneholder ikke resultater for gitt arbeidsforholdId."));
    }

    private static UttakDto lagUttakDto(UttakResultatPeriodeEntitet uttakResultatPeriode, BeregningsresultatAndel brukersAndel) {
        List<UttakResultatPeriodeAktivitetEntitet> aktiviteter = uttakResultatPeriode.getAktiviteter();
        UttakResultatPeriodeAktivitetEntitet korrektUttakAndel = finnKorrektUttaksAndel(brukersAndel, aktiviteter);
        return UttakDto.build()
            .medTrekkdager(korrektUttakAndel.getTrekkdager())
            .medStønadskontoType(korrektUttakAndel.getTrekkonto())
            .medPeriodeResultatType(uttakResultatPeriode.getPeriodeResultatType())
            .medGradering(korrektUttakAndel.isGraderingInnvilget())
            .create();
    }

    private static UttakResultatPeriodeAktivitetEntitet finnKorrektUttaksAndel(BeregningsresultatAndel brukersAndel, List<UttakResultatPeriodeAktivitetEntitet> aktiviteter) {
        if (brukersAndel.getAktivitetStatus().equals(AktivitetStatus.FRILANSER)) {
            return førsteAvType(aktiviteter, UttakArbeidType.FRILANS);
        } else if (brukersAndel.getAktivitetStatus().equals(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)) {
            return førsteAvType(aktiviteter, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);
        } else if (brukersAndel.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER)) {
            return finnKorrektArbeidstakerAndel(brukersAndel, aktiviteter);
        } else {
            return førsteAvType(aktiviteter, UttakArbeidType.ANNET);
        }
    }

    private static UttakResultatPeriodeAktivitetEntitet førsteAvType(List<UttakResultatPeriodeAktivitetEntitet> aktiviteter, UttakArbeidType type) {
        return aktiviteter.stream()
            .filter(a -> a.getUttakArbeidType().equals(type))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke periodeaktivitet fra uttak for uttak arbeid type " + type));
    }

    private static UttakResultatPeriodeAktivitetEntitet finnKorrektArbeidstakerAndel(BeregningsresultatAndel brukersAndel, List<UttakResultatPeriodeAktivitetEntitet> aktiviteter) {
        List<UttakResultatPeriodeAktivitetEntitet> korrekteAktiviteter = finnKorrekteAktiviteter(brukersAndel, aktiviteter);
        if (korrekteAktiviteter.size() != 1) {
            throw new IllegalArgumentException("Forventet akkurat 1 uttakaktivitet for beregningsresultat andel " + brukersAndel.getAktivitetStatus() + " "
                + brukersAndel.getArbeidsforholdOrgnr() + " " + brukersAndel.getArbeidsforholdRef() + ". Antall matchende aktiviteter var " + korrekteAktiviteter.size());

        }
        return korrekteAktiviteter.get(0);
    }

    private static List<UttakResultatPeriodeAktivitetEntitet> finnKorrekteAktiviteter(BeregningsresultatAndel brukersAndel, List<UttakResultatPeriodeAktivitetEntitet> aktiviteter) {
        return aktiviteter.stream()
            .filter(aktivitet -> Objects.equals(brukersAndel.getArbeidsforholdOrgnr(), aktivitet.getArbeidsforholdOrgnr()))
            .filter(aktivitet -> {
                String referanse = brukersAndel.getArbeidsforholdRef() == null ? null : brukersAndel.getArbeidsforholdRef().getReferanse();
                return Objects.equals(referanse, aktivitet.getArbeidsforholdId());
            })
            .filter(aktivitet -> Objects.equals(UttakArbeidType.ORDINÆRT_ARBEID, aktivitet.getUttakArbeidType()))
            .collect(Collectors.toList());
    }

    private static UttakResultatPeriodeEntitet finnTilhørendeUttakPeriodeAktivitet(Collection<UttakResultatPeriodeEntitet> uttakResultatPerioder,
                                                                                   BeregningsresultatPeriode beregningsresultatPeriode) {
        return uttakResultatPerioder.stream()
            .filter(uttakResultatPeriode -> !uttakResultatPeriode.getFom().isAfter(beregningsresultatPeriode.getBeregningsresultatPeriodeFom()))
            .filter(uttakResultatPeriode -> !uttakResultatPeriode.getTom().isBefore(beregningsresultatPeriode.getBeregningsresultatPeriodeTom()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("BeregningsresultatPeriode tilhører ikke noen periode fra UttakResultatEntitet"));
    }

    static int beregnDagsats(BeregningsresultatPeriode beregningsresultatPeriode) {
        return beregningsresultatPeriode.getBeregningsresultatAndelList().stream()
            .mapToInt(BeregningsresultatAndel::getDagsats)
            .sum();
    }

    private static BeregningsresultatAndel slåSammenAndeler(BeregningsresultatAndel a, BeregningsresultatAndel b) {
        ArbeidsforholdRef førsteArbeidsforholdId = a.getArbeidsforholdRef();
        ArbeidsforholdRef andreArbeidsforholdId = b.getArbeidsforholdRef();
        boolean harUlikeArbeidsforholdIder = false;
        if (førsteArbeidsforholdId != null && andreArbeidsforholdId != null) {
            harUlikeArbeidsforholdIder = !Objects.equals(førsteArbeidsforholdId.getReferanse(), andreArbeidsforholdId.getReferanse());
        }
        if (harUlikeArbeidsforholdIder
            || a.getUtbetalingsgrad().compareTo(b.getUtbetalingsgrad()) != 0
            || a.getStillingsprosent().compareTo(b.getStillingsprosent()) != 0
            || !a.getBeregningsresultatPeriode().equals(b.getBeregningsresultatPeriode())) {
            throw new IllegalStateException("Utviklerfeil: Andeler som slås sammen skal ikke ha ulikt arbeidsforhold, periode, stillingsprosent eller utbetalingsgrad");
        }
        BeregningsresultatAndel ny = Kopimaskin.deepCopy(a, a.getBeregningsresultatPeriode());
        BeregningsresultatAndel.builder(ny)
            .medDagsats(a.getDagsats() + b.getDagsats())
            .medDagsatsFraBg(a.getDagsatsFraBg() + b.getDagsatsFraBg());
        return ny;
    }
}
