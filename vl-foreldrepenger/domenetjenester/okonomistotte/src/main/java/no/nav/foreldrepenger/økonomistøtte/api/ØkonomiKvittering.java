package no.nav.foreldrepenger.økonomistøtte.api;

public class ØkonomiKvittering {

    private Long behandlingId;
    private Long fagsystemId;
    private String meldingKode;
    private String alvorlighetsgrad;
    private String beskrMelding;


    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getFagsystemId() {
        return fagsystemId;
    }

    public String getAlvorlighetsgrad() {
        return alvorlighetsgrad;
    }

    public String getMeldingKode() {
        return meldingKode;
    }

    public String getBeskrMelding() {
        return beskrMelding;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public void setFagsystemId(Long fagsystemId) {
        this.fagsystemId = fagsystemId;
    }

    public void setMeldingKode(String meldingKode) {
        this.meldingKode = meldingKode;
    }

    public void setAlvorlighetsgrad(String alvorlighetsgrad) {
        this.alvorlighetsgrad = alvorlighetsgrad;
    }

    public void setBeskrMelding(String beskrMelding) {
        this.beskrMelding = beskrMelding;
    }

}
