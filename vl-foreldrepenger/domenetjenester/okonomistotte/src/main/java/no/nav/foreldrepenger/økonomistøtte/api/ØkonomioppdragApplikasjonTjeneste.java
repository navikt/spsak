package no.nav.foreldrepenger.økonomistøtte.api;

public interface ØkonomioppdragApplikasjonTjeneste {

    /**
     * Oppretter oppdrag, genererer oppdragXML via mapping og
     * så sender det til oppdragssystemet.
     *
     * @param behandlingId                behandling.id
     * @param ventendeTaskId              TaskId til ventende prosessTask
     * @param skalOppdragSendesTilØkonomi hvis ja: Send oppdrag til oppdragssystemet, hvis nei: Ikke send oppdrag til oppdragssystemet
     */
    void utførOppdrag(Long behandlingId, Long ventendeTaskId, boolean skalOppdragSendesTilØkonomi);

    /**
     * Finn tilsvarende oppdrag som venter på kvittering
     * og behandle det
     *
     * @param kvittering Kvittering fra oppdragssystemet
     */
    void behandleKvittering(ØkonomiKvittering kvittering);

}
