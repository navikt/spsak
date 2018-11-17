package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import java.util.Optional;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class OpprettSakDto implements AbacDto {

    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    @NotNull
    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String behandlingstemaOffisiellKode;

    @NotNull
    @Digits(integer = 19, fraction = 0)
    private String aktørId;

    public OpprettSakDto(String journalpostId, String behandlingstemaOffisiellKode, String aktørId) {
        this.journalpostId = journalpostId;
        this.behandlingstemaOffisiellKode = behandlingstemaOffisiellKode;
        this.aktørId = aktørId;
    }

    OpprettSakDto() { //For Jackson
    }

    public Optional<String> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public String getBehandlingstemaOffisiellKode() {
        return behandlingstemaOffisiellKode;
    }

    public String getAktørId() {
        return aktørId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilAktørId(aktørId);
    }
}
