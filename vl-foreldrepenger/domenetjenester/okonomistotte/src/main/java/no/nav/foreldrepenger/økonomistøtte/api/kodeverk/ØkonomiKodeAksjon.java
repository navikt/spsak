package no.nav.foreldrepenger.økonomistøtte.api.kodeverk;

public enum ØkonomiKodeAksjon {
    A("A"),
    B("B"),
    C("C"),
    EN("1"),
    TO("2"),
    TRE("3"),
    FIRE("4"),
    FEM("5"),
    SEKS("6"),
    SJU("7");

    private String kodeAksjon;

    ØkonomiKodeAksjon(String kodeAksjon) {
        this.kodeAksjon = kodeAksjon;
    }

    public String getKodeAksjon() {
        return kodeAksjon;
    }
}
