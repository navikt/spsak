package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class HistorikkinnslagTotrinnsvurdering {
    private int sekvensNr;
    private String begrunnelse;
    private boolean godkjent;
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;
    LocalDateTime aksjonspunktSistEndret;

    HistorikkinnslagTotrinnsvurdering(int sekvensNr) {
        this.sekvensNr = sekvensNr;
    }

    public HistorikkinnslagTotrinnsvurdering() {
        this(0);
    }

    public LocalDateTime getAksjonspunktSistEndret() {
        return aksjonspunktSistEndret;
    }

    public void setAksjonspunktSistEndret(LocalDateTime aksjonspunktSistEndret) {
        this.aksjonspunktSistEndret = aksjonspunktSistEndret;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public void setAksjonspunktDefinisjon(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
    }

    public int getSekvensNr() {
        return sekvensNr;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public boolean erGodkjent() {
        return godkjent;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public void setGodkjent(boolean godkjent) {
        this.godkjent = godkjent;
    }

}
