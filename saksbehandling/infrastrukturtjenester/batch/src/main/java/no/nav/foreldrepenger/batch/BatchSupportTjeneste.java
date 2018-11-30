package no.nav.foreldrepenger.batch;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;

public interface BatchSupportTjeneste {

    /**
     * Initiell oppretting av BatchSchedulerTask - vil opprette og kjøre en umiddelbart hvis det ikke allerede finnes en KLAR.
     **/
    void startBatchSchedulerTask();

    /**
     * Opprett en gruppe batchrunners fulgt av en batchscheduler
     **/
    void opprettScheduledTasks(ProsessTaskGruppe gruppe);

    /**
     * Finn riktig batchtjeneste for oppgitt batchnavn.
     */
    BatchTjeneste finnBatchTjenesteForNavn(String batchNavn);

    /**
     * Prøv å kjøre feilete tasks på nytt - restart av andre system.
     */
    void retryAlleProsessTasksFeilet();

}
