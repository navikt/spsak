package no.nav.foreldrepenger.kontrakter.fordel;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class JournalpostFerdigstiltDto implements AbacDto {

    @Digits(integer = 18, fraction = 0)
    private String saksnummer;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    private UUID forsendelseId;

    public JournalpostFerdigstiltDto(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public void setForsendelseId(UUID forsendelseId) {
        this.forsendelseId = forsendelseId;
    }

    public Optional<UUID> getForsendelseId() {
        return Optional.ofNullable(this.forsendelseId);
    }

    private JournalpostFerdigstiltDto() { //For Jackson
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett().leggTilJournalPostId(journalpostId, false);
        if (saksnummer != null) {
            abacDataAttributter.leggTilSaksnummer(saksnummer);
        }
        return abacDataAttributter;
    }

}
