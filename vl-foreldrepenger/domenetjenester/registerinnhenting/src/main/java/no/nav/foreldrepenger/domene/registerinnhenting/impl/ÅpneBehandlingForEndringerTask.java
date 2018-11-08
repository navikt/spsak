package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static no.nav.foreldrepenger.domene.registerinnhenting.impl.ÅpneBehandlingForEndringerTask.TASKTYPE;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollProsessTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;

@ApplicationScoped
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class ÅpneBehandlingForEndringerTask extends BehandlingskontrollProsessTask {
    public static final String TASKTYPE = "behandlingskontroll.åpneBehandlingForEndringer";

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    ÅpneBehandlingForEndringerTask() {
        // for CDI proxy
    }
    
    @Inject
    public ÅpneBehandlingForEndringerTask(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                          BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste,
                                          BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider, behandlingskontrollTjeneste, behandlingskontrollTaskTjeneste);
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Timed
    @Override
    protected void prosesser(Behandling behandling) {
        StartpunktType startpunkt = kodeverkTabellRepository.finnStartpunktType(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT.getKode());
        reaktiverAksjonspunkter(behandling, startpunkt);
        behandling.setÅpnetForEndring(true);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, startpunkt.getBehandlingSteg());
    }

    private void reaktiverAksjonspunkter(Behandling behandling, StartpunktType startpunkt) {
        Set<String> aksjonspunkterFraOgMedStartpunkt = behandlingskontrollTjeneste
            .finnAksjonspunktDefinisjonerFraOgMed(behandling, startpunkt.getBehandlingSteg(), true);

        behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(ap -> aksjonspunkterFraOgMedStartpunkt.contains(ap.getAksjonspunktDefinisjon().getKode()))
            .forEach(ap -> {
                if (!ap.erAktivt()) {
                    aksjonspunktRepository.reaktiver(ap);
                }
                aksjonspunktRepository.setReåpnet(ap);
            });
    }
}
