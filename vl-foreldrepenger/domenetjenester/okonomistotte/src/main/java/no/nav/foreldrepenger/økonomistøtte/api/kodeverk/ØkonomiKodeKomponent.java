package no.nav.foreldrepenger.økonomistøtte.api.kodeverk;

// Etter avtale med Økonomi/OS
public enum ØkonomiKodeKomponent {
    VLFP("VLFP"),
    OS("OS")
    ;

    private String kodeKomponent;

    ØkonomiKodeKomponent(String kodeKomponent) {
        this.kodeKomponent = kodeKomponent;
    }

    public String getKodeKomponent() {
        return kodeKomponent;
    }
}