package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class SaldoerDto {
    private final Optional<LocalDate> maksDatoUttak;

    private final Map<String, StønadskontoDto> stonadskontoer;

    public SaldoerDto(Optional<LocalDate> maksDatoUttak, Map<String, StønadskontoDto> stonadskontoer) {
        this.maksDatoUttak = maksDatoUttak;
        this.stonadskontoer = stonadskontoer;
    }

    public Optional<LocalDate> getMaksDatoUttak() {
        return maksDatoUttak;
    }

    public Map<String, StønadskontoDto> getStonadskontoer() {
        return stonadskontoer;
    }
}
