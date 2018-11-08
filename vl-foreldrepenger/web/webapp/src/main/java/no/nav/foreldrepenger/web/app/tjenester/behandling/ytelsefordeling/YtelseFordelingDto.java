package no.nav.foreldrepenger.web.app.tjenester.behandling.ytelsefordeling;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.PeriodeDto;

public class YtelseFordelingDto {
    private List<PeriodeDto> ikkeOmsorgPerioder;
    private List<PeriodeDto> aleneOmsorgPerioder;
    private LocalDate endringsDato;
    private LocalDate førsteUttaksDato;

    private YtelseFordelingDto() {
    }

    public List<PeriodeDto> getIkkeOmsorgPerioder() {
        return ikkeOmsorgPerioder;
    }

    public List<PeriodeDto> getAleneOmsorgPerioder() {
        return aleneOmsorgPerioder;
    }

    public LocalDate getEndringsDato() {
        return endringsDato;
    }

    public LocalDate getFørsteUttaksDato() {
        return førsteUttaksDato;
    }

    public static class Builder {

        private final YtelseFordelingDto kladd = new YtelseFordelingDto();

        public Builder medIkkeOmsorgPerioder(List<PeriodeDto> ikkeOmsorgPerioder) {
            kladd.ikkeOmsorgPerioder = ikkeOmsorgPerioder;
            return this;
        }

        public Builder medAleneOmsorgPerioder(List<PeriodeDto> aleneOmsorgPerioder) {
            kladd.aleneOmsorgPerioder = aleneOmsorgPerioder;
            return this;
        }

        public Builder medEndringsDato(LocalDate endringsDato) {
            kladd.endringsDato = endringsDato;
            return this;
        }

        public Builder medFørsteUttaksDato(LocalDate førsteUttaksDato) {
            kladd.førsteUttaksDato = førsteUttaksDato;
            return this;
        }

        public YtelseFordelingDto build() {
            return kladd;
        }

    }
}
