package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.task;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.SendForlengelsesbrevTaskProperties;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.ForlengetDokument;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@ProsessTask(SendForlengelsesbrevTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SendForlengelsesbrevTask implements ProsessTaskHandler {

    private static final Logger log = LoggerFactory.getLogger(SendForlengelsesbrevTask.class);

    private DokumentDataTjeneste dokumentDataTjeneste;

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    SendForlengelsesbrevTask() {
        // for CDI proxy
    }
    
    @Inject
    public SendForlengelsesbrevTask(DokumentDataTjeneste dokumentDataTjeneste,
            DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste,
            BehandlingRepository behandlingRepository,
            BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.dokumentDataTjeneste = dokumentDataTjeneste;
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (skalSendeForlengelsesbrev(behandling)) {
            sendForlengelsesbrevOgOppdaterBehandling(behandling, kontekst);
            log.info("Utført for behandling: {}", behandlingId);
        } else {
            log.info("Ikke utført for behandling: {}, behandlingsfrist ikke utløpt", behandlingId);
        }
    }

    private void sendForlengelsesbrevOgOppdaterBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandling.setBehandlingstidFrist(LocalDate.now(FPDateUtil.getOffset()).plusWeeks(behandling.getType().getBehandlingstidFristUker()));
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        DokumentType forlengetDokument = new ForlengetDokument(DokumentMalType.FORLENGET_DOK);
        Long dokumentDataId = dokumentDataTjeneste.lagreDokumentData(behandling.getId(), forlengetDokument);
        dokumentBestillerApplikasjonTjeneste.produserDokument(dokumentDataId, HistorikkAktør.VEDTAKSLØSNINGEN, null);
    }

    private boolean skalSendeForlengelsesbrev(Behandling behandling) {
        return !behandling.erUnderIverksettelse() && LocalDate.now(FPDateUtil.getOffset()).isAfter(behandling.getBehandlingstidFrist());
    }
}
