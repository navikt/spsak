package no.nav.foreldrepenger.domene.kontrollerfakta.impl.fp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.impl.KontrollerFaktaTjenesteImpl;

@FagsakYtelseTypeRef("FP")
@BehandlingTypeRef
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaTjenesteForeldrepenger extends KontrollerFaktaTjenesteImpl {

    protected KontrollerFaktaTjenesteForeldrepenger() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaTjenesteForeldrepenger(BehandlingRepositoryProvider repositoryProvider,
                                                 @FagsakYtelseTypeRef("FP") @BehandlingTypeRef KontrollerFaktaUtledereTjeneste utlederTjeneste,
                                                 BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        super(repositoryProvider.getBehandlingRepository(), utlederTjeneste, behandlingskontrollTjeneste);
    }
}
