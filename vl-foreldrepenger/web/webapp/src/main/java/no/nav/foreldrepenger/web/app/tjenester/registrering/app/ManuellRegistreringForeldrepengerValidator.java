package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.util.Objects.isNull;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.PAAKREVD_FELT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.DekningsgradDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.EgenVirksomhetDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.FrilansDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.OverføringsperiodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.VirksomhetDto;

class ManuellRegistreringForeldrepengerValidator {

    private ManuellRegistreringForeldrepengerValidator() {
        // Klassen skal ikke instansieres
    }

    static List<FeltFeilDto> validerOpplysninger(ManuellRegistreringForeldrepengerDto registreringDto) {
        return Stream.of(
            validerAndreYtelser(),
            validerDekningsgrad(registreringDto.getDekningsgrad()),
            validerEgenVirksomhet(registreringDto.getEgenVirksomhet()),
            validerFrilans(registreringDto.getFrilans()),
            validerTidsromPermisjon(registreringDto))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    static List<FeltFeilDto> validerFrilans(FrilansDto frilans) {
        if (Boolean.TRUE.equals(frilans.getHarSokerPeriodeMedFrilans())) {
            if (frilans.getPerioder().isEmpty()) {
                return Arrays.asList(new FeltFeilDto("", ManuellRegistreringValidatorTekster.MINDRE_ELLER_LIK_LENGDE));
            }
        }
        return new ArrayList<>();
    }

    static List<FeltFeilDto> validerEgenVirksomhet(EgenVirksomhetDto egenVirksomhet) {
        List<FeltFeilDto> feltFeil = new ArrayList<>();
        String feltnavn = "harArbeidetIEgenVirksomhet";
        if (egenVirksomhet.getHarArbeidetIEgenVirksomhet() == null) {
            feltFeil.add(new FeltFeilDto(feltnavn, ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
        }
        if (Boolean.TRUE.equals(egenVirksomhet.getHarArbeidetIEgenVirksomhet())) {
            for (VirksomhetDto virksomhet : egenVirksomhet.getVirksomheter()) {
                leggTilFeilForVirksomhet(feltFeil, virksomhet);
            }
        }

        return feltFeil;
    }

    private static void leggTilFeilForVirksomhet(List<FeltFeilDto> feltFeil, VirksomhetDto virksomhet) {
        if (virksomhet.getNavn() == null) {
            feltFeil.add(new FeltFeilDto("virksomhetNavn", ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
        }
        if (virksomhet.getVirksomhetRegistrertINorge() == null) {
            feltFeil.add(new FeltFeilDto("virksomhetRegistrertINorge", ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
        }
        if (virksomhet.getLandJobberFra() == null) {
            feltFeil.add(new FeltFeilDto("landJobberFra", ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
        }
        if (virksomhet.getVirksomhetRegistrertINorge() == null) {
            feltFeil.add(new FeltFeilDto("virksomhetRegistrertINorge", ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
        }
        if (Boolean.TRUE.equals(virksomhet.getVirksomhetRegistrertINorge())) {
            if (virksomhet.getOrganisasjonsnummer() == null) {
                feltFeil.add(new FeltFeilDto("virksomhetOrganisasjonsnummer", ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
            }
        }
    }

    static List<FeltFeilDto> validerDekningsgrad(DekningsgradDto dekningsgrad) {
        List<FeltFeilDto> feltFeil = new ArrayList<>();
        String feltnavn = "dekningsgrad";
        if (dekningsgrad == null) {
            feltFeil.add(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
        }
        return feltFeil;
    }

    static List<FeltFeilDto> validerAndreYtelser() {
        return new ArrayList<>();
    }

    static List<FeltFeilDto> validerTidsromPermisjon(ManuellRegistreringForeldrepengerDto registreringDto) {

        List<FeltFeilDto> result = new ArrayList<>();
        //Valider far(medmor) spesifikke felter
        validerTidsromPermisjonFarEllerMedmor(registreringDto).ifPresent(feltFeilDto1 -> result.add(feltFeilDto1));

        //Valider tidspermisjonsfelter som er felles for alle foreldretyper
        TidsromPermisjonDto tidsromPermisjon = registreringDto.getTidsromPermisjon();
        Optional<FeltFeilDto> feltFeilPermisjonsperiode = validerPermisjonsperiode(tidsromPermisjon);
        feltFeilPermisjonsperiode.ifPresent(feil -> result.add(feil));

        result.addAll(validerTidsromPermisjonSpørsmål(tidsromPermisjon));

        if (tidsromPermisjon.getUtsettelsePeriode().isEmpty()) {
            result.addAll(validerUtsettelse(tidsromPermisjon.getUtsettelsePeriode()));
        }
        if (tidsromPermisjon.getGraderingPeriode().isEmpty()) {
            result.addAll(validerGradering(tidsromPermisjon.getGraderingPeriode()));
        }

        return result;
    }

    static Optional<FeltFeilDto> validerTidsromPermisjonFarEllerMedmor(ManuellRegistreringForeldrepengerDto registreringDto) {
        TidsromPermisjonDto tidsromPermisjon = registreringDto.getTidsromPermisjon();
        OverføringsperiodeDto overføringsperiode = tidsromPermisjon.getOverforingsperiode();
        if (overføringsperiode != null) {
            if (isNull(overføringsperiode.getOverforingArsak())) {
                return Optional.of(new FeltFeilDto("årsakForOverføring", PAAKREVD_FELT));
            }
            //Opprett periode av fra til dato.
            List<ManuellRegistreringValidatorUtil.Periode> perioder = Collections.singletonList(new ManuellRegistreringValidatorUtil.Periode(overføringsperiode.getFomDato(),
                overføringsperiode.getTomDato()));
            return validerSomIkkePåkrevdePerioder(perioder, "årsakForOverføring");
        }
        return Optional.empty();
    }

    static List<FeltFeilDto> validerTidsromPermisjonSpørsmål(TidsromPermisjonDto tidsromPermisjon) {
        List<FeltFeilDto> feltFeil = new ArrayList<>();
        if (isNull(tidsromPermisjon.getDenAndreForelderenHarRettPaForeldrepenger())) {
            feltFeil.add(new FeltFeilDto("HarRettPaForeldrepenger", PAAKREVD_FELT));
        }
        if (isNull(tidsromPermisjon.getSokerHarAleneomsorg())) {
            feltFeil.add(new FeltFeilDto("HarAleneomsorg", PAAKREVD_FELT));
        }
        return feltFeil;
    }

    static List<FeltFeilDto> validerGradering(List<GraderingDto> graderingPerioder) {
        String feltnavnGradering = "gradering";
        List<FeltFeilDto> feltFeilGradering = new ArrayList<>();
        List<ManuellRegistreringValidatorUtil.Periode> perioder = graderingPerioder.stream().map(fkp ->
            new ManuellRegistreringValidatorUtil.Periode(fkp.getPeriodeFom(), fkp.getPeriodeTom())).collect(Collectors.toList());

        Optional<FeltFeilDto> feilIPerioder = validerSomPåkrevdePerioder(perioder, feltnavnGradering);
        feilIPerioder.ifPresent(feil -> feltFeilGradering.add(feil));

        for (GraderingDto gradering : graderingPerioder) {
            if (gradering.getPeriodeForGradering() == null) {
                feltFeilGradering.add(new FeltFeilDto("periodeForGradering", PAAKREVD_FELT));
            }
            if (gradering.getProsentandelArbeid() == null) {
                feltFeilGradering.add(new FeltFeilDto("prosentandelArbeid", PAAKREVD_FELT));
            }
            if (harSamtidigUttakUtenSamtidigUttaksprosent(gradering)) {
                feltFeilGradering.add(new FeltFeilDto("samtidigUttaksprosent", PAAKREVD_FELT));
            }
        }
        return feltFeilGradering;
    }

    private static boolean harSamtidigUttakUtenSamtidigUttaksprosent(GraderingDto gradering) {
        return gradering.getHarSamtidigUttak() && gradering.getSamtidigUttaksprosent() == null;
    }

    private static Optional<FeltFeilDto> validerSomPåkrevdePerioder(List<ManuellRegistreringValidatorUtil.Periode> perioder, String feltnavn) {
        List<String> feilPerioder = new ArrayList<>();

        feilPerioder.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
        if (!feilPerioder.isEmpty()) {
            return Optional.of(new FeltFeilDto(feltnavn, feilPerioder.stream().collect(Collectors.joining(", "))));
        }
        return validerSomIkkePåkrevdePerioder(perioder, feltnavn);
    }

    private static Optional<FeltFeilDto> validerSomIkkePåkrevdePerioder(List<ManuellRegistreringValidatorUtil.Periode> perioder, String feltnavn) {
        List<String> feilPerioder = new ArrayList<>();

        feilPerioder.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
        feilPerioder.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));

        if (!feilPerioder.isEmpty()) {
            return Optional.of(new FeltFeilDto(feltnavn, feilPerioder.stream().collect(Collectors.joining(", "))));
        }
        return Optional.empty();
    }

    static List<FeltFeilDto> validerUtsettelse(List<UtsettelseDto> utsettelsePerioder) {
        String feltnavnTidsromForPermisjon = "utsettelsePerioder";
        List<String> feilUtsettelsePerioder = new ArrayList<>();
        List<FeltFeilDto> feltFeilUtsettelse = new ArrayList<>();

        List<ManuellRegistreringValidatorUtil.Periode> perioder = utsettelsePerioder.stream().map(fkp ->
            new ManuellRegistreringValidatorUtil.Periode(fkp.getPeriodeFom(), fkp.getPeriodeTom())).collect(Collectors.toList());

        feilUtsettelsePerioder.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
        feilUtsettelsePerioder.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
        feilUtsettelsePerioder.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));

        if (!feilUtsettelsePerioder.isEmpty()) {
            FeltFeilDto feltFeilUtsettelsePerioder = new FeltFeilDto(feltnavnTidsromForPermisjon, feilUtsettelsePerioder.stream().collect(Collectors.joining(", ")));
            feltFeilUtsettelse.add(feltFeilUtsettelsePerioder);
        }

        for (UtsettelseDto utsettelse : utsettelsePerioder) {
            if (utsettelse.getArsakForUtsettelse() == null) {
                feltFeilUtsettelse.add(new FeltFeilDto("arsakForUtsettelse", PAAKREVD_FELT));
            }
        }
        return feltFeilUtsettelse;
    }

    static Optional<FeltFeilDto> validerPermisjonsperiode(TidsromPermisjonDto tidsromPermisjon) {
        String feltnavn = "permisjonperioder";
        List<String> feil = new ArrayList<>();
        List<PermisjonPeriodeDto> permisjonperioder = tidsromPermisjon.getPermisjonsPerioder();
        List<ManuellRegistreringValidatorUtil.Periode> perioder = permisjonperioder.stream().map(fkp ->
            new ManuellRegistreringValidatorUtil.Periode(fkp.getPeriodeFom(), fkp.getPeriodeTom())).collect(Collectors.toList());
        feil.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));

        if (feil.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeltFeilDto(feltnavn, feil.stream().collect(Collectors.joining(", "))));
    }
}
