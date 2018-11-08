package no.nav.foreldrepenger.økonomistøtte.api.kodeverk;

public enum ØkonomiKodeKlassifik {
    FPATORD("FPATORD"), // FP (foreldrepenger), AT - arbeidstaker, ORD - ordinær
    FPATFRI("FPATFRI"),
    FPSND_OP("FPSND-OP"),
    FPATAL("FPATAL"),
    FPATSJO("FPATSJO"),
    FPSNDDM_OP("FPSNDDM-OP"),
    FPSNDJB_OP("FPSNDJB-OP"),
    FPSNDFI("FPSNDFI"),
    FPATFER("FPATFER"),
    FPREFAG_IOP("FPREFAG-IOP"), //FP (foreldrepenger), REFAG - arbeidsgiver
    FPREFAGFER_IOP("FPREFAGFER-IOP");

    private String kodeKlassifik;

    ØkonomiKodeKlassifik(String kodeKlassifik) {
        this.kodeKlassifik = kodeKlassifik;
    }

    public String getKodeKlassifik() {
        return kodeKlassifik;
    }
}
