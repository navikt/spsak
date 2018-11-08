package no.nav.foreldrepenger.kodeverk;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.BatchTjeneste;
/**
 * Henter ned offisielle kodeverk fra NAV som brukes i løsningen og synker den til egen kodeverk-tabell. 
 */
@ApplicationScoped
public class KodeverkSynkroniseringBatchTjeneste implements BatchTjeneste {

    private static final String BATCHNAVN = "BVL005";

    private KodeverkSynkronisering kodeverkSynkronisering;

    KodeverkSynkroniseringBatchTjeneste() {
        // for CDI proxy
    }

    @Inject
    public KodeverkSynkroniseringBatchTjeneste(KodeverkSynkronisering kodeverkSynkronisering) {
        this.kodeverkSynkronisering = kodeverkSynkronisering;
    }

    @Override
    public String launch(BatchArguments arguments) {
        kodeverkSynkronisering.synkroniserAlleKodeverk();
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
