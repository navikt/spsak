package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.abonnent.tps.FødselHendelseDto;

public class FødselHendelseDtoBuilder {

    private String id = "123";
    private List<AktørId> aktørIdForeldre;
    private LocalDate fødselsdato;


    public static FødselHendelseDtoBuilder builder() {
        return new FødselHendelseDtoBuilder();
    }

    public FødselHendelseDtoBuilder medForelder(AktørId aktørId) {
        aktørIdForeldre = Collections.singletonList(aktørId);
        return this;
    }

    public FødselHendelseDtoBuilder medFødselsdato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato;
        return this;
    }

    public FødselHendelseDto build() {
        Objects.requireNonNull(aktørIdForeldre);
        Objects.requireNonNull(fødselsdato);

        FødselHendelseDto dto = new FødselHendelseDto();
        dto.setId(id);
        dto.setAktørIdForeldre(aktørIdForeldre.stream()
            .map(AktørId::getId)
            .collect(toList()));
        dto.setFødselsdato(fødselsdato);
        return dto;
    }
}
