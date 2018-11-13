package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Observerer behandlinger med åpne aksjonspunkter og oppretter deretter oppgave i Gsak.
 */
@ApplicationScoped
public class OpprettOppgaveEventObserver {

    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private OppgaveTjeneste oppgaveTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private TotrinnTjeneste totrinnTjeneste;

    @Inject
    public OpprettOppgaveEventObserver(OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository, OppgaveTjeneste oppgaveTjeneste, ProsessTaskRepository prosessTaskRepository, TotrinnTjeneste totrinnTjeneste) {
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    /**
     * Håndterer oppgave etter at behandlingskontroll er kjørt ferdig.
     */
    public void opprettOppgaveDersomDetErÅpneAksjonspunktForAktivtBehandlingSteg(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        List<Aksjonspunkt> åpneAksjonspunkt = event.getÅpneAksjonspunktForAktivtBehandlingSteg(AksjonspunktType.MANUELL);
        Behandling behandling = event.getBehandling();
        if (behandling.isBehandlingPåVent()) {
            oppgaveTjeneste.opprettTaskAvsluttOppgave(behandling);
            return;
        }

        Collection<Totrinnsvurdering> totrinnsvurderings = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
        //TODO(OJR) kunne informasjonen om hvilken oppgaveårsak som skal opprettes i GSAK være knyttet til AksjonspunktDef?
        if (!åpneAksjonspunkt.isEmpty()) {
            if (harAksjonspunkt(åpneAksjonspunkt, AksjonspunktDefinisjon.FATTER_VEDTAK)) {
                oppgaveTjeneste.avsluttOppgaveOgStartTask(behandling, behandling.getBehandleOppgaveÅrsak(), OpprettOppgaveGodkjennVedtakTask.TASKTYPE);
            } else if (erSendtTilbakeFraBeslutter(totrinnsvurderings)) {
                opprettOppgaveVedBehov(behandling);
            } else if (harAksjonspunkt(åpneAksjonspunkt, AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NK)) {
                // Avslutt eksisterende oppgave og opprett ny (for ny behandlende enhet)
                oppgaveTjeneste.avsluttOppgaveOgStartTask(behandling, behandling.getBehandleOppgaveÅrsak(), OpprettOppgaveForBehandlingTask.TASKTYPE);
            } else {
                opprettOppgaveVedBehov(behandling);
            }
        }
    }

    private void opprettOppgaveVedBehov(Behandling behandling) {
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        Optional<OppgaveBehandlingKobling> aktivOppgave = OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(behandling.getBehandleOppgaveÅrsak(), oppgaveBehandlingKoblinger);
        if (!aktivOppgave.isPresent()) {
            ProsessTaskData enkeltTask = opprettProsessTaskData(behandling, OpprettOppgaveForBehandlingTask.TASKTYPE);
            enkeltTask.setCallIdFraEksisterende();
            prosessTaskRepository.lagre(enkeltTask);
        }
    }

    private boolean erSendtTilbakeFraBeslutter(Collection<Totrinnsvurdering> åpneTotrinnVurderinger) {
        return åpneTotrinnVurderinger.stream().anyMatch(ap -> !ap.getVurderPåNyttÅrsaker().isEmpty());
    }

    private boolean harAksjonspunkt(List<Aksjonspunkt> åpneAksjonspunkt, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return åpneAksjonspunkt.stream().anyMatch(apListe -> apListe.getAksjonspunktDefinisjon().equals(aksjonspunktDefinisjon));
    }

    private ProsessTaskData opprettProsessTaskData(Behandling behandling, String prosesstaskType) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(prosesstaskType);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return prosessTaskData;
    }
}
