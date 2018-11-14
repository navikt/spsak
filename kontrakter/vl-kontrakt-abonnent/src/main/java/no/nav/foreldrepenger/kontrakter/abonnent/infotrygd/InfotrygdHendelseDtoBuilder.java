package no.nav.foreldrepenger.kontrakter.abonnent.infotrygd;

import java.time.LocalDate;
import java.util.Objects;

public class InfotrygdHendelseDtoBuilder {
    private InfotrygdHendelseDto dto;

    private InfotrygdHendelseDtoBuilder(InfotrygdHendelseDto dto) {
        this.dto = dto;
    }

    public static InfotrygdHendelseDtoBuilder annulert() {
        return new InfotrygdHendelseDtoBuilder(new InfotrygdHendelseDto(InfotrygdHendelseDto.Hendelsetype.YTELSE_ANNULERT));
    }

    public static InfotrygdHendelseDtoBuilder endring() {
        return new InfotrygdHendelseDtoBuilder(new InfotrygdHendelseDto(InfotrygdHendelseDto.Hendelsetype.YTELSE_ENDRET));
    }

    public static InfotrygdHendelseDtoBuilder innvilget() {
        return new InfotrygdHendelseDtoBuilder(new InfotrygdHendelseDto(InfotrygdHendelseDto.Hendelsetype.YTELSE_INNVILGET));
    }

    public static InfotrygdHendelseDtoBuilder opphørt() {
        return new InfotrygdHendelseDtoBuilder(new InfotrygdHendelseDto(InfotrygdHendelseDto.Hendelsetype.YTELSE_OPPHØRT));
    }

    public InfotrygdHendelseDtoBuilder medUnikId(String id) {
        this.dto.setId(id);
        return this;
    }

    public InfotrygdHendelseDtoBuilder medAktørId(String aktørId) {
        this.dto.setAktørId(aktørId);
        return this;
    }

    public InfotrygdHendelseDtoBuilder medFraOgMed(LocalDate fom) {
        this.dto.setFom(fom);
        return this;
    }

    public InfotrygdHendelseDtoBuilder medTypeYtelse(String typeYtelse) {
        this.dto.setTypeYtelse(typeYtelse);
        return this;
    }

    public InfotrygdHendelseDtoBuilder medIdentdato(String identdato) {
        this.dto.setIdentdato(identdato);
        return this;
    }

    public InfotrygdHendelseDto build() {
        verifyStateForBuild();
        return dto;
    }

    private void verifyStateForBuild() {
        Objects.requireNonNull(dto);
        Objects.requireNonNull(dto.getAktørId());
        Objects.requireNonNull(dto.getTypeYtelse());
        Objects.requireNonNull(dto.getFom());
        Objects.requireNonNull(dto.getIdentdato());
        Objects.requireNonNull(dto.getId());
    }
}
