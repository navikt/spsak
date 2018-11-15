package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class JournalpostFerdigstiltDto implements AbacDto {

    private static final int PAYLOAD_MAX_CHARS = 12000;

    @Digits(integer = 18, fraction = 0)
    private String saksnummer;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    private UUID forsendelseId;

    /**
     * Siden XML'en encodes før overføring må lengden på XML'en lagres som en separat property for å kunne valideres.
     * Lengden er basert på at MOTTAT_DOKUMENT.XML_PAYLOAD ern en VARCHAR2(4000)
     */
    @JsonProperty("payloadLength")
    @Max(PAYLOAD_MAX_CHARS)
    @Min(1)
    private Integer payloadLength;

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
