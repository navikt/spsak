package no.nav.foreldrepenger.behandlingslager.fagsak;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;

public interface FagsakLåsRepository extends BehandlingslagerRepository {

    /**
     * Tar lås på underliggende rader
     *
     * @param fagsakId fagsaken
     * @return låsen
     */
    FagsakLås taLås(Long fagsakId);

    /**
     * Tar lås på underliggende rader
     * Kaller bare på {@link #taLås(Long)}
     *
     * @param fagsak fagsaken
     * @return låsen
     */
    FagsakLås taLås(Fagsak fagsak);

    /**
     * Verifisering av lås
     *
     * @param fagsakLås låsen
     */
    void oppdaterLåsVersjon(FagsakLås fagsakLås);
}
