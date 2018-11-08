package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling;

public class Behandlingsstatus {

    private String behandlingsId;
    private String behandlingsTypeKode; //Kodeverk. http://nav.no/kodeverk/Kodeverk/Behandlingstyper
    private String sakstemaKode;
    private String aktørId;
    private String ansvarligEnhetRef;

    public String getBehandlingsId() {
        return behandlingsId;
    }

    public void setBehandlingsId(String behandlingsId) {
        this.behandlingsId = behandlingsId;
    }

    public String getBehandlingsTypeKode() {
        return behandlingsTypeKode;
    }

    public void setBehandlingsTypeKode(String behandlingsTypeKode) {
        this.behandlingsTypeKode = behandlingsTypeKode;
    }

    public String getSakstemaKode() {
        return sakstemaKode;
    }

    public void setSakstemaKode(String sakstemaKode) {
        this.sakstemaKode = sakstemaKode;
    }

    public String getAktørId() {
        return aktørId;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public String getAnsvarligEnhetRef() {
        return ansvarligEnhetRef;
    }

    public void setAnsvarligEnhetRef(String ansvarligEnhetRef) {
        this.ansvarligEnhetRef = ansvarligEnhetRef;
    }
}
