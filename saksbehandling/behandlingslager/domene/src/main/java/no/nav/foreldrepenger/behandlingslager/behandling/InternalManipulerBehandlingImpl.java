package no.nav.foreldrepenger.behandlingslager.behandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

@ApplicationScoped
public class InternalManipulerBehandlingImpl implements InternalManipulerBehandling {
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;

    public InternalManipulerBehandlingImpl() {
        // For CDI proxy
    }

    @Inject
    public InternalManipulerBehandlingImpl(GrunnlagRepositoryProvider repositoryProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    public InternalManipulerBehandlingImpl(BehandlingRepository behandlingRepository, KodeverkRepository kodeverkRepository) {
        this.behandlingRepository = behandlingRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType) {
        forceOppdaterBehandlingSteg(behandling, stegType, BehandlingStegStatus.UDEFINERT);
    }

    @Override
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus stegStatus) {
        forceOppdaterBehandlingSteg(behandling, stegType, stegStatus, BehandlingStegStatus.UTFØRT);
    }

    @Override
    public void forceOppdaterBehandlingSteg(Behandling behandling, BehandlingStegType stegType, BehandlingStegStatus nesteStegStatus,
            BehandlingStegStatus sluttStatusForEksisterendeSteg) {

        // finn riktig mapping av kodeverk slik at vi får med dette når Behandling brukes videre.
        BehandlingStegStatus nesteStatus = canonicalize(nesteStegStatus);
        BehandlingStegStatus sluttStatus = canonicalize(
                (sluttStatusForEksisterendeSteg == null ? BehandlingStegStatus.UTFØRT : sluttStatusForEksisterendeSteg));
        BehandlingStegType canonStegType = canonicalize(stegType);

        // Oppdater behandling til den nye stegtilstanden
        BehandlingStegTilstand stegTilstand = new BehandlingStegTilstand(behandling, canonStegType);
        stegTilstand.setBehandlingStegStatus(nesteStatus);
        behandling.oppdaterBehandlingStegOgStatus(stegTilstand, sluttStatus);
    }

    private BehandlingStegType canonicalize(BehandlingStegType stegType) {
        return stegType == null ? null : behandlingRepository.finnBehandlingStegType(stegType.getKode());
    }

    @SuppressWarnings("unchecked")
    protected <V extends Kodeliste> V canonicalize(V kodeliste) {
        return kodeliste == null ? null
                : (V) kodeverkRepository.finn(kodeliste.getClass(), kodeliste.getKode());
    }

}
