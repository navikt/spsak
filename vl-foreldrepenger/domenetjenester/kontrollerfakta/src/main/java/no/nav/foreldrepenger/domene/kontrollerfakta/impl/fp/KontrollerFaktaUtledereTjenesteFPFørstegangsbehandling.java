package no.nav.foreldrepenger.domene.kontrollerfakta.impl.fp;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@ApplicationScoped
@BehandlingTypeRef
@StartpunktRef
@FagsakYtelseTypeRef("FP")
public class KontrollerFaktaUtledereTjenesteFPFørstegangsbehandling extends KontrollerFaktaUtledereTjenesteFP {

    @Inject
    public KontrollerFaktaUtledereTjenesteFPFørstegangsbehandling(BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
    }

    @Override
    public List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling) {
        return leggTilFellesutledere(behandling);
    }
}
