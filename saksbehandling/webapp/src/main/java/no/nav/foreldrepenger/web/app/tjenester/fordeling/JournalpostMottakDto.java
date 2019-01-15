package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;
import no.nav.vedtak.util.StringUtils;

public class JournalpostMottakDto implements AbacDto {

    private static final int PAYLOAD_MAX_CHARS = 12000;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String saksnummer;

    @NotNull
    @Digits(integer = 18, fraction = 0)
    private String journalpostId;

    private UUID forsendelseId;

    @NotNull
    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String behandlingstemaOffisiellKode;

    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String dokumentTypeIdOffisiellKode;

    private LocalDate forsendelseMottatt;

    @Size(max = 25)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String dokumentKategoriOffisiellKode;

    @Size(max = 5)
    private String journalForendeEnhet;

    @JsonProperty("payload")
    @Pattern(regexp = InputValideringRegex.BASE64_RFC4648_URLSAFE_WITH_PADDING)
    @Size(max = PAYLOAD_MAX_CHARS * 2) //Gir plass til 50% flere byte enn characters, det bør holde
    private String base64EncodedPayload;

    /**
     * Siden XML'en encodes før overføring må lengden på XML'en lagres som en separat property for å kunne valideres.
     * Lengden er basert på at MOTTAT_DOKUMENT.XML_PAYLOAD ern en VARCHAR2(4000)
     */
    @JsonProperty("payloadLength")
    @Max(PAYLOAD_MAX_CHARS)
    @Min(1)
    private Integer payloadLength;

    public JournalpostMottakDto(String saksnummer, String journalpostId, String behandlingstemaOffisiellKode, String dokumentTypeIdOffisiellKode, LocalDate forsendelseMottatt, String payload) {
        this.saksnummer = saksnummer;
        this.journalpostId = journalpostId;
        this.behandlingstemaOffisiellKode = behandlingstemaOffisiellKode;
        this.dokumentTypeIdOffisiellKode = dokumentTypeIdOffisiellKode;
        this.forsendelseMottatt = forsendelseMottatt;
        if (!StringUtils.nullOrEmpty(payload)) {
            byte[] bytes = payload.getBytes(Charset.forName("UTF-8"));
            this.payloadLength = payload.length();
            this.base64EncodedPayload = Base64.getUrlEncoder().encodeToString(bytes);
        }
    }

    JournalpostMottakDto() { //For Jackson
    }

    static Optional<String> getPayloadValiderLengde(String base64EncodedPayload, Integer deklarertLengde) {
        if (base64EncodedPayload == null) {
            return Optional.empty();
        }
        if (deklarertLengde == null) {
            throw JournalpostMottakFeil.FACTORY.manglerPayloadLength().toException();
        }
        byte[] bytes = Base64.getDecoder().decode(base64EncodedPayload);
        String streng = new String(bytes, Charset.forName("UTF-8"));
        if (streng.length() != deklarertLengde) {
            throw JournalpostMottakFeil.FACTORY.feilPayloadLength(deklarertLengde, streng.length()).toException();
        }
        return Optional.of(streng);
    }

    public Optional<UUID> getForsendelseId() {
        return Optional.ofNullable(this.forsendelseId);
    }

    public void setForsendelseId(UUID forsendelseId) {
        this.forsendelseId = forsendelseId;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getBehandlingstemaOffisiellKode() {
        return behandlingstemaOffisiellKode;
    }

    public Optional<String> getDokumentTypeIdOffisiellKode() {
        return Optional.ofNullable(dokumentTypeIdOffisiellKode);
    }

    public Optional<LocalDate> getForsendelseMottatt() {
        return Optional.ofNullable(forsendelseMottatt);
    }

    public String getDokumentKategoriOffisiellKode() {
        return dokumentKategoriOffisiellKode;
    }

    public void setDokumentKategoriOffisiellKode(String dokumentKategoriOffisiellKode) {
        this.dokumentKategoriOffisiellKode = dokumentKategoriOffisiellKode;
    }

    public String getJournalForendeEnhet() {
        return journalForendeEnhet;
    }

    public void setJournalForendeEnhet(String journalForendeEnhet) {
        this.journalForendeEnhet = journalForendeEnhet;
    }

    public void setDokumentTypeIdOffisiellKode(String dokumentTypeIdOffisiellKode) {
        this.dokumentTypeIdOffisiellKode = dokumentTypeIdOffisiellKode;
    }

    @JsonIgnore
    public Optional<String> getPayload() {
        return getPayloadValiderLengde(base64EncodedPayload, payloadLength);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilSaksnummer(saksnummer);
    }

}
