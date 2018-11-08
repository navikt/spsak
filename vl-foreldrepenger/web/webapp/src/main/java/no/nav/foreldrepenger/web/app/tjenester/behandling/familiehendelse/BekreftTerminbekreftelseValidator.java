package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.exceptions.Valideringsfeil;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BekreftTerminbekreftelseValidator {

    private Period tidlistUtstedelseAvTerminBekreftelse;

    static final String UTSTEDTDATO_TERMINBEKREFTELSE_IFT_TERMINDATO = "Terminbekreftelse må være utstedt senest %d uker og %d dager før termindato";

    @SuppressWarnings("unused")
    private BekreftTerminbekreftelseValidator() {
        // for CDI
    }

    @Inject
    public BekreftTerminbekreftelseValidator(
            @KonfigVerdi(value = "terminbekreftelse.tidligst.utstedelse.før.termin") Period tidlistUtstedelseAvTerminBekreftelse) {
        this.tidlistUtstedelseAvTerminBekreftelse = tidlistUtstedelseAvTerminBekreftelse;
    }

    public void validerOpplysninger(BekreftTerminbekreftelseAksjonspunktDto dto) {
        List<FeltFeilDto> funnetFeil = Stream.of(validerUtstedtdato(dto))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!funnetFeil.isEmpty()) {
            throw new Valideringsfeil(funnetFeil);
        }
    }

    Optional<FeltFeilDto> validerUtstedtdato(BekreftTerminbekreftelseAksjonspunktDto dto) {
        String feltnavn = "utstedtdato";
        LocalDate utstedtdato = dto.getUtstedtdato();
        LocalDate termindato = dto.getTermindato();
        if (Objects.nonNull(termindato) && Objects.nonNull(utstedtdato) &&
                !utstedtdato.isAfter(termindato.minus(tidlistUtstedelseAvTerminBekreftelse))) {
            return Optional.of(new FeltFeilDto(feltnavn, lagMelding()));
        }
        return Optional.empty();
    }

    private String lagMelding() {
        int dager = tidlistUtstedelseAvTerminBekreftelse.getDays() % 7;
        int uker = (tidlistUtstedelseAvTerminBekreftelse.getDays() - dager) / 7;
        return String.format(UTSTEDTDATO_TERMINBEKREFTELSE_IFT_TERMINDATO, uker, dager - 1);
    }
}
