package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@Dependent
public class VurderØkonomiOppdragProvider {

    private BehandlingRepository behandlingRepository;
    private Instance<VurderØkonomiOppdrag> vurderØkonomiOppdragInstance;

    VurderØkonomiOppdragProvider() {
        // CDI
    }

    @Inject
    public VurderØkonomiOppdragProvider(BehandlingRepositoryProvider repositoryProvider, @Any Instance<VurderØkonomiOppdrag> vurderØkonomiOppdragInstance) {
        this.vurderØkonomiOppdragInstance = vurderØkonomiOppdragInstance;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    VurderØkonomiOppdrag getVurderØkonomiOppdrag(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        String ytelseKode = behandling.getFagsakYtelseType().getKode();
        FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral fagsakTypeRef = new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(ytelseKode);
        Instance<VurderØkonomiOppdrag> selected = vurderØkonomiOppdragInstance.select(fagsakTypeRef);
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for fagsakYtelseType: " + ytelseKode);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for fagsakYtelseType: " + ytelseKode);
        }
        VurderØkonomiOppdrag vurderØkonomiOppdrag = selected.get();
        if (vurderØkonomiOppdrag.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + vurderØkonomiOppdrag.getClass());
        }
        return vurderØkonomiOppdrag;
    }
}
