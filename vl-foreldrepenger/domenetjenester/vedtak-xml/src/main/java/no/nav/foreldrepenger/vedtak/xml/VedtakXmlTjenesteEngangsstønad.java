package no.nav.foreldrepenger.vedtak.xml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjenesteEngangsstønad;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class VedtakXmlTjenesteEngangsstønad extends VedtakXmlTjeneste {

    VedtakXmlTjenesteEngangsstønad() {
        // for CDI proxy
    }

    @Inject
    public VedtakXmlTjenesteEngangsstønad(BehandlingRepositoryProvider repositoryProvider,
                                          PersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste,
                                          @FagsakYtelseTypeRef("ES") BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste) {
        super(repositoryProvider, personopplysningXmlTjeneste, behandlingsresultatXmlTjeneste);
    }
}
