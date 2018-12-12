package no.nav.foreldrepenger.autotest.sykepenger.modell;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SykepengesøknadWrapper {

    @JsonProperty
    private String journalpostId;

    @JsonProperty
    private String aktørId;

    @JsonProperty
    private String saksnummer;

    @JsonProperty
    private String payload;

    @JsonProperty
    private Integer payloadLength;

    public SykepengesøknadWrapper(String journalpostId, String aktørId, String saksnummer, String payload, Integer payloadLength) {
        this.journalpostId = journalpostId;
        this.aktørId = aktørId;
        this.saksnummer = saksnummer;
        this.payload = payload;
        this.payloadLength = payloadLength;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getAktørId() {
        return aktørId;
    }

    public Integer getPayloadLength() {
        return payloadLength;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
