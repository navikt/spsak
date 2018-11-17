package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatPeriodeDto;
import no.nav.vedtak.util.Tuple;

class BeregningsresultatMedUttaksplanMapper {
    private BeregningsresultatMedUttaksplanMapper() {
        // for å hindre instanser
    }

    static BeregningsresultatMedUttaksplanDto lagBeregningsresultatMedUttaksplan(Behandling behandling,
                                                                                 BeregningsresultatFP beregningsresultatFP) {
        LocalDate opphørsdato = null; // FIXME SP: trenger opphørsdato og uttak modell
        return BeregningsresultatMedUttaksplanDto.build()
            .medSokerErMor(getSøkerErMor(behandling))
            .medOpphoersdato(opphørsdato)
            .medPerioder(lagPerioder(beregningsresultatFP))
            .create();
    }

    private static boolean getSøkerErMor(Behandling behandling) {
        return RelasjonsRolleType.MORA.equals(behandling.getFagsak().getRelasjonsRolleType());
    }

    static List<BeregningsresultatPeriodeDto> lagPerioder(BeregningsresultatFP beregningsresultatFP) {
        // FIXME SP: håndter resultat ifht. uttak andeler (her fjernet gammel foreldrepenger modell).
        List<BeregningsresultatPeriode> beregningsresultatPerioder = beregningsresultatFP.getBeregningsresultatPerioder();
        Map<Tuple<AktivitetStatus, Optional<String>>, Optional<LocalDate>> andelTilSisteUtbetalingsdatoMap = finnSisteUtbetalingdatoForAlleAndeler(beregningsresultatPerioder);
        return beregningsresultatPerioder.stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(beregningsresultatPeriode -> BeregningsresultatPeriodeDto.build()
                .medFom(beregningsresultatPeriode.getBeregningsresultatPeriodeFom())
                .medTom(beregningsresultatPeriode.getBeregningsresultatPeriodeTom())
                .medDagsats(beregnDagsats(beregningsresultatPeriode))
                .medAndeler(lagAndeler(beregningsresultatPeriode, andelTilSisteUtbetalingsdatoMap))
                .create())
            .collect(Collectors.toList());
    }

    static List<BeregningsresultatPeriodeAndelDto> lagAndeler(BeregningsresultatPeriode beregningsresultatPeriode, Map<Tuple<AktivitetStatus,
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
