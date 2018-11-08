package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto;

import java.util.List;

public class StønadskontoDto {

    private final String stonadskontotype;
    private final int maxDager;
    private final int saldo;
    private final List<AktivitetSaldoDto> aktivitetSaldoDtoList;

    public StønadskontoDto(String stønadskontotype, int maxDager, int saldo, List<AktivitetSaldoDto> aktivitetSaldoDtoList) {
        this.stonadskontotype = stønadskontotype;
        this.maxDager = maxDager;
        this.saldo = saldo;
        this.aktivitetSaldoDtoList = aktivitetSaldoDtoList;
    }

    public String getStonadskontotype() {
        return stonadskontotype;
    }

    public int getMaxDager() {
        return maxDager;
    }

    public int getSaldo() {
        return saldo;
    }

    public List<AktivitetSaldoDto> getAktivitetSaldoDtoList() {
        return aktivitetSaldoDtoList;
    }

}
