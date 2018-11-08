package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.KontrollerFaktaPeriode;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AvklarFaktaUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.BekreftetUttakPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.KontrollerFaktaPeriodeDto;

public class AvklarFaktaUttakValidatorTest {

    public static final LocalDate IDAG = LocalDate.now();

    @Test
    public void skal_validere_finnes_ingen_søknadsperiode() {
        List<OppgittPeriode> gjeldendeFordeling = Arrays.asList(
            OppgittPeriodeBuilder.ny().medPeriode(IDAG.minusDays(10), IDAG).medPeriodeType(UttakPeriodeType.FELLESPERIODE).build()
        );
        AvklarFaktaUttakDto dto = new AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto();

        Optional<FeltFeilDto> feltFeil = AvklarFaktaUttakValidator.validerSøknadsperioder(dto.getBekreftedePerioder(), gjeldendeFordeling, Optional.empty());
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals("Påkrevd minst en søknadsperiode"));
    }

    @Test
    public void skal_validere_endre_søknadsperiode_startdato() {
        List<OppgittPeriode> gjeldendeFordeling = Arrays.asList(
            OppgittPeriodeBuilder.ny().medPeriode(IDAG.minusDays(10), IDAG).medPeriodeType(UttakPeriodeType.FELLESPERIODE).build()
        );

        AvklarFaktaUttakDto dto = new AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(11), LocalDate.now());
        dto.setBekreftedePerioder(Arrays.asList(bekreftetUttakPeriodeDto));

        Optional<FeltFeilDto> feltFeil = AvklarFaktaUttakValidator.validerSøknadsperioder(dto.getBekreftedePerioder(), gjeldendeFordeling, Optional.empty());
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals("Startdato på søknadsperiode kan ikke endres"));
    }

    @Test
    public void skal_validere_overlappende_perioder() {
        List<OppgittPeriode> gjeldendeFordeling = Arrays.asList(
            OppgittPeriodeBuilder.ny().medPeriode(IDAG.minusDays(20), IDAG).medPeriodeType(UttakPeriodeType.FELLESPERIODE).build()
        );

        AvklarFaktaUttakDto dto = new AvklarFaktaUttakDto.AvklarFaktaUttakPerioderDto();
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto_1 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(20), LocalDate.now().minusDays(10));
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto_2 = getBekreftetUttakPeriodeDto(LocalDate.now().minusDays(11), LocalDate.now());
        dto.setBekreftedePerioder(Arrays.asList(bekreftetUttakPeriodeDto_1, bekreftetUttakPeriodeDto_2));

        Optional<FeltFeilDto> feltFeil = AvklarFaktaUttakValidator.validerSøknadsperioder(dto.getBekreftedePerioder(), gjeldendeFordeling, Optional.empty());
        assertThat(feltFeil).isPresent();
        assertThat(feltFeil).hasValueSatisfying(ff -> ff.getMelding().equals("Periodene må ikke overlappe"));
    }

    private BekreftetUttakPeriodeDto getBekreftetUttakPeriodeDto(LocalDate fom, LocalDate tom) {
        BekreftetUttakPeriodeDto bekreftetUttakPeriodeDto = new BekreftetUttakPeriodeDto();
        OppgittPeriode bekreftetperiode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE)
            .build();
        bekreftetUttakPeriodeDto.setBekreftetPeriode(new KontrollerFaktaPeriodeDto(KontrollerFaktaPeriode.ubekreftet(bekreftetperiode)));
        return bekreftetUttakPeriodeDto;
    }

}
