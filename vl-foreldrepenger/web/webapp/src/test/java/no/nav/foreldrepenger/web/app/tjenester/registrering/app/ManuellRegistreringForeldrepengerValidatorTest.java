package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.FrilansDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.DekningsgradDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;

public class ManuellRegistreringForeldrepengerValidatorTest {


    @Test
    public void skal_validere_dekningsgrad() {
        DekningsgradDto dekningsgrad = null;
        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerDekningsgrad(dekningsgrad);
        assertThat(feltFeil).as("Dekningsgrad skal ikke kunne være null").isNotEmpty();

        dekningsgrad = DekningsgradDto.HUNDRE;
        feltFeil = ManuellRegistreringForeldrepengerValidator.validerDekningsgrad(dekningsgrad);
        assertThat(feltFeil).as("Deksningsgrad skal være gyldig når dekningsgrad er satt").isEmpty();
    }

    @Test
    public void skal_validere_fellesperiode_for_far_eller_medmor_dato_satt_til_null() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());

        // Setter en av datoene i fellesperioden til null
        permisjon.getPermisjonsPerioder().get(0).setPeriodeFom(null);

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_fellesperiode_for_far_eller_medmor_overlappende_perioder() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());

        // Gjør perioder overlappende
        permisjon.getPermisjonsPerioder().get(1).setPeriodeFom(LocalDate.now());

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER));
    }

    @Test
    public void skal_validere_fellesperiode_for_far_eller_medmor() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isNotPresent();
    }

    @Test
    public void skal_validere_fellesperiode_for_far_eller_medmor_start_for_sluttdato() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());

        // Sett start før slutt
        permisjon.getPermisjonsPerioder().get(1).setPeriodeTom(LocalDate.now());

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_fedrekvote_dato_satt_til_null() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigPermisjonPeriode(UttakPeriodeType.FEDREKVOTE));

        // Setter en av datoene i perioden til null
        permisjon.getPermisjonsPerioder().get(0).setPeriodeFom(null);

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_fedrekvote_overlappende_perioder() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigPermisjonPeriode(UttakPeriodeType.FEDREKVOTE));

        // Gjør perioder overlappende
        permisjon.getPermisjonsPerioder().get(1).setPeriodeFom(LocalDate.now());

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER));
    }

    @Test
    public void skal_validere_fedrekvote_start_for_sluttdato() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigPermisjonPeriode(UttakPeriodeType.FEDREKVOTE));

        // Sett start før slutt
        permisjon.getPermisjonsPerioder().get(1).setPeriodeTom(LocalDate.now());

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_fedrekvote() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigPermisjonPeriode(UttakPeriodeType.FEDREKVOTE));

        Optional<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerPermisjonsperiode(permisjon);
        assertThat(feltFeil).isNotPresent();
    }


    @Test
    public void skal_validere_utsettelse_dato_satt_til_null() {
        List<UtsettelseDto> utsettelsePerioder = lagGyldigUtsettelsePerioder();

        // Setter en av datoene i perioden til null
        utsettelsePerioder.get(0).setPeriodeFom(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerUtsettelse(utsettelsePerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_utsettelse_overlappende_perioder() {
        List<UtsettelseDto> utsettelsePerioder = lagGyldigUtsettelsePerioder();

        // Gjør perioder overlappende
        utsettelsePerioder.get(1).setPeriodeFom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerUtsettelse(utsettelsePerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER));
    }

    @Test
    public void skal_validere_utsettelse_start_for_sluttdato() {
        List<UtsettelseDto> utsettelsePerioder = lagGyldigUtsettelsePerioder();

        // Sett start før slutt
        utsettelsePerioder.get(1).setPeriodeTom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerUtsettelse(utsettelsePerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_utsettelse_årsak_må_være_satt() {
        List<UtsettelseDto> utsettelsePerioder = lagGyldigUtsettelsePerioder();

        // Sett start årsak til null
        utsettelsePerioder.get(1).setArsakForUtsettelse(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerUtsettelse(utsettelsePerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_utsettelse() {
        List<UtsettelseDto> utsettelsePerioder = lagGyldigUtsettelsePerioder();

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerUtsettelse(utsettelsePerioder);
        assertThat(feltFeil).isEmpty();
    }

    @Test
    public void skal_validere_gradering_dato_satt_til_null() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Setter en av datoene i perioden til null
        graderingPerioder.get(0).setPeriodeFom(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering_overlappende_perioder() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Gjør perioder overlappende
        graderingPerioder.get(1).setPeriodeFom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER));
    }

    @Test
    public void skal_validere_gradering_start_for_sluttdato() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Sett start før slutt
        graderingPerioder.get(1).setPeriodeTom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_gradering_prosentandel_må_være_satt() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Sett start årsak til null
        graderingPerioder.get(1).setProsentandelArbeid(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering_samtidig_uttak_samtidig_uttaksprosent_må_være_satt() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Sett start årsak til null
        graderingPerioder.get(1).setHarSamtidigUttak(true);
        graderingPerioder.get(1).setSamtidigUttaksprosent(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering_periode_må_være_satt() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        // Sett gradering periode til null
        graderingPerioder.get(1).setPeriodeForGradering(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering() {
        List<GraderingDto> graderingPerioder = lagGyldigGraderingPerioder();

        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerGradering(graderingPerioder);
        assertThat(feltFeil).isEmpty();
    }


    private List<PermisjonPeriodeDto> lagGyldigPermisjonPeriode(UttakPeriodeType type) {
        List<PermisjonPeriodeDto> permisjonsPerioder = new ArrayList<>();
        PermisjonPeriodeDto permisjonPeriode1 = new PermisjonPeriodeDto();
        permisjonPeriode1.setPeriodeFom(LocalDate.now());
        permisjonPeriode1.setPeriodeTom(LocalDate.now().plusWeeks(3));
        permisjonPeriode1.setPeriodeType(type);
        permisjonsPerioder.add(permisjonPeriode1);

        PermisjonPeriodeDto permisjonPeriode2 = new PermisjonPeriodeDto();
        permisjonPeriode2.setPeriodeFom(LocalDate.now().plusWeeks(3));
        permisjonPeriode2.setPeriodeTom(LocalDate.now().plusWeeks(5));
        permisjonPeriode2.setPeriodeType(type);
        permisjonsPerioder.add(permisjonPeriode2);

        return permisjonsPerioder;
    }

    private List<UtsettelseDto> lagGyldigUtsettelsePerioder() {
        List<UtsettelseDto> utsettelsePerioder = new ArrayList<>();

        UtsettelseDto utsettelsePeriode1 = new UtsettelseDto();
        utsettelsePeriode1.setPeriodeFom(LocalDate.now());
        utsettelsePeriode1.setPeriodeTom(LocalDate.now().plusWeeks(3));
        utsettelsePeriode1.setArsakForUtsettelse(UtsettelseÅrsak.ARBEID);
        utsettelsePeriode1.setPeriodeForUtsettelse(UttakPeriodeType.FELLESPERIODE);
        utsettelsePerioder.add(utsettelsePeriode1);

        UtsettelseDto utsettelsePeriode2 = new UtsettelseDto();
        utsettelsePeriode2.setPeriodeFom(LocalDate.now().plusWeeks(3));
        utsettelsePeriode2.setPeriodeTom(LocalDate.now().plusWeeks(5));
        utsettelsePeriode2.setArsakForUtsettelse(UtsettelseÅrsak.ARBEID);
        utsettelsePeriode2.setPeriodeForUtsettelse(UttakPeriodeType.FELLESPERIODE);
        utsettelsePerioder.add(utsettelsePeriode2);

        return utsettelsePerioder;
    }

    @Test
    public void skal_validere_at_frilansperioder_må_være_satt() {
        FrilansDto frilansDto = new FrilansDto();
        frilansDto.setHarSokerPeriodeMedFrilans(true);
        frilansDto.setPerioder(new ArrayList<>());
        List<FeltFeilDto> feltFeil = ManuellRegistreringForeldrepengerValidator.validerFrilans(frilansDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    private List<PermisjonPeriodeDto> lagGyldigFellesPerioder() {
        List<PermisjonPeriodeDto> fellesPerioder = new ArrayList<>();
        PermisjonPeriodeDto permisjonPeriode1 = new PermisjonPeriodeDto();
        permisjonPeriode1.setPeriodeFom(LocalDate.now());
        permisjonPeriode1.setPeriodeTom(LocalDate.now().plusWeeks(3));
        permisjonPeriode1.setMorsAktivitet(MorsAktivitet.ARBEID);
        permisjonPeriode1.setPeriodeType(UttakPeriodeType.FELLESPERIODE);
        fellesPerioder.add(permisjonPeriode1);

        PermisjonPeriodeDto fellesPeriode2 = new PermisjonPeriodeDto();
        fellesPeriode2.setPeriodeFom(LocalDate.now().plusWeeks(3));
        fellesPeriode2.setPeriodeTom(LocalDate.now().plusWeeks(5));
        fellesPeriode2.setMorsAktivitet(MorsAktivitet.INNLAGT);
        permisjonPeriode1.setPeriodeType(UttakPeriodeType.FELLESPERIODE);
        fellesPerioder.add(fellesPeriode2);

        return fellesPerioder;
    }

    private List<GraderingDto> lagGyldigGraderingPerioder() {
        List<GraderingDto> graderingPerioder = new ArrayList<>();
        GraderingDto graderingPeriode1 = new GraderingDto();
        graderingPeriode1.setPeriodeFom(LocalDate.now());
        graderingPeriode1.setPeriodeTom(LocalDate.now().plusWeeks(3));
        graderingPeriode1.setOrgNr("1234567890");
        graderingPeriode1.setProsentandelArbeid(BigDecimal.valueOf(20));
        graderingPeriode1.setPeriodeForGradering(UttakPeriodeType.MØDREKVOTE);
        graderingPeriode1.setSkalGraderes(true);
        graderingPeriode1.setHarSamtidigUttak(true);
        graderingPeriode1.setSamtidigUttaksprosent(BigDecimal.TEN);
        graderingPerioder.add(graderingPeriode1);

        GraderingDto graderingPeriode2 = new GraderingDto();
        graderingPeriode2.setPeriodeFom(LocalDate.now().plusWeeks(3));
        graderingPeriode2.setPeriodeTom(LocalDate.now().plusWeeks(5));
        graderingPeriode2.setOrgNr("2345678901");
        graderingPeriode2.setProsentandelArbeid(BigDecimal.valueOf(20));
        graderingPeriode2.setPeriodeForGradering(UttakPeriodeType.FELLESPERIODE);
        graderingPeriode2.setSkalGraderes(false);
        graderingPerioder.add(graderingPeriode2);

        return graderingPerioder;
    }


}
