package no.nav.foreldrepenger.behandlingskontroll.task;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Kjører behandlingskontroll automatisk fra der prosessen står.
 */
@ApplicationScoped
@ProsessTask(FortsettBehandlingTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class FortsettBehandlingTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    FortsettBehandlingTask() {
        // For CDI proxy
    }

    @Inject
    public FortsettBehandlingTask(BehandlingRepositoryProvider repositoryProvider) {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public void doTask(ProsessTaskData data) {

        // dynamisk lookup, så slipper vi å validere bean ved oppstart i test av moduler etc. før det faktisk brukes
        CDI<Object> cdi = CDI.current();
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = cdi.select(BehandlingskontrollTjeneste.class).get();

        try {
            Long behandlingId = data.getBehandlingId();
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
            Boolean manuellFortsettelse = Optional.ofNullable(data.getPropertyValue(FortsettBehandlingTaskProperties.MANUELL_FORTSETTELSE))
                .map(Boolean::valueOf)
                .orElse(Boolean.FALSE);

            if (manuellFortsettelse) {
                behandlingskontrollTjeneste.settAutopunkterTilUtført(kontekst, false);
            } else {
                String utført = data.getPropertyValue(FortsettBehandlingTaskProperties.UTFORT_AUTOPUNKT);
                if (utført != null) {
                    AksjonspunktDefinisjon aksjonspunkt = aksjonspunktRepository.finnAksjonspunktDefinisjon(utført);
                    behandlingskontrollTjeneste.settAutopunktTilUtført(aksjonspunkt, kontekst);
                }
                validerBehandlingIkkeErSattPåVent(behandlingId);
            }

            behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        } finally {
            cdi.destroy(behandlingskontrollTjeneste);
        }
    }

    private void validerBehandlingIkkeErSattPåVent(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.isBehandlingPåVent()) {
            throw new IllegalStateException("Utviklerfeil: Ikke tillatt å fortsette behandling på vent");
        }
    }
}
