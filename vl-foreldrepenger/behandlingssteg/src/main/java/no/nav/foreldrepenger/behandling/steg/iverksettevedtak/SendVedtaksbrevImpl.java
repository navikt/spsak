package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;

@ApplicationScoped
public class SendVedtaksbrevImpl implements SendVedtaksbrev {

    private static final Logger log = LoggerFactory.getLogger(SendVedtaksbrevImpl.class);

    private BehandlingRepository behandlingRepository;

    private BehandlingVedtakRepository behandlingVedtakRepository;
    private SendVarselTjeneste varselTjeneste;

    SendVedtaksbrevImpl() {
        // for CDI proxy
    }

    @Inject
    public SendVedtaksbrevImpl(BehandlingRepositoryProvider repositoryProvider, SendVarselTjeneste varselTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.varselTjeneste = varselTjeneste;
    }

    @Override
    public void sendVedtaksbrev(Long behandlingId) {
        Optional<BehandlingVedtak> behandlingVedtakOpt = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (!behandlingVedtakOpt.isPresent()) {
            log.info("Det foreligger ikke vedtak i behandling: {}, kan ikke sende vedtaksbrev", behandlingId); //$NON-NLS-1$
            return;
        }
        BehandlingVedtak behandlingVedtak = behandlingVedtakOpt.get();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.erKlage()) {
            if (!skalSendeVedtaksbrevIKlagebehandling(behandling)) {
                log.info("Sender ikke vedtaksbrev for klagebehandling: {}, gjelder medhold fra NFP", behandlingId); //$NON-NLS-1$
                return;
            } else if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
                SendVedtaksbrevFeil.FACTORY.kanIkkeSendeVedtaksbrev(behandlingVedtak.getVedtakResultatType().getNavn(), behandlingId).log(log);
                return;
            }
        }

        if (erBehandlingEtterKlage(behandling) && !skalSendeVedtaksbrevEtterKlage(behandling)) {
            log.info("Sender ikke vedtaksbrev for vedtak fra omgjøring fra klageinstansen på behandling {}, gjelder medhold fra klageinstans", behandlingId); //$NON-NLS-1$
            return;
        }

        if (behandlingVedtak.isBeslutningsvedtak()) {
            if (harSendtVarselOmRevurdering(behandlingId)) {
                log.info("Sender informasjonsbrev om uendret utfall i behandling: {}", behandlingId); //$NON-NLS-1$
            } else {
                log.info("Uendret utfall av revurdering og har ikke sendt varsel om revurdering. Sender ikke brev for behandling: {}", behandlingId); //$NON-NLS-1$
                return;
            }
        } else {
            log.info("Sender vedtaksbrev({}) for foreldrepenger i behandling: {}", behandlingVedtak.getVedtakResultatType().getNavn(), behandlingId); //$NON-NLS-1
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

    private boolean skalSendeVedtaksbrevIKlagebehandling(Behandling behandling) {
        if (behandling.erRevurdering() && erBehandlingEtterKlage(behandling)) {
            return false;
        }
        KlageVurderingResultat vurdering = behandling.hentGjeldendeKlageVurderingResultat().orElse(null);
        if (vurdering == null) {
            return false;
        }
        if (KlageVurdering.MEDHOLD_I_KLAGE.equals(vurdering.getKlageVurdering())
                && (KlageVurdertAv.NFP.equals(vurdering.getKlageVurdertAv()) || KlageVurdertAv.NK.equals(vurdering.getKlageVurdertAv()))) {
            return false;
        }
        return true;
    }


    private boolean skalSendeVedtaksbrevEtterKlage(Behandling behandling) {
        Behandling klage = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId(), BehandlingType.KLAGE).orElse(null);
        if (klage == null) {
            return true;
        }
        KlageVurderingResultat vurdering = klage.hentGjeldendeKlageVurderingResultat().orElse(null);
        if (vurdering == null) { // henlagt eller ikke avsluttet
            return true;
        }
        if (KlageVurdering.MEDHOLD_I_KLAGE.equals(vurdering.getKlageVurdering())
            && KlageVurdertAv.NK.equals(vurdering.getKlageVurdertAv())) {
            return false;
        }
        return true;
    }

    private boolean erBehandlingEtterKlage(Behandling behandling) {
        return BehandlingÅrsakType.årsakerEtterKlageBehandling().stream().anyMatch(behandling::harBehandlingÅrsak);
    }
}
