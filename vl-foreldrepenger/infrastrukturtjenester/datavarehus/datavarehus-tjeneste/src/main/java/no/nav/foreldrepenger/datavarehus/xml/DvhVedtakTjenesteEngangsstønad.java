package no.nav.foreldrepenger.datavarehus.xml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.oppdrag.OppdragXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.DvhPersonopplysningXmlTjenesteEngangsstønad;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class DvhVedtakTjenesteEngangsstønad extends DvhVedtakTjeneste {

    DvhVedtakTjenesteEngangsstønad() {
        // for CDI proxy
    }

    @Inject
    public DvhVedtakTjenesteEngangsstønad(@FagsakYtelseTypeRef("ES") VedtakXmlTjeneste vedtakXmlTjeneste,
                                          DvhPersonopplysningXmlTjenesteEngangsstønad personopplysningXmlTjeneste,
                                          OppdragXmlTjeneste oppdragXmlTjeneste) {
        super(vedtakXmlTjeneste, personopplysningXmlTjeneste, oppdragXmlTjeneste);
    }
}
