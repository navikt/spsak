package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatDokRegelEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TrekkdagerUtregningUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.vedtak.feil.FeilFactory;

final class FastsettePerioderRevurderingUtil {

    private FastsettePerioderRevurderingUtil() {
    }

    static Set<Stønadskonto> reduserteStønadskontoer(UttakResultatEntitet opprinneligUttak,
                                                     LocalDate endringsdato,
                                                     Set<Stønadskonto> originaleStønadskontoer,
                                                     AktivitetIdentifikator aktivitetIdentifikator) {
        Set<Stønadskonto> reduserte = new HashSet<>();
        originaleStønadskontoer.forEach(originalStønadskonto -> reduserte.add(redusertStønadskonto(originalStønadskonto,
            opprinneligUttak, endringsdato, aktivitetIdentifikator)));
        return reduserte;
    }

    static List<UttakResultatPeriodeEntitet> perioderFørEndringsdato(UttakResultatEntitet opprinneligUttak, LocalDate endringsdato) {
        List<UttakResultatPeriodeEntitet> opprinneligePerioder = opprinneligUttak.getGjeldendePerioder().getPerioder();

        List<UttakResultatPeriodeEntitet> perioderFør = new ArrayList<>();

        for (UttakResultatPeriodeEntitet periode : opprinneligePerioder) {
            if (periode.getTom().isBefore(endringsdato)) {
                perioderFør.add(kopierPeriode(periode));
            } else if (periode.getTidsperiode().inkluderer(endringsdato) && !periode.getFom().isEqual(endringsdato)) {
                perioderFør.add(splittPeriode(periode, endringsdato));
            }
        }

        return perioderFør;
    }

    static LocalDate finnEndringsdatoRevurdering(Behandling revurdering, YtelsesFordelingRepository ytelsesFordelingRepository) {
        return ytelsesFordelingRepository.hentAggregat(revurdering).getAvklarteDatoer()
            .orElseThrow(() -> FeilFactory.create(FastsettePerioderFeil.class).manglendeAvklarteDatoer().toException())
            .getEndringsdato();
    }

    static Behandling finnOriginalBehandling(Behandling revurdering) {
        return revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Original behandling mangler på revurdering - skal ikke skje"));
    }

    private static Stønadskonto redusertStønadskonto(Stønadskonto originalStønadskonto,
                                                     UttakResultatEntitet opprinneligUttak,
                                                     LocalDate endringsdato,
                                                     AktivitetIdentifikator aktivitetIdentifikator) {
        int reduserteMaksdager = originalStønadskonto.getMaxDager() - antallDagerBruktFramTilDato(originalStønadskonto.getStønadskontoType(),
            opprinneligUttak, endringsdato, aktivitetIdentifikator);
        Stønadskonto.Builder builder = new Stønadskonto.Builder()
            .medMaxDager(reduserteMaksdager)
            .medStønadskontoType(originalStønadskonto.getStønadskontoType());
        return builder.build();
    }

    private static int antallDagerBruktFramTilDato(StønadskontoType stønadskontoType,
                                                   UttakResultatEntitet opprinneligUttak,
                                                   LocalDate endringsdato,
                                                   AktivitetIdentifikator aktivitetIdentifikator) {

        return perioderFørEndringsdato(opprinneligUttak, endringsdato)
            .stream()
            .flatMap(periode -> periode.getAktiviteter().stream())
            .filter(aktivitet -> isEqual(aktivitet.getUttakAktivitet(), aktivitetIdentifikator))
            .filter(aktivitet -> Objects.equals(aktivitet.getTrekkonto(), stønadskontoType))
            .mapToInt(UttakResultatPeriodeAktivitetEntitet::getTrekkdager)
            .sum();
    }

    private static UttakResultatPeriodeEntitet splittPeriode(UttakResultatPeriodeEntitet periode, LocalDate endringsdato) {
        UttakResultatPeriodeEntitet nyPeriode = kopierPeriode(periode, endringsdato.minusDays(1));
        for (UttakResultatPeriodeAktivitetEntitet aktivitet : periode.getAktiviteter()) {
            nyPeriode.leggTilAktivitet(kopierAktivitet(aktivitet, nyPeriode, regnUtTrekkdager(aktivitet, nyPeriode.getTom())));
        }
        return nyPeriode;
    }

