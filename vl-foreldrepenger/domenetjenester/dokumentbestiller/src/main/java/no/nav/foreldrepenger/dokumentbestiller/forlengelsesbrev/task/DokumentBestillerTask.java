package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(DokumentBestillerTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class DokumentBestillerTask implements ProsessTaskHandler {

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    DokumentBestillerTask() {
        // for CDI proxy
    }
    
    @Inject
    public DokumentBestillerTask(DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste) {
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String historikkAktør = prosessTaskData.getPropertyValue(DokumentBestillerTaskProperties.HISTORIKK_AKTØR_KEY);
        String dokumentDataId = prosessTaskData.getPropertyValue(DokumentBestillerTaskProperties.DOKUMENT_DATA_ID_KEY);
        String dokumentBegrunnelse = prosessTaskData.getPropertyValue(DokumentBestillerTaskProperties.DOKUMENT_BEGRUNNELSE_ID_KEY);
        dokumentBestillerApplikasjonTjeneste.produserDokument(Long.valueOf(dokumentDataId), new HistorikkAktør(historikkAktør), dokumentBegrunnelse);
    }
}
