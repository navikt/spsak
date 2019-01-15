package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto;

public class BeregningsresultatEngangsstønadDto {

    private Long beregnetTilkjentYtelse;
    private Long satsVerdi;
    private Integer antallBarn;

    public BeregningsresultatEngangsstønadDto(Long beregnetTilkjentYtelse, Long satsVerdi, Integer antallBarn) {
        this.beregnetTilkjentYtelse = beregnetTilkjentYtelse;
        this.satsVerdi = satsVerdi;
        this.antallBarn = antallBarn;
    }

    public BeregningsresultatEngangsstønadDto() {
    }

    public void setBeregnetTilkjentYtelse(Long beregnetTilkjentYtelse) {
        this.beregnetTilkjentYtelse = beregnetTilkjentYtelse;
    }

    public void setSatsVerdi(Long satsVerdi) {
        this.satsVerdi = satsVerdi;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    public Long getBeregnetTilkjentYtelse() {
        return beregnetTilkjentYtelse;
    }

    public Long getSatsVerdi() {
        return satsVerdi;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

}
