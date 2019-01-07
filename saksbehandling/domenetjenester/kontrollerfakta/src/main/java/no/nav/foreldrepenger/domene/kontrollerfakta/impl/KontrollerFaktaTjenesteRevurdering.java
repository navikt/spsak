package no.nav.foreldrepenger.domene.kontrollerfakta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.StartpunktRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;

@FagsakYtelseTypeRef("FP")
@BehandlingTypeRef("BT-004")
@StartpunktRef
@ApplicationScoped
public class KontrollerFaktaTjenesteRevurdering extends KontrollerFaktaTjenesteImpl {

    protected KontrollerFaktaTjenesteRevurdering() {
        // for CDI proxy
    }

    @Inject
    public KontrollerFaktaTjenesteRevurdering(GrunnlagRepositoryProvider repositoryProvider,
                                              @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") KontrollerFaktaUtledereTjeneste utlederTjeneste,
                                              BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        super(repositoryProvider.getBehandlingRepository(), utlederTjeneste, behandlingskontrollTjeneste);
    }

}

