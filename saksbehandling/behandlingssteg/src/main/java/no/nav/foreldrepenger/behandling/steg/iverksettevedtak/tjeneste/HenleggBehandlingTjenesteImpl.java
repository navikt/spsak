package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
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
    public HenleggBehandlingTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
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
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        håndterHenleggelseUtenOppgitteSøknadsopplysninger(behandling, kontekst);
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET.equals(årsakKode)) {
            sendHenleggelsesbrev(behandlingId, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
        behandlingskontrollTjeneste.lagHistorikkinnslagForHenleggelse(behandlingId, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.SAKSBEHANDLER);
    }

    private void sendHenleggelsesbrev(Long behandlingId, HistorikkAktør vedtaksløsningen) {
        varselTjeneste.sendVarsel(behandlingId, "Henleggelse");
    }

    private void håndterHenleggelseUtenOppgitteSøknadsopplysninger(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        Søknad søknad = søknadRepository.hentSøknad(behandling);
        if (søknad == null) {
            // Må ta behandling av vent for å tillate henleggelse (krav i Behandlingskontroll)
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        }
    }

}
