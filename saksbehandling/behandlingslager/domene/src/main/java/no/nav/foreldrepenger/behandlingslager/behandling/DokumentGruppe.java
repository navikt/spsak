package no.nav.foreldrepenger.behandlingslager.behandling;

public enum DokumentGruppe {

    SØKNAD("SØKNAD"),
    INNTEKTSMELDING("INNTEKTSMELDING"),
    ENDRINGSSØKNAD("ENDRINGSSØKNAD"), // TODO SP: Trenger vi denne lenger? Eller er dette korreksjon av søknad?
    KLAGE("KLAGE"),
    VEDLEGG("VEDLEGG"), // TODO SP : Trenger vi denne lenger?
    UDEFINERT("-"); //$NON-NLS-1$

    private final String kode;

    DokumentGruppe(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
