package no.nav.foreldrepenger.behandlingskontroll.observer;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Observerer Aksjonspunkt*Events og måler metrikker for hvor lang tid det har tatt å løse.
 */
@ApplicationScoped
public class MetrikkerForAksjonspunktEventObserver {

    private static final Logger log = LoggerFactory.getLogger(MetrikkerForAksjonspunktEventObserver.class);

    private MetricRegistry metricRegistry;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private KodeverkRepository kodeverkRepository;

    @Inject
    public MetrikkerForAksjonspunktEventObserver(MetricRegistry metricRegistry, BehandlingRepositoryProvider repositoryProvider) {
        this.metricRegistry = metricRegistry;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    public void måleTidFraBehandlingÅpnetTilFørsteAksjonspunktUtført(@Observes AksjonspunktUtførtEvent aksjonspunktUtførtEvent) {

        Long behandlingId = aksjonspunktUtførtEvent.getKontekst().getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        List<Aksjonspunkt> aksjonspunkterUtført = aksjonspunktUtførtEvent.getAksjonspunkter();

        for (Aksjonspunkt aksjonspunktUtført : aksjonspunkterUtført) {
            Set<Aksjonspunkt> aksjonspunktSetAndreLukkedeOgUtførte = behandling.getAksjonspunkter()
                .stream()
                .filter(Aksjonspunkt::erManuell)
                .filter(aksjonspunkt -> !aksjonspunkt.erÅpentAksjonspunkt())
                .filter(aksjonspunkt -> !aksjonspunkt.getId().equals(aksjonspunktUtført.getId()))
                .collect(Collectors.toSet());

            if (aksjonspunktSetAndreLukkedeOgUtførte.isEmpty()) {
                // aksjonspunktUtført er det første utførte for denne behandlingen
                try {
                    BehandlingTema behandlingTema = getBehandlingTema(behandling);
                    if (!BehandlingTema.UDEFINERT.equals(behandlingTema)) {
                        String grafanaKey = String.format("fpsak.%s.behandling.tid.ledetid.forste.manuell", behandlingTema.getOffisiellKode());
                        LocalDateTime naa = LocalDateTime.now(FPDateUtil.getOffset());

                        // ikke anta at aksjonspunktUtført.endretTidspunkt er oppdatert ennå
                        long between = ChronoUnit.MINUTES.between(behandling.getOpprettetDato(), naa);
                        metricRegistry.timer(grafanaKey).update(between, TimeUnit.MINUTES);
                    }
                } catch (RuntimeException e) {
                    FeilFactory.create(Feilene.class).feilVedLagringAvStatistikk(e).log(log);
                }
            }
        }
    }

    private BehandlingTema getBehandlingTema(Behandling behandling) {
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());

        BehandlingTema behandlingTemaTemp = getBehandlingsTemaForFagsak(fagsak);

        return kodeverkRepository.finn(BehandlingTema.class, behandlingTemaTemp);
    }

    private BehandlingTema getBehandlingsTemaForFagsak(Fagsak s) {
        return BehandlingTema.fraFagsak(s);
    }

    interface Feilene extends DeklarerteFeil {
        @TekniskFeil(feilkode = "FP-484313", feilmelding = "Feil oppsto i lagring av statistikk for behandlingstid ", logLevel = WARN)
        Feil feilVedLagringAvStatistikk(RuntimeException e);
    }
}
