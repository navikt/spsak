package no.nav.foreldrepenger.datavarehus.xml;

import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.oppdrag.OppdragXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.personopplysninger.PersonopplysningXmlTjeneste;

public abstract class DvhVedtakTjeneste {

    private VedtakXmlTjeneste vedtakXmlTjeneste;

    DvhVedtakTjeneste() {
        // for CDI proxy
    }

    public DvhVedtakTjeneste(VedtakXmlTjeneste vedtakXmlTjeneste, PersonopplysningXmlTjeneste personopplysningXmlTjeneste, OppdragXmlTjeneste oppdragXmlTjeneste) {
        this.vedtakXmlTjeneste = vedtakXmlTjeneste;
        this.vedtakXmlTjeneste.setOppdragXmlTjeneste(oppdragXmlTjeneste); //Vi må sette oppdrag tjenesten som skal brukes for mapping av datavarehus.
        this.vedtakXmlTjeneste.setPersonopplysningXmlTjeneste(personopplysningXmlTjeneste);
    }

    public String opprettDvhVedtakXml(Long behandlingId) {
        return vedtakXmlTjeneste.opprettVedtakXml(behandlingId);
    }
}
