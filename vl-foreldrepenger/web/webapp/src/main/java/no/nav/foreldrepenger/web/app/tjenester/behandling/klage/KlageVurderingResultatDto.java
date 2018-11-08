package no.nav.foreldrepenger.web.app.tjenester.behandling.klage;


import java.time.LocalDate;

public class KlageVurderingResultatDto {
    private String klageVurdering;
    private String begrunnelse;
    private String klageAvvistArsak;
    private String klageAvvistArsakNavn;
    private String klageMedholdArsak;
    private String klageMedholdArsakNavn;
    private String klageVurdertAv;
    private LocalDate vedtaksdatoPaklagdBehandling;

    public KlageVurderingResultatDto() {
    }

    public String getKlageVurdering() {
        return klageVurdering;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public String getKlageAvvistArsak() {
        return klageAvvistArsak;
    }

    public String getKlageMedholdArsak() {
        return klageMedholdArsak;
    }

    public String getKlageVurdertAv() {
        return klageVurdertAv;
    }

    public String getKlageAvvistArsakNavn() {
        return klageAvvistArsakNavn;
    }

    public String getKlageMedholdArsakNavn() {
        return klageMedholdArsakNavn;
    }

    public LocalDate getVedtaksdatoPaklagdBehandling() {
        return vedtaksdatoPaklagdBehandling;
    }

    void setKlageVurdering(String klageVurdering) {
        this.klageVurdering = klageVurdering;
    }

    void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    void setKlageAvvistArsak(String klageAvvistArsak) {
        this.klageAvvistArsak = klageAvvistArsak;
    }

    void setKlageAvvistArsakNavn(String klageAvvistArsakNavn) {
        this.klageAvvistArsakNavn = klageAvvistArsakNavn;
    }

    void setKlageMedholdArsak(String klageMedholdArsak) {
        this.klageMedholdArsak = klageMedholdArsak;
    }

    void setKlageMedholdArsakNavn(String klageMedholdArsakNavn) {
        this.klageMedholdArsakNavn = klageMedholdArsakNavn;
    }

    void setKlageVurdertAv(String klageVurdertAv) {
        this.klageVurdertAv = klageVurdertAv;
    }

    void setVedtaksdatoPaklagdBehandling(LocalDate vedtaksdatoPaklagdBehandling) {
        this.vedtaksdatoPaklagdBehandling = vedtaksdatoPaklagdBehandling;
    }
}