    private static UttakResultatPeriodeEntitet kopierPeriode(UttakResultatPeriodeEntitet periode) {
        UttakResultatPeriodeEntitet nyPeriode = kopierPeriode(periode, periode.getTom());
        for (UttakResultatPeriodeAktivitetEntitet aktivitet : periode.getAktiviteter()) {
            nyPeriode.leggTilAktivitet(kopierAktivitet(aktivitet, nyPeriode, aktivitet.getTrekkdager()));
        }
        return nyPeriode;
    }

    private static UttakResultatPeriodeEntitet kopierPeriode(UttakResultatPeriodeEntitet periode, LocalDate nyTom) {
        UttakResultatPeriodeEntitet.Builder builder = new UttakResultatPeriodeEntitet.Builder(periode.getFom(), nyTom)
            .medPeriodeResultat(periode.getPeriodeResultatType(), periode.getPeriodeResultatÅrsak())
            .medGraderingInnvilget(periode.isGraderingInnvilget())
            .medUtsettelseType(periode.getUtsettelseType())
            .medSamtidigUttak(periode.isSamtidigUttak())
            .medFlerbarnsdager(periode.isFlerbarnsdager())
            .medGraderingAvslagÅrsak(periode.getGraderingAvslagÅrsak())
            .medManueltBehandlet(periode.isManueltBehandlet())
            .medBegrunnelse(periode.getBegrunnelse());
        if (periode.getDokRegel() != null) {
            builder.medDokRegel(kopierDokRegel(periode.getDokRegel()));
        }
        if (periode.getPeriodeSøknad().isPresent()) {
            builder.medPeriodeSoknad(periode.getPeriodeSøknad().get());
        }
        return builder.build();
    }

    private static UttakResultatDokRegelEntitet kopierDokRegel(UttakResultatDokRegelEntitet dokRegel) {
        UttakResultatDokRegelEntitet.Builder nyDokRegel;
        if (dokRegel.isTilManuellBehandling()) {
            nyDokRegel = UttakResultatDokRegelEntitet.medManuellBehandling(dokRegel.getManuellBehandlingÅrsak());
        } else {
            nyDokRegel = UttakResultatDokRegelEntitet.utenManuellBehandling();
        }
        return nyDokRegel
            .medRegelEvaluering(dokRegel.getRegelEvaluering())
            .medRegelInput(dokRegel.getRegelInput())
            .build();
    }

    private static UttakResultatPeriodeAktivitetEntitet kopierAktivitet(UttakResultatPeriodeAktivitetEntitet aktivitet,
                                                                        UttakResultatPeriodeEntitet periode,
                                                                        int nyeTrekkdager) {
        return new UttakResultatPeriodeAktivitetEntitet.Builder(periode, aktivitet.getUttakAktivitet())
            .medArbeidsprosent(aktivitet.getArbeidsprosent())
            .medTrekkdager(nyeTrekkdager)
            .medTrekkonto(aktivitet.getTrekkonto())
            .medErSøktGradering(aktivitet.isSøktGradering())
            .medUtbetalingsprosent(aktivitet.getUtbetalingsprosent())
            .build();
    }

    private static int regnUtTrekkdager(UttakResultatPeriodeAktivitetEntitet aktivitet, LocalDate tom) {
        return TrekkdagerUtregningUtil.trekkdagerFor(new Periode(aktivitet.getFom(), tom), aktivitet.isGraderingInnvilget(), aktivitet.getTrekkdager() != 0,
            aktivitet.getArbeidsprosent(), false);
    }

    private static boolean isEqual(UttakAktivitetEntitet uttakAktivitet, AktivitetIdentifikator aktivitetIdentifikator) {
        return Objects.equals(uttakAktivitet.getUttakArbeidType(), FastsettePerioderRegelResultatKonvertererImpl.lagArbeidType(aktivitetIdentifikator))
            && Objects.equals(uttakAktivitet.getArbeidsforholdOrgnr(), aktivitetIdentifikator.getOrgNr())
            && Objects.equals(uttakAktivitet.getArbeidsforholdId(), aktivitetIdentifikator.getArbeidsforholdId());
    }
}
