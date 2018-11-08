package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepositoryImpl;

@Dependent
public class KøKontroller {


    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;
    private BehandlingRevurderingRepositoryImpl behandlingRevurderingRepository;
    private BehandlingRepository behandlingRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    public KøKontroller() {
        // For CDI proxy
    }

    @Inject
    public KøKontroller(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                        BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste,
                        BehandlingRevurderingRepositoryImpl behandlingRevurderingRepository,
                        BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingskontrollTaskTjeneste = behandlingskontrollTaskTjeneste;
        this.behandlingRevurderingRepository = behandlingRevurderingRepository;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
    }


    void dekøFørsteBehandlingISakskompleks(Behandling behandling) {
        Optional<Behandling> køetBehandlingMedforelder = behandlingRevurderingRepository.finnKøetBehandlingMedforelder(behandling.getFagsak());
        Behandling behandlingSomSkalBehandles;
        if (behandlingHarÅrsak(køetBehandlingMedforelder, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)) {
            behandlingSomSkalBehandles = køetBehandlingMedforelder.get();

            // Legger nyopprettet behandling i kø, siden denne ikke skal behandles nå
            enkøBehandling(behandling);

        } else {
            behandlingSomSkalBehandles = behandling;
        }
        opprettTaskForÅStarteBehandling(behandlingSomSkalBehandles);
    }

    public void dekøNesteBehandlingISakskompleks(Behandling behandling) {
        opprettTaskForÅStarteBehandling(behandling);
    }

    private void enkøBehandling(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling.getId());
        BehandlingÅrsak.builder(BehandlingÅrsakType.KØET_BEHANDLING).buildFor(behandling);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING);
    }

    void opprettTaskForÅStarteBehandling(Behandling behandling) {
        behandlingskontrollTaskTjeneste.opprettFortsettBehandlingTask(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId(), Optional.of(AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING));
    }

    private boolean behandlingHarÅrsak(Optional<Behandling> køetBehandlingMedforelder, BehandlingÅrsakType behandlingÅrsakType) {
        return køetBehandlingMedforelder
            .map(beh -> beh.getBehandlingÅrsaker().stream()
                .anyMatch(årsak -> årsak.getBehandlingÅrsakType().equals(behandlingÅrsakType)))
            .orElse(false);
    }

}
