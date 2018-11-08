package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;

@ApplicationScoped
public class VurderOgSendØkonomiOppdragImpl implements VurderOgSendØkonomiOppdrag {

    private static final Logger log = LoggerFactory.getLogger(VurderOgSendØkonomiOppdragImpl.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;
    private VurderØkonomiOppdragProvider vurderØkonomiOppdragProvider;

    VurderOgSendØkonomiOppdragImpl() {
        // for CDI proxy
    }

    @Inject
    public VurderOgSendØkonomiOppdragImpl(BehandlingRepositoryProvider repositoryProvider, ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste,
                                          VurderØkonomiOppdragProvider vurderØkonomiOppdragProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.økonomioppdragApplikasjonTjeneste = økonomioppdragApplikasjonTjeneste;
        this.vurderØkonomiOppdragProvider = vurderØkonomiOppdragProvider;
    }

    @Override
    public void sendOppdrag(Long behandlingId, Long ventendeTaskId, boolean skalOppdragSendesTilØkonomi) {
        log.info("Sender oppdrag Økonomioppdrag i behandling: {} med ventende task: {}", behandlingId, ventendeTaskId);
        økonomioppdragApplikasjonTjeneste.utførOppdrag(behandlingId, ventendeTaskId, skalOppdragSendesTilØkonomi);
    }

    @Override
    public boolean skalSendeOppdrag(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        // Skal bare sende oppdrag når det foreligger vedtak
        if (!behandlingVedtakOpt.isPresent()) {
            return false;
        }
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        VurderØkonomiOppdrag vurderØkonomiOppdrag = vurderØkonomiOppdragProvider.getVurderØkonomiOppdrag(behandlingId);
        return vurderØkonomiOppdrag.skalSendeOppdrag(behandling, behandlingVedtak);
    }
}
