package no.nav.foreldrepenger.behandling.statusobserver;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@Dependent
public class OppdaterFagsakStatusProvider {

    private Instance<OppdaterFagsakStatus> oppdaterFagsakStatuser;

    OppdaterFagsakStatusProvider() {
        // CDI
    }

    @Inject
    public OppdaterFagsakStatusProvider(@Any Instance<OppdaterFagsakStatus> oppdaterFagsakStatuser) {
        this.oppdaterFagsakStatuser = oppdaterFagsakStatuser;
    }

    public OppdaterFagsakStatus getOppdaterFagsakStatus(Behandling behandling) {
        String faksakTypeKode = behandling.getFagsak().getYtelseType().getKode();
        FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral fagsakTypeRef = new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(faksakTypeKode);
        Instance<OppdaterFagsakStatus> instance = oppdaterFagsakStatuser.select(fagsakTypeRef);
        if (instance.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for faksakType: " + faksakTypeKode);
        } else if (instance.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for faksakTypeKode: " + faksakTypeKode);
        }
        OppdaterFagsakStatus minInstans = instance.get();

        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }
}
