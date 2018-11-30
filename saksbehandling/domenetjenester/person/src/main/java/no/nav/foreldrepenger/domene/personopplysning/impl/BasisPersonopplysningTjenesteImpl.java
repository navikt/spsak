package no.nav.foreldrepenger.domene.personopplysning.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;

@ApplicationScoped
public class BasisPersonopplysningTjenesteImpl extends AbstractPersonopplysningTjenesteImpl implements BasisPersonopplysningTjeneste {

    BasisPersonopplysningTjenesteImpl() {
        super();
    }

    @Inject
    public BasisPersonopplysningTjenesteImpl(BehandlingRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(repositoryProvider, skjæringstidspunktTjeneste);
    }

}
