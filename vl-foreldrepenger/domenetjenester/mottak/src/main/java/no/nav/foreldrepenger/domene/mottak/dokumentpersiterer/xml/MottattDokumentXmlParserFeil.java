package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface MottattDokumentXmlParserFeil extends DeklarerteFeil {

    MottattDokumentXmlParserFeil FACTORY = FeilFactory.create(MottattDokumentXmlParserFeil.class);

    @TekniskFeil(feilkode = "FP-958724", feilmelding = "Fant ikke xsd for namespacet '%s'", logLevel = WARN)
    Feil ukjentNamespace(String namespace, IllegalStateException e);

    @TekniskFeil(feilkode = "FP-312346", feilmelding = "Feil ved parsing av ukjent journaldokument-type med namespace '%s'", logLevel = ERROR)
    Feil uventetFeilVedParsingAvSoeknadsXml(String namespace, Exception e);
}
