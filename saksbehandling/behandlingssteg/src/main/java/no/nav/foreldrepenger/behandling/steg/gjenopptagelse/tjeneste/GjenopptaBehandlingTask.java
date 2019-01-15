package no.nav.foreldrepenger.behandling.steg.gjenopptagelse.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Utfører automatisk gjenopptagelse av en behandling som har
 * et åpent aksjonspunkt som er et autopunkt og har en frist som er passert.
 */
@ApplicationScoped
@ProsessTask(GjenopptaBehandlingTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class GjenopptaBehandlingTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "behandlingskontroll.gjenopptaBehandling";

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RegisterdataEndringshåndterer registerdataOppdaterer;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;

    GjenopptaBehandlingTask() {
        // for CDI proxy
    }
    
    @Inject
    public GjenopptaBehandlingTask(BehandlingRepository behandlingRepository,
                                   BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                   RegisterdataEndringshåndterer registerdataOppdaterer,
                                   BehandlendeEnhetTjeneste behandlendeEnhetTjeneste) {

        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.registerdataOppdaterer = registerdataOppdaterer;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {

        Long behandlingsId = prosessTaskData.getBehandlingId();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingsId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);

        behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);

        behandlendeEnhetTjeneste.sjekkEnhetVedGjenopptak(behandling).ifPresent(organisasjonsEnhet -> {
            behandling.setBehandlendeEnhet(organisasjonsEnhet);
            behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        });

        registerdataOppdaterer.oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
    }
}
