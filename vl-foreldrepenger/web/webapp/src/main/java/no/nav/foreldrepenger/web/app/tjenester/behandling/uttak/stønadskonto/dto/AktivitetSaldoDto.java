package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto;

import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetIdentifikatorDto;

public class AktivitetSaldoDto {

    private final AktivitetIdentifikatorDto aktivitetIdentifikator;
    private final int saldo;

    public AktivitetSaldoDto(AktivitetIdentifikatorDto aktivitetIdentifikator, int saldo) {
        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.saldo = saldo;
    }

    public AktivitetIdentifikatorDto getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }

    public int getSaldo() {
        return saldo;
    }
}
