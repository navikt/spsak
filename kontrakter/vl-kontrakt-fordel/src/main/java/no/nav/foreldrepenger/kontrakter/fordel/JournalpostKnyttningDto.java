package no.nav.foreldrepenger.kontrakter.fordel;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class JournalpostKnyttningDto implements AbacDto {
    @Valid
    @JsonProperty
    private SaksnummerDto saksnummerDto;
    @Valid
    @JsonProperty
    private JournalpostIdDto journalpostIdDto;

    public JournalpostKnyttningDto(String saksnummer, String journalpostId) {
        this(new SaksnummerDto(saksnummer), new JournalpostIdDto(journalpostId));
    }

    public JournalpostKnyttningDto(SaksnummerDto saksnummerDto, JournalpostIdDto journalpostIdDto) {
        this.saksnummerDto = saksnummerDto;
        this.journalpostIdDto = journalpostIdDto;
    }

    public JournalpostKnyttningDto() {  // For Jackson
    }

    @JsonIgnore
    public String getSaksnummer() {
        return saksnummerDto.getSaksnummer();
    }

    @JsonIgnore
    public String getJournalpostId() {
        return journalpostIdDto.getJournalpostId();
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = saksnummerDto.abacAttributter();
        abacDataAttributter.leggTil(journalpostIdDto.abacAttributter());
        return abacDataAttributter;
    }
}
