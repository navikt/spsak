package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task.StartBerørtBehandlingTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveSendTilInfotrygdTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class HenleggBehandlingTjenesteImpl implements HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private SøknadRepository søknadRepository;
    private FagsakRepository fagsakRepository;
    private SendVarselTjeneste varselTjeneste;

    public HenleggBehandlingTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public HenleggBehandlingTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                         BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                         ProsessTaskRepository prosessTaskRepository,
                                         SendVarselTjeneste varselTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.varselTjeneste = varselTjeneste;
    }

    @Override
    public void henleggBehandling(Long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingskontrollKontekst kontekst =  behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        håndterHenleggelseUtenOppgitteSøknadsopplysninger(behandling, kontekst);
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET.equals(årsakKode)
                || BehandlingResultatType.HENLAGT_KLAGE_TRUKKET.equals(årsakKode)
                || BehandlingResultatType.HENLAGT_INNSYN_TRUKKET.equals(årsakKode)) {
            sendHenleggelsesbrev(behandlingId, HistorikkAktør.VEDTAKSLØSNINGEN);
        } else if (BehandlingResultatType.MANGLER_BEREGNINGSREGLER.equals(årsakKode)) {
            fagsakRepository.fagsakSkalBehandlesAvInfotrygd(behandling.getFagsakId());
            opprettOppgaveTilInfotrygd(behandling);
        }
        behandlingskontrollTjeneste.lagHistorikkinnslagForHenleggelse(behandlingId, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.SAKSBEHANDLER);
        startTaskForDekøingAvBerørtBehandling(behandling);
    }

    private void sendHenleggelsesbrev(Long behandlingId, HistorikkAktør vedtaksløsningen) {
        varselTjeneste.sendVarsel(behandlingId, "Henleggelse");
    }

    private void opprettOppgaveTilInfotrygd(Behandling behandling) {
        ProsessTaskData data = new ProsessTaskData(OpprettOppgaveSendTilInfotrygdTask.TASKTYPE);
        data.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(data);
    }

    private void håndterHenleggelseUtenOppgitteSøknadsopplysninger(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        Søknad søknad = søknadRepository.hentSøknad(behandling);
        if (søknad == null) {
            // Må ta behandling av vent for å tillate henleggelse (krav i Behandlingskontroll)
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        }
    }

    private void startTaskForDekøingAvBerørtBehandling(Behandling behandling) {
        ProsessTaskData taskData = new ProsessTaskData(StartBerørtBehandlingTask.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(taskData);
    }

}
