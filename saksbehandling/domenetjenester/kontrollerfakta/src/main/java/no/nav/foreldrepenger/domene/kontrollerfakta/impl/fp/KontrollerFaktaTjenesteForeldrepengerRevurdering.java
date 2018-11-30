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
@BehandlingTypeRef("BT-004")
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaTjenesteForeldrepengerRevurdering extends KontrollerFaktaTjenesteImpl {

    protected KontrollerFaktaTjenesteForeldrepengerRevurdering() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaTjenesteForeldrepengerRevurdering(BehandlingRepositoryProvider repositoryProvider,
                                                            @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") KontrollerFaktaUtledereTjeneste utlederTjeneste,
                                                            BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        super(repositoryProvider.getBehandlingRepository(), utlederTjeneste, behandlingskontrollTjeneste);
    }

}

