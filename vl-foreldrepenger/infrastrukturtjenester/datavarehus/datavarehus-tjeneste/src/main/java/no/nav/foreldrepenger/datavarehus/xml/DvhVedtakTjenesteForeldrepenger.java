package no.nav.foreldrepenger.datavarehus.xml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.oppdrag.OppdragXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.DvhPersonopplysningXmlTjenesteForeldrepenger;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class DvhVedtakTjenesteForeldrepenger extends DvhVedtakTjeneste {

    DvhVedtakTjenesteForeldrepenger() {
        // for CDI proxy
    }

    @Inject
    public DvhVedtakTjenesteForeldrepenger(@FagsakYtelseTypeRef("FP") VedtakXmlTjeneste vedtakXmlTjeneste,
                                           DvhPersonopplysningXmlTjenesteForeldrepenger personopplysningXmlTjeneste,
                                           OppdragXmlTjeneste oppdragXmlTjeneste) {
        super(vedtakXmlTjeneste, personopplysningXmlTjeneste, oppdragXmlTjeneste);
    }
}
