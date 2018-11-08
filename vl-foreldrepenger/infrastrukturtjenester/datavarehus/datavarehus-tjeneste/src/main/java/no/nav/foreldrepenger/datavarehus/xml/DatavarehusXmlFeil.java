package no.nav.foreldrepenger.datavarehus.xml;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DatavarehusXmlFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "FP-103784", feilmelding = "DVH-Vedtak-XML kan ikke utarbeides for behandling %s i tilstand %s", logLevel = LogLevel.WARN)
    Feil behandlingErIFeilTilstand(Long behandlingId, String tilstand);

    @TekniskFeil(feilkode = "FP-445341", feilmelding = "DVH-Vedtak-XML kan ikke utarbeides for behandling %s, serialiseringsfeil", logLevel = LogLevel.ERROR)
    Feil serialiseringsfeil(Long behandlingId, Exception cause);

    @TekniskFeil(feilkode = "FP-745272", feilmelding = "DVH-Vedtak-XML for Engangsstønad kan ikke utarbeides i fagsak id %s som gjelder ytelse %s", logLevel = LogLevel.WARN)
    Feil feilYtelseType(Long id, String beskrivelse);
}
