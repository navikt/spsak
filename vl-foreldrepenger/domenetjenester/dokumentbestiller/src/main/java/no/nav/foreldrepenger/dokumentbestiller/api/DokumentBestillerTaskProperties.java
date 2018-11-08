package no.nav.foreldrepenger.dokumentbestiller.api;

public class DokumentBestillerTaskProperties {

    public static final String TASKTYPE = "dokumentbestiller.bestillDokument";

    public static final String DOKUMENT_DATA_ID_KEY = "dokumentDataId";
    public static final String HISTORIKK_AKTØR_KEY = "historikkAktor";
    public static final String DOKUMENT_BEGRUNNELSE_ID_KEY = "dokumentBegrunnelse";

    private DokumentBestillerTaskProperties() {
        // Skal ikke konstrueres
    }
}
