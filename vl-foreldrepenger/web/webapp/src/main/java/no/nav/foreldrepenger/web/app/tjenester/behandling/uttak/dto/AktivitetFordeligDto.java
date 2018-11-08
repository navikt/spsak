package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

public class AktivitetFordeligDto {

    private final AktivitetIdentifikatorDto aktivitetIdentifikator;
    private final int fordelteDager;

    AktivitetFordeligDto(AktivitetIdentifikatorDto aktivitetIdentifikator, int fordelteDager) {
        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.fordelteDager = fordelteDager;
    }

    public AktivitetIdentifikatorDto getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }

    public int getFordelteDager() {
        return fordelteDager;
    }
}
