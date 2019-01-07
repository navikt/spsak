package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

/**
 * Utfører innhenting av registerdata.
 */
@ApplicationScoped
@ProsessTask(RegisterdataOppdatererTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class RegisterdataOppdatererTask extends BehandlingProsessTask {

    private static final Logger log = LoggerFactory.getLogger(RegisterdataOppdatererTask.class);

    public static final String TASKTYPE = "behandlingskontroll.registerdataOppdaterBehandling";

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RegisterdataEndringshåndterer registerdataOppdaterer;
    private BehandlingRepository behandlingRepository;

    RegisterdataOppdatererTask() {
        // for CDI proxy
    }
    
    @Inject
    public RegisterdataOppdatererTask(GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                      BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                      RegisterdataEndringshåndterer registerdataOppdaterer) {
        super(grunnlagRepositoryProvider);
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.registerdataOppdaterer = registerdataOppdaterer;
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long behandlingsId = prosessTaskData.getBehandlingId();
        // NB lås før hent behandling
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingsId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);

        // sjekk forhåndsbetingelser for å innhente registerdata
        if (behandling.erSaksbehandlingAvsluttet()) {
            throw new IllegalStateException("Utvikler-feil: Behandling er avsluttet, kan ikke innhente registerdata: " + behandling.getStatus());
        }

        log.info("Innhenter registerdata opplysninger, startpunkt før={}", behandling.getStartpunkt());

        behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
        registerdataOppdaterer.oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);
    }
}
