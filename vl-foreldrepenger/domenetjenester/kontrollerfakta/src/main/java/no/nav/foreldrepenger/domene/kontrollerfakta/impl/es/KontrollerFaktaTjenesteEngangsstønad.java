package no.nav.foreldrepenger.domene.kontrollerfakta.impl.es;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.impl.KontrollerFaktaTjenesteImpl;

@FagsakYtelseTypeRef("ES")
@BehandlingTypeRef
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaTjenesteEngangsstønad extends KontrollerFaktaTjenesteImpl {

    protected KontrollerFaktaTjenesteEngangsstønad() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaTjenesteEngangsstønad(BehandlingRepositoryProvider repositoryProvider,
                                                @FagsakYtelseTypeRef("ES") @BehandlingTypeRef KontrollerFaktaUtledereTjeneste utlederTjeneste,
                                                BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        super(repositoryProvider.getBehandlingRepository(), utlederTjeneste, behandlingskontrollTjeneste);
    }

}
