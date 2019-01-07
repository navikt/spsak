package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;

@ApplicationScoped
public class SendVedtaksbrevImpl implements SendVedtaksbrev {

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevImpl.class);

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private SendVarselTjeneste varselTjeneste;
    private BehandlingRepository behandlingRepository;

    SendVedtaksbrevImpl() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevImpl(ResultatRepositoryProvider repositoryProvider, SendVarselTjeneste varselTjeneste) {
        this.behandlingVedtakRepository = repositoryProvider.getVedtakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.varselTjeneste = varselTjeneste;
    }

    @Override
    public void sendVedtaksbrev(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentVedtakFor(behandling.getBehandlingsresultat().getId());
        if (!behandlingVedtakOpt.isPresent()) {
            log.info("Det foreligger ikke vedtak i behandling: {}, kan ikke sende vedtaksbrev", behandlingId); //$NON-NLS-1$
            return;
        }
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();

        if (behandlingVedtak.isBeslutningsvedtak()) {
            if (harSendtVarselOmRevurdering(behandlingId)) {
                log.info("Sender informasjonsbrev om uendret utfall i behandling: {}", behandlingId); //$NON-NLS-1$
            } else {
                log.info("Uendret utfall av revurdering og har ikke sendt varsel om revurdering. Sender ikke brev for behandling: {}", behandlingId); //$NON-NLS-1$
                return;
            }
        } else {
            log.info("Sender vedtaksbrev({}) for foreldrepenger i behandling: {}", behandlingVedtak.getVedtakResultatType().getNavn(), behandlingId); // $NON-NLS-1
        }
        sendBrev(behandlingId);
    }

    private void sendBrev(Long behandlingId) {
        varselTjeneste.sendVarsel(behandlingId, "VedtaksBrev");
    }

    private boolean harSendtVarselOmRevurdering(Long behandlingId) {
        // TODO: Vurder om vi har sendt brev om revurdering
        return false;
    }

}
