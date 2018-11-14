package no.nav.foreldrepenger.kontrakter.abonnent.infotrygd;

import java.time.LocalDate;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.kontrakter.abonnent.HendelseDto;
import no.nav.vedtak.util.InputValideringRegex;

/**
 * Dto for hendelser fra infotrygd.
 * For å bruke skal denne legges inn i HendelseWrapperDto gjennom HendelseWrapperDto.lagDto
 * Kan bygges med InfotrygdHendelseDtoBuilder
 */
public class InfotrygdHendelseDto extends HendelseDto {

    public static final String AVSENDER = "infotrygd";

    public enum Hendelsetype {
        YTELSE_ANNULERT,
        YTELSE_ENDRET,
        YTELSE_INNVILGET,
        YTELSE_OPPHØRT
    }

    @NotNull
    @Digits(integer = 19, fraction = 0)
    private String aktørId;

    @NotNull
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(min =2, max = 20)
    private String hendelsetype;

    @NotNull
    @Size(min = 2, max = 2)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String typeYtelse;

    @NotNull
    private LocalDate fom;

    @NotNull
    @Digits(integer = 8, fraction = 0)
    private String identdato;

    public InfotrygdHendelseDto() {
        // Jackson
    }

    public InfotrygdHendelseDto(Hendelsetype hendelsetype) {
        this.hendelsetype = hendelsetype.name();
    }

    void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    void setHendelsetype(String hendelsetype) {
        this.hendelsetype = hendelsetype;
    }

    void setTypeYtelse(String typeYtelse) {
        this.typeYtelse = typeYtelse;
    }

    void setFom(LocalDate fom) {
        this.fom = fom;
    }

    void setIdentdato(String identdato) {
        this.identdato = identdato;
    }

    public String getAktørId() {
        return aktørId;
    }

    public String getTypeYtelse() {
        return typeYtelse;
    }

    public LocalDate getFom() {
        return fom;
    }

    public String getIdentdato() {
        return identdato;
    }

    @Override
    public String getAvsenderSystem() {
        return AVSENDER;
    }

    @Override
    public String getHendelsetype() {
        return hendelsetype;
    }
}
