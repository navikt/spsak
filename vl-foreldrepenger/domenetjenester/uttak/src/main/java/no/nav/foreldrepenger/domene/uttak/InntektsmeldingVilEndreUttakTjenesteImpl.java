package no.nav.foreldrepenger.domene.uttak;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.foreldrepenger.domene.uttak.perioder.PerioderUtenHelgUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class InntektsmeldingVilEndreUttakTjenesteImpl implements InntektsmeldingVilEndreUttakTjeneste {

    private UttakRepository uttakRepository;

    InntektsmeldingVilEndreUttakTjenesteImpl() {
        // CDI
    }

    @Inject
    public InntektsmeldingVilEndreUttakTjenesteImpl(UttakRepository uttakRepository) {
        this.uttakRepository = uttakRepository;
    }

    @Override
    public boolean graderingVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding) {
        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);
        for (Gradering gradering : inntektsmelding.getGraderinger()) {
            if (!graderingFinnesIUttak(uttakResultat, gradering, inntektsmelding)) {
                return true;
            }
        }

        List<UttakResultatPeriodeAktivitetEntitet> graderteAktiviteter = graderteAktiviteterMedArbeidsforhold(uttakResultat);
        for (UttakResultatPeriodeAktivitetEntitet gradertAktivitet : graderteAktiviteter) {
            if (!gradertAktivitetFinnesIInntektsmelding(gradertAktivitet, inntektsmelding)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean utsettelseArbeidVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding) {
        return utsettelseVilEndreUttak(behandling, inntektsmelding, UtsettelseÅrsak.ARBEID, UttakUtsettelseType.ARBEID);
    }

    @Override
    public boolean utsettelseFerieVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding) {
        return utsettelseVilEndreUttak(behandling, inntektsmelding, UtsettelseÅrsak.FERIE, UttakUtsettelseType.FERIE);
    }

    private boolean utsettelseVilEndreUttak(Behandling behandling, Inntektsmelding inntektsmelding,
                                            UtsettelseÅrsak inntektsmeldingUtsettelseType,
                                            UttakUtsettelseType uttakUtsettelseType) {
        UttakResultatEntitet uttakResultat = uttakRepository.hentUttakResultat(behandling);

        for (UtsettelsePeriode utsettelse : inntektsmelding.getUtsettelsePerioder()) {
            if (Objects.equals(utsettelse.getÅrsak(), inntektsmeldingUtsettelseType) && !utsettelseFinnesIUttak(uttakResultat, utsettelse, uttakUtsettelseType)) {
                return true;
            }
        }

        List<UttakResultatPeriodeEntitet> perioderMedUtsettelseIUttak = utsettelsePerioderMedArbeidsforhold(uttakResultat);
        for (UttakResultatPeriodeEntitet uttsettelsesPeriode : perioderMedUtsettelseIUttak) {
            if (Objects.equals(uttsettelsesPeriode.getUtsettelseType(), uttakUtsettelseType) &&
                !utsettelseFinnesIInntektsmelding(uttsettelsesPeriode, inntektsmelding, inntektsmeldingUtsettelseType, uttakUtsettelseType)) {
                return true;
            }
        }

        return false;
    }

    private boolean utsettelseFinnesIInntektsmelding(UttakResultatPeriodeEntitet utsettelsesPeriode,
                                                     Inntektsmelding inntektsmelding,
                                                     UtsettelseÅrsak inntektsmeldingUtsettelseType,
                                                     UttakUtsettelseType uttakUtsettelseType) {
        for (UtsettelsePeriode inntektsmeldingUtsettelse : inntektsmelding.getUtsettelsePerioder()) {
            if (PerioderUtenHelgUtil.periodeUtenHelgOmslutter(inntektsmeldingUtsettelse.getPeriode().getFomDato(), inntektsmeldingUtsettelse.getPeriode().getTomDato(),
                utsettelsesPeriode.getFom(), utsettelsesPeriode.getTom())) {
                if (Objects.equals(inntektsmeldingUtsettelse.getÅrsak(), inntektsmeldingUtsettelseType) &&
                    Objects.equals(utsettelsesPeriode.getUtsettelseType(), uttakUtsettelseType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<UttakResultatPeriodeEntitet> utsettelsePerioderMedArbeidsforhold(UttakResultatEntitet uttakResultat) {
        return uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .filter(periode -> !Objects.equals(UttakUtsettelseType.UDEFINERT, periode.getUtsettelseType()))
            .collect(Collectors.toList());
    }

    private boolean gradertAktivitetFinnesIInntektsmelding(UttakResultatPeriodeAktivitetEntitet gradertAktivitet, Inntektsmelding inntektsmelding) {
        for (Gradering inntektsmeldingGradering : inntektsmelding.getGraderinger()) {
            if (omslutter(inntektsmeldingGradering, gradertAktivitet) && likArbeidstidsprosent(gradertAktivitet, inntektsmeldingGradering)) {
                return true;
            }
        }
        return false;
    }

    private boolean likArbeidstidsprosent(UttakResultatPeriodeAktivitetEntitet gradertAktivitet, Gradering inntektsmeldingGradering) {
        return Objects.equals(inntektsmeldingGradering.getArbeidstidProsent(), gradertAktivitet.getArbeidsprosent());
    }

    private boolean omslutter(Gradering inntektsmeldingGradering, UttakResultatPeriodeAktivitetEntitet gradertAktivitet) {
        return PerioderUtenHelgUtil.periodeUtenHelgOmslutter(inntektsmeldingGradering.getPeriode().getFomDato(), inntektsmeldingGradering.getPeriode().getTomDato(),
            gradertAktivitet.getFom(), gradertAktivitet.getTom());
    }

    private List<UttakResultatPeriodeAktivitetEntitet> graderteAktiviteterMedArbeidsforhold(UttakResultatEntitet uttakResultat) {
        return uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .flatMap(periode -> periode.getAktiviteter().stream())
            .filter(UttakResultatPeriodeAktivitetEntitet::isGraderingInnvilget)
            .collect(Collectors.toList());
    }

    private boolean utsettelseFinnesIUttak(UttakResultatEntitet uttakResultat,
                                           UtsettelsePeriode utsettelse,
                                           UttakUtsettelseType utsettelseType) {
        List<UttakResultatPeriodeEntitet> perioder = matchendePerioderIUttak(uttakResultat, utsettelse.getPeriode());

        if (perioder.isEmpty()) {
            return false;
        }

        return perioder.stream()
            .allMatch(periode -> Objects.equals(periode.getUtsettelseType(), utsettelseType));
    }

    private List<UttakResultatPeriodeEntitet> matchendePerioderIUttak(UttakResultatEntitet uttakResultat, DatoIntervallEntitet tidsperiode) {
        return uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .filter(periode -> omslutter(periode, tidsperiode))
            .collect(Collectors.toList());
    }

    private boolean graderingFinnesIUttak(UttakResultatEntitet uttakResultat,
                                          Gradering gradering,
                                          Inntektsmelding inntektsmelding) {
        List<UttakResultatPeriodeAktivitetEntitet> aktiviteterMedArbeidsforholdIperiode =
            aktiviteterMedArbeidsforholdIPeriode(uttakResultat, gradering, inntektsmelding);

        if (aktiviteterMedArbeidsforholdIperiode.isEmpty()) {
            return false;
        }

        return aktiviteterMedArbeidsforholdIperiode.stream()
            .allMatch(aktivitet -> Objects.equals(aktivitet.getArbeidsprosent(), gradering.getArbeidstidProsent()));
    }

    private List<UttakResultatPeriodeAktivitetEntitet> aktiviteterMedArbeidsforholdIPeriode(UttakResultatEntitet uttakResultat,
                                                                                             Gradering gradering,
                                                                                             Inntektsmelding inntektsmelding) {
        return uttakResultat.getGjeldendePerioder().getPerioder().stream()
            .filter(periode -> omslutter(periode, gradering.getPeriode()))
            .flatMap(periode -> periode.getAktiviteter().stream())
            .filter(aktivitet -> Objects.equals(aktivitet.getArbeidsforholdOrgnr(), inntektsmelding.getVirksomhet().getOrgnr()))
            .filter(aktivitet -> Objects.equals(aktivitet.getArbeidsforholdId(), inntektsmelding.getArbeidsforholdRef().getReferanse()))
            .collect(Collectors.toList());
    }

    private boolean omslutter(UttakResultatPeriodeEntitet periode, DatoIntervallEntitet tidsperiode) {
        return PerioderUtenHelgUtil.periodeUtenHelgOmslutter(tidsperiode.getFomDato(), tidsperiode.getTomDato(),
            periode.getFom(), periode.getTom());
    }
}
