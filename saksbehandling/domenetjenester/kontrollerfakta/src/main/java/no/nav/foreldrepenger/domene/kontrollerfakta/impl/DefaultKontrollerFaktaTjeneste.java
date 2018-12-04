package no.nav.foreldrepenger.domene.kontrollerfakta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;

@FagsakYtelseTypeRef("FP")
@BehandlingTypeRef
@StartpunktRef
@ApplicationScoped
public class DefaultKontrollerFaktaTjeneste extends KontrollerFaktaTjenesteImpl {

    protected DefaultKontrollerFaktaTjeneste() {
        // for CDI proxy
    }

    @Inject
    public DefaultKontrollerFaktaTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                                 @FagsakYtelseTypeRef("FP") @BehandlingTypeRef KontrollerFaktaUtledereTjeneste utlederTjeneste,
                                                 BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        super(repositoryProvider.getBehandlingRepository(), utlederTjeneste, behandlingskontrollTjeneste);
    }
}
