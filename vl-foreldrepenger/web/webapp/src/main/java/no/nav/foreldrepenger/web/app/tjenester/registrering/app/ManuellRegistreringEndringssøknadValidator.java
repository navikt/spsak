package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.util.Objects.isNull;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.PAAKREVD_FELT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;

class ManuellRegistreringEndringssøknadValidator {

    private ManuellRegistreringEndringssøknadValidator() {
    }

    static List<FeltFeilDto> validerOpplysninger(ManuellRegistreringEndringsøknadDto registreringDto) {
        return Stream.of(
            validerTidsromPermisjon(registreringDto))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    static List<FeltFeilDto> validerTidsromPermisjon(ManuellRegistreringEndringsøknadDto registreringDto) {
        List<FeltFeilDto> result = new ArrayList<>();
        //Valider far(medmor) spesifikke felter
        validerOverføringAvKvoter(registreringDto).ifPresent(feltFeilDto1 -> result.add(feltFeilDto1));

        //Valider tidspermisjonsfelter som er felles for alle foreldretyper
        TidsromPermisjonDto tidsromPermisjon = registreringDto.getTidsromPermisjon();
        if(tidsromPermisjon != null) {
            Optional<FeltFeilDto> feltFeilPermisjonsperiode = validerPermisjonsperiode(tidsromPermisjon);
            feltFeilPermisjonsperiode.ifPresent(feil -> result.add(feil));

            if (tidsromPermisjon.getUtsettelsePeriode() != null) {
                result.addAll(ManuellRegistreringForeldrepengerValidator.validerUtsettelse(tidsromPermisjon.getUtsettelsePeriode()));
            }
            if (tidsromPermisjon.getGraderingPeriode() != null) {
                result.addAll(ManuellRegistreringForeldrepengerValidator.validerGradering(tidsromPermisjon.getGraderingPeriode()));
            }
        }

        return result;
    }

    static Optional<FeltFeilDto> validerPermisjonsperiode(TidsromPermisjonDto tidsromPermisjon) {
        String feltnavn = "permisjonperioder";
        List<String> feil = new ArrayList<>();
        List<PermisjonPeriodeDto> permisjonperioder = tidsromPermisjon.getPermisjonsPerioder();
        if (!isNull(permisjonperioder)) {
            List<ManuellRegistreringValidatorUtil.Periode> perioder = permisjonperioder.stream().map(fkp ->
                new ManuellRegistreringValidatorUtil.Periode(fkp.getPeriodeFom(), fkp.getPeriodeTom())).collect(Collectors.toList());
            feil.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
            feil.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
            feil.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));
        }

        if (feil.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeltFeilDto(feltnavn, feil.stream().collect(Collectors.joining(", "))));
    }

    static Optional<FeltFeilDto> validerOverføringAvKvoter(ManuellRegistreringEndringsøknadDto registreringDto) {
        TidsromPermisjonDto tidsromPermisjon = registreringDto.getTidsromPermisjon();

        if (tidsromPermisjon.getOverforingsperiode() != null && isNull(tidsromPermisjon.getOverforingsperiode().getOverforingArsak())) {
            return Optional.of(new FeltFeilDto("årsakForOverføring", PAAKREVD_FELT));
        }
        // FIXME PK-53783: legge til validering paa fom/tom finnes ved case og fom foer tom

        return Optional.empty();
    }

}
