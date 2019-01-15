package no.nav.foreldrepenger.web.app.tjenester.dokument.dto;

import javax.validation.constraints.Digits;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class JournalpostIdDto implements AbacDto {

    @JsonProperty("journalpostId")
    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    public JournalpostIdDto(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilJournalPostId(journalpostId, true);
    }

    public String getJournalpostId() {
        return journalpostId;
    }
}
