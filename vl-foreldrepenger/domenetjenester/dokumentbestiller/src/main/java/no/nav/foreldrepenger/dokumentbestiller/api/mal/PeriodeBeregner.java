package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.vedtak.feil.FeilFactory;

public class PeriodeBeregner {

    private PeriodeBeregner() {
        //for sonar
    }


    public static List<UttakResultatPeriodeEntitet> finnPerioderMedStønadskontoType(List<UttakResultatPeriodeEntitet> perioder, StønadskontoType stønadskontoType) {
        return
            perioder.stream()
                .filter(uttakPeriode -> uttakPeriode.getAktiviteter().stream()
                    .map(UttakResultatPeriodeAktivitetEntitet::getTrekkonto)
                    .filter(tk -> stønadskontoType.equals(tk)).count() > 0)
                .collect(Collectors.toList());
    }

    public static BeregningsgrunnlagPeriode finnBeregninsgrunnlagperiode(BeregningsresultatPeriode periode,
                                                                         List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {
        for (BeregningsgrunnlagPeriode beregningsgrunnlagPeriode : beregningsgrunnlagPerioder) {
            if (!periode.getBeregningsresultatPeriodeFom().isBefore(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()) &&
                (beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom() == null || (!periode.getBeregningsresultatPeriodeTom().isAfter(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom())))) {
                return beregningsgrunnlagPeriode;
            }
        }
        throw FeilFactory.create(DokumentBestillerFeil.class).kanIkkeMatchePerioder("beregningsgrunnlagperiode").toException();
    }

    public static UttakResultatPeriodeEntitet finnUttaksPeriode(BeregningsresultatPeriode periode, List<UttakResultatPeriodeEntitet> uttakPerioder) {
        for (UttakResultatPeriodeEntitet uttakPeriode : uttakPerioder) {
            if (!periode.getBeregningsresultatPeriodeFom().isBefore(uttakPeriode.getFom()) && !periode.getBeregningsresultatPeriodeTom().isAfter(uttakPeriode.getTom())) {
                return uttakPeriode;
            }
        }
        throw FeilFactory.create(DokumentBestillerFeil.class).kanIkkeMatchePerioder("uttaksperiode").toException();
    }

    public static Optional<BeregningsgrunnlagPrStatusOgAndel> finnBgPerStatusOgAndelHvisFinnes(AktivitetStatus status,
                                                                                               List<BeregningsgrunnlagPrStatusOgAndel> bgPerStatusOgAndelListe, Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        for (BeregningsgrunnlagPrStatusOgAndel bgPerStatusOgAndel : bgPerStatusOgAndelListe) {
            if (status.equals(bgPerStatusOgAndel.getAktivitetStatus())) {
                if (bgPerStatusOgAndel.gjelderSammeArbeidsforhold(virksomhet, arbeidsforholdRef)) {
                    return Optional.of(bgPerStatusOgAndel);
                }
            }
        }
        return Optional.empty();
    }

    public static boolean alleAktiviteterHarNullUtbetaling(List<UttakResultatPeriodeAktivitetEntitet> uttakAktiviteter) {
        return uttakAktiviteter.stream().allMatch(aktivitet -> aktivitet.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) == 0);
    }

