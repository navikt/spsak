package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

public interface SaksbehandlingDokumentmottakTjeneste {

    /** Dokument ankommet, returner Prosess Task gruppe id. */
    void dokumentAnkommet(InngåendeSaksdokument saksdokument);
}
