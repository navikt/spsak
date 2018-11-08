package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;

public class ManuellRegistreringEndringssøknadValidatorTest {

    ManuellRegistreringEndringsøknadDto registreringDto;

    @Before
    public void setup() {
        registreringDto = new ManuellRegistreringEndringsøknadDto();
    }
    @Test
    public void skal_validere_gyldig_fellesperiode() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        List<FeltFeilDto> feltFeilDtos = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeilDtos).hasSize(0);
    }

    @Test
    public void skal_validere_fellesperiode_start_for_sluttdato() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setPermisjonsPerioder(lagGyldigFellesPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett start før slutt
        permisjon.getPermisjonsPerioder().get(1).setPeriodeTom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).isNotEmpty();
        assertThat(feltFeil).anySatisfy(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_gradering_dato_satt_til_null() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Setter en av datoene i perioden til null
        permisjon.getGraderingPeriode().get(0).setPeriodeFom(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering_overlappende_perioder() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Gjør perioder overlappende
        permisjon.getGraderingPeriode().get(1).setPeriodeFom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.OVERLAPPENDE_PERIODER));
    }

    @Test
    public void skal_validere_gradering_start_for_sluttdato() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett start før slutt
        permisjon.getGraderingPeriode().get(1).setPeriodeTom(LocalDate.now());

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.STARTDATO_FØR_SLUTTDATO));
    }

    @Test
    public void skal_validere_gradering_prosentandel_må_være_satt() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett start årsak til null
        permisjon.getGraderingPeriode().get(1).setProsentandelArbeid(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering_periode_må_være_satt() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett gradering periode til null
        permisjon.getGraderingPeriode().get(1).setPeriodeForGradering(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_gradering() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setGraderingPeriode(lagGyldigGraderingPerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).isEmpty();
    }

    @Test
    public void skal_validere_gyldig_utsettelse() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setUtsettelsePeriode(lagGyldigUtsettelsePerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett start årsak til null
        permisjon.getUtsettelsePeriode().get(1).setArsakForUtsettelse(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
        assertThat(feltFeil).hasSize(1);
        assertThat(feltFeil).first().satisfies(ff -> ff.getMelding().equals(ManuellRegistreringValidatorTekster.PAAKREVD_FELT));
    }

    @Test
    public void skal_validere_utsettelse_årsak_må_være_satt() {
        TidsromPermisjonDto permisjon = new TidsromPermisjonDto();
        permisjon.setUtsettelsePeriode(lagGyldigUtsettelsePerioder());
        registreringDto.setTidsromPermisjon(permisjon);

        // Sett start årsak til null
        permisjon.getUtsettelsePeriode().get(1).setArsakForUtsettelse(null);

        List<FeltFeilDto> feltFeil = ManuellRegistreringEndringssøknadValidator.validerOpplysninger(registreringDto);
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

    private List<UtsettelseDto> lagGyldigUtsettelsePerioder() {
        List<UtsettelseDto> utsettelsePerioder = new ArrayList<>();

        UtsettelseDto utsettelsePeriode1 = new UtsettelseDto();
        utsettelsePeriode1.setPeriodeFom(LocalDate.now());
        utsettelsePeriode1.setPeriodeTom(LocalDate.now().plusWeeks(3));
        utsettelsePeriode1.setArsakForUtsettelse(UtsettelseÅrsak.ARBEID);
        utsettelsePeriode1.setPeriodeForUtsettelse(UttakPeriodeType.FELLESPERIODE);
        utsettelsePeriode1.setErArbeidstaker(true);
        utsettelsePeriode1.setOrgNr("1234567890");
        utsettelsePerioder.add(utsettelsePeriode1);

        UtsettelseDto utsettelsePeriode2 = new UtsettelseDto();
        utsettelsePeriode2.setPeriodeFom(LocalDate.now().plusWeeks(3));
        utsettelsePeriode2.setPeriodeTom(LocalDate.now().plusWeeks(5));
        utsettelsePeriode2.setArsakForUtsettelse(UtsettelseÅrsak.ARBEID);
        utsettelsePeriode2.setPeriodeForUtsettelse(UttakPeriodeType.FELLESPERIODE);
        utsettelsePeriode2.setErArbeidstaker(true);
        utsettelsePeriode2.setOrgNr("2345678901");
        utsettelsePerioder.add(utsettelsePeriode2);

        return utsettelsePerioder;
    }
}
