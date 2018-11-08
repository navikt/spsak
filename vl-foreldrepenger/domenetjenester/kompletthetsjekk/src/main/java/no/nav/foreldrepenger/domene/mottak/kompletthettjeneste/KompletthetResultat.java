package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;

public class KompletthetResultat {

    private boolean erOppfylt;
    private LocalDateTime ventefrist;
    private Venteårsak venteårsak;

    private KompletthetResultat(boolean erOppfylt, LocalDateTime ventefrist, Venteårsak venteårsak) {
        this.erOppfylt = erOppfylt;
        this.ventefrist = ventefrist;
        this.venteårsak = venteårsak;
    }

    public static KompletthetResultat oppfylt() {
        return new KompletthetResultat(true, null, null);
    }

    public static KompletthetResultat ikkeOppfylt(LocalDateTime ventefrist, Venteårsak venteårsak) {
        return new KompletthetResultat(false, ventefrist, venteårsak);
    }

    public static KompletthetResultat fristUtløpt() {
        return new KompletthetResultat(false, null, null);
    }

    public LocalDateTime getVentefrist() {
        return ventefrist;
    }

    public Venteårsak getVenteårsak() {
        return venteårsak;
    }

    public boolean erOppfylt() {
        return erOppfylt;
    }

    public boolean erFristUtløpt() {
        return !erOppfylt && ventefrist == null;
    }
}
