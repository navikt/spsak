package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

public interface VurderOgSendØkonomiOppdrag {
    /**
     * Vurder om oppdrag skal sendes, hvis ja: Send oppdrag
     *
     * @param behandlingId behandling.id
     * @param ventendeTaskId TaskId til ventende prosessTask
     * @param skalOppdragSendesTilØkonomi hvis ja: Send oppdrag til oppdragssystemet, hvis nei: Ikke send oppdrag til oppdragssystemet
     */
    void sendOppdrag(Long behandlingId, Long ventendeTaskId, boolean skalOppdragSendesTilØkonomi);

    /**
     * Vurder om oppdrag skal sendes.
     *
     * @param behandlingId behandling.id
     * @return
     */
    boolean skalSendeOppdrag(Long behandlingId);
}
