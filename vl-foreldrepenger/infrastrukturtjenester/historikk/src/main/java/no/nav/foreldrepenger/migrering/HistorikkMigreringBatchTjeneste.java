package no.nav.foreldrepenger.migrering;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringTjeneste;

/**
 * Migrerer historikkinnslag fra Fundamentet (historikkinnslag.tekst, JSON) til Utbyggeren (HISTORIKKINNSLAG_DEL, tabell)
 */
@ApplicationScoped
public class HistorikkMigreringBatchTjeneste implements BatchTjeneste {

    private static final String BATCHNAVN = "BVL099";

    private HistorikkMigreringTjeneste historikkMigreringTjeneste;

    HistorikkMigreringBatchTjeneste() {
        // for CDI proxy
    }

    @Inject
    public HistorikkMigreringBatchTjeneste(HistorikkMigreringTjeneste historikkMigreringTjeneste) {
        this.historikkMigreringTjeneste = historikkMigreringTjeneste;
    }

    @Override
    public String launch(BatchArguments arguments) {
        historikkMigreringTjeneste.migrerAlleHistorikkinnslag();
        return BATCHNAVN + "-" + UUID.randomUUID();
    }

    @Override
    public BatchStatus status(String batchInstanceNumber) {
        return BatchStatus.OK;
    }

    @Override
    public String getBatchName() {
        return BATCHNAVN;
    }
}
