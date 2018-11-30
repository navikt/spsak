package no.nav.foreldrepenger.autotest.sykepenger.modell;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InntektsmeldingWrapper {

    @JsonProperty
    private String journalpostId;

    @JsonProperty
    private String aktørId;

    @JsonProperty
    private Long saksnummer;

    @JsonProperty
    private String payload;

    @JsonProperty
    private Integer payloadLength;

    public InntektsmeldingWrapper(String journalpostId, String aktørId, Long saksnummer, String payload, Integer payloadLength) {
        this.journalpostId = journalpostId;
        this.aktørId = aktørId;
        this.saksnummer = saksnummer;
        this.payload = payload;
        this.payloadLength = payloadLength;
    }

    public Integer getPayloadLength() {
        return payloadLength;
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

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public Long getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
