package no.nav.foreldrepenger.domene.medlem.impl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@Dependent
public class MedlemEndringssjekkerProvider {

    private Instance<MedlemEndringssjekker> alleEndringssjekkere;

    MedlemEndringssjekkerProvider() {
        // CDI
    }

    @Inject
    public MedlemEndringssjekkerProvider(@Any Instance<MedlemEndringssjekker> alleEndringssjekkere) {
        this.alleEndringssjekkere = alleEndringssjekkere;
    }

    MedlemEndringssjekker getEndringssjekker(Behandling behandling) {
        String faksakTypeKode = behandling.getFagsak().getYtelseType().getKode();
        FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral fagsakTypeRef = new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(faksakTypeKode);
        Instance<MedlemEndringssjekker> selected = alleEndringssjekkere.select(fagsakTypeRef);
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for faksakType: " + faksakTypeKode);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for faksakTypeKode: " + faksakTypeKode);
        }
        MedlemEndringssjekker minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }
}