    public static Optional<UttakResultatPeriodeAktivitetEntitet> finnAktivitetMedStatusHvisFinnes(AktivitetStatus status,
                                                                                                  List<UttakResultatPeriodeAktivitetEntitet> uttakAktiviteter, Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        Map<AktivitetStatus, UttakArbeidType> uttakAktivitetStatusMap = new HashMap<>();
        uttakAktivitetStatusMap.put(AktivitetStatus.ARBEIDSTAKER, UttakArbeidType.ORDINÆRT_ARBEID);
        uttakAktivitetStatusMap.put(AktivitetStatus.FRILANSER, UttakArbeidType.FRILANS);
        uttakAktivitetStatusMap.put(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE);

        for (UttakResultatPeriodeAktivitetEntitet aktivitet : uttakAktiviteter) {
            if (uttakAktivitetStatusMap.getOrDefault(status, UttakArbeidType.ANNET).equals(aktivitet.getUttakArbeidType())) {
                if (virksomhet == null || virksomhet.getOrgnr().equals(aktivitet.getArbeidsforholdOrgnr())) {
                    if (arbeidsforholdRef == null || (arbeidsforholdRef.gjelderForSpesifiktArbeidsforhold() && arbeidsforholdRef.getReferanse().equals(aktivitet.getArbeidsforholdId()))) {
                        return Optional.of(aktivitet);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static void mapUttakPerioder(DokumentTypeMedPerioderDto dto, List<UttakResultatPeriodeEntitet> perioder) {
        for (UttakResultatPeriodeEntitet periode : perioder) {
            mapUttakPeriode(dto, periode);
        }
    }

    private static void mapUttakPeriodeAktivitet(DokumentTypeMedPerioderDto dto, UttakResultatPeriodeAktivitetEntitet aktivitet) {
        if (StønadskontoType.FELLESPERIODE.equals(aktivitet.getTrekkonto())) {
            if (dto.getSisteDagIFellesPeriode() == null || dto.getSisteDagIFellesPeriode().isBefore(aktivitet.getTom())) {
                dto.setSisteDagIFellesPeriode(aktivitet.getTom());
            }
        }
    }

    private static void mapInnvilgetUttakPeriode(DokumentTypeMedPerioderDto dto, UttakResultatPeriodeEntitet periode) {
        dto.setInnvilgetFinnes(true);
        if (dto.getSisteDagAvSistePeriode() == null || dto.getSisteDagAvSistePeriode().isBefore(periode.getTom())) {
            dto.setSisteDagAvSistePeriode(periode.getTom());
            dto.setStønadsperiodeTom(periode.getTom());
            dto.setSisteUtbetalingsdag(periode.getTom());
        }
        if (dto.getStønadsperiodeFom() == null || dto.getStønadsperiodeFom().isAfter(periode.getFom())) {
            dto.setStønadsperiodeFom(periode.getFom());
        }
    }

    private static void mapUttakPeriode(DokumentTypeMedPerioderDto dto, UttakResultatPeriodeEntitet periode) {
        for (UttakResultatPeriodeAktivitetEntitet aktivitet : periode.getAktiviteter()) {
            mapUttakPeriodeAktivitet(dto, aktivitet);
        }
        if (PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType())) {
            mapInnvilgetUttakPeriode(dto, periode);
        } else if (PeriodeResultatType.AVSLÅTT.equals(periode.getPeriodeResultatType())) {
            dto.setAvslagFinnes(true);

        } else if (!UttakUtsettelseType.UDEFINERT.equals(periode.getUtsettelseType())) {
            if (dto.getSisteDagMedUtsettelse() == null || dto.getSisteDagMedUtsettelse().isBefore(periode.getTom())) {
                dto.setSisteDagMedUtsettelse(periode.getTom());
            }
        }
    }

    //TODO PK-54195 lag nye tester
    public static int beregnTapteDagerFørTermin(List<UttakResultatPeriodeEntitet> perioder, Optional<Stønadskonto> stønadsKontoForeldrepengerFørFødsel) {
        int totaltAntallDager = stønadsKontoForeldrepengerFørFødsel.map(Stønadskonto::getMaxDager).orElse(0);
        if (totaltAntallDager <= 0) {
            return 0;
        }
        List<UttakResultatPeriodeEntitet> perioderMedForeldrepengerFørFødsel = finnPerioderMedStønadskontoType(perioder, StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        int brukteDager = 0;
        for (UttakResultatPeriodeEntitet periode : perioderMedForeldrepengerFørFødsel) {
            brukteDager += periode.getAktiviteter().stream().filter(aktivitet -> StønadskontoType.FORELDREPENGER_FØR_FØDSEL.equals(aktivitet.getTrekkonto())).filter(aktivitet -> aktivitet.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0).mapToInt(UttakResultatPeriodeAktivitetEntitet::getTrekkdager).max().orElse(0);
        }
        return totaltAntallDager - brukteDager;
    }

    public static Optional<Stønadskonto> finnStønadsKontoMedType(Set<Stønadskonto> stønadskontoer, StønadskontoType foreldrepengerFørFødsel) {
        return stønadskontoer.stream().
            filter(stønadskonto -> foreldrepengerFørFødsel.equals(stønadskonto.getStønadskontoType()))
            .findFirst();
    }

    public static void setDefaultVerdier(DokumentTypeMedPerioderDto dto) {
        dto.setDagerTaptFørTermin(0);
        dto.setDisponibleDager(0);
        dto.setDisponibleFellesDager(0);
        dto.setAntallPerioder(0);
        dto.setAvslagFinnes(false);
        dto.setInnvilgetFinnes(false);
        dto.setSisteDagAvSistePeriode(LocalDate.MIN);
    }

    public static boolean erPeriodeDekket(UttakResultatPeriodeEntitet uttaksPeriode, List<BeregningsresultatPeriode> perioder) {
        for (BeregningsresultatPeriode periode : perioder) {
            if (!periode.getBeregningsresultatPeriodeFom().isBefore(uttaksPeriode.getFom()) && !periode.getBeregningsresultatPeriodeTom().isAfter(uttaksPeriode.getTom())) {
                return true;
            }
        }
        return false;
    }
}
