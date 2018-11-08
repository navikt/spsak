package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseValidator;

public class BekreftTerminbekreftelseValidatorTest {

    private static final String FEILMELDING = "Terminbekreftelse må være utstedt senest 14 uker og 3 dager før termindato";
    private static Period tidlistUtstedelseAvTerminBekreftelse = Period.parse("P14W4D");
    private static BekreftTerminbekreftelseValidator validator;

    @BeforeClass
    public static void setup() {
        validator = new BekreftTerminbekreftelseValidator(tidlistUtstedelseAvTerminBekreftelse);
    }

    @Test
    public void skal_ikke_validare_ok_når_utstedtdato_er_før_26_svangerskapsuke() {
        String forventetFeltnavn = "utstedtdato";
        LocalDate utstedtdato = LocalDate.now().minusWeeks(14).minusDays(4);
        LocalDate termindato = LocalDate.now();
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto("begrunnelse", termindato, utstedtdato,
                1);
        Optional<FeltFeilDto> feltFeil = validator.validerUtstedtdato(dto);
        assertThat(feltFeil).as(FEILMELDING).isPresent();
        assertThat(feltFeil.get().getNavn()).isEqualTo(forventetFeltnavn);
        assertThat(feltFeil.get().getMelding()).as(FEILMELDING)
            .isEqualTo(FEILMELDING);

    }

    @Test
    public void skal_validare_ok_når_utstedtdato_er_14_uker_og_3_dager_før_termindato() {
        LocalDate utstedtdato = LocalDate.now().minusWeeks(14).minusDays(3);
        LocalDate termindato = LocalDate.now();
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto("begrunnelse", termindato, utstedtdato,
                1);

        Optional<FeltFeilDto> feltFeil = validator.validerUtstedtdato(dto);
        assertThat(feltFeil).as(FEILMELDING).isNotPresent();

    }

    @Test
    public void skal_validare_ok_når_utstedtdato_er_14_uker_og_2_dager_før_termindato() {
        LocalDate utstedtdato = LocalDate.now().minusWeeks(14).minusDays(2);
        LocalDate termindato = LocalDate.now();
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto("begrunnelse", termindato, utstedtdato,
                1);

        Optional<FeltFeilDto> feltFeil = validator.validerUtstedtdato(dto);
        assertThat(feltFeil).as(FEILMELDING).isNotPresent();
    }
}
