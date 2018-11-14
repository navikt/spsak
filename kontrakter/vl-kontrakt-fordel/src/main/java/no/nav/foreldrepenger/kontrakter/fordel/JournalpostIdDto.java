package no.nav.foreldrepenger.kontrakter.fordel;

import javax.validation.constraints.Digits;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class JournalpostIdDto implements AbacDto {

    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    public JournalpostIdDto(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public JournalpostIdDto() {  // For Jackson
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilJournalPostId(journalpostId, false);
    }

    public String getJournalpostId() {
        return journalpostId;
    }
}
