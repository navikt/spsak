package no.nav.foreldrepenger.vedtak.xml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.BehandlingsresultatXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjenesteForeldrepenger;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VedtakXmlTjenesteForeldrepenger extends VedtakXmlTjeneste {

    VedtakXmlTjenesteForeldrepenger() {
        // for CDI proxy
    }

    @Inject
    public VedtakXmlTjenesteForeldrepenger(BehandlingRepositoryProvider repositoryProvider,
                                           PersonopplysningXmlTjenesteForeldrepenger personopplysningXmlTjeneste,
                                           @FagsakYtelseTypeRef("FP") BehandlingsresultatXmlTjeneste behandlingsresultatXmlTjeneste) {
        super(repositoryProvider, personopplysningXmlTjeneste, behandlingsresultatXmlTjeneste);
    }
}
