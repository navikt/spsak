package no.nav.foreldrepenger.vedtak.xml;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VedtakXmlFeil extends DeklarerteFeil {

    VedtakXmlFeil FACTORY = FeilFactory.create(VedtakXmlFeil.class);

    @TekniskFeil(feilkode = "FP-142918", feilmelding = "Vedtak-XML kan ikke utarbeides for behandling %s i tilstand %s", logLevel = LogLevel.WARN)
    Feil behandlingErIFeilTilstand(Long behandlingId, String tilstand);

    @TekniskFeil(feilkode = "FP-190756", feilmelding = "Vedtak-XML kan ikke utarbeides for behandling %s, serialiseringsfeil", logLevel = LogLevel.ERROR)
    Feil serialiseringsfeil(Long behandlingId, Exception cause);

    @TekniskFeil(feilkode = "FP-701652", feilmelding = "Vedtak-XML kan ikke utarbeides for behandling %s, deserialiseringsfeil", logLevel = LogLevel.ERROR)
    Feil deserialiseringsfeil(Long behandlingId, IOException cause);

    @TekniskFeil(feilkode = "FP-351904", feilmelding = "Vedtak-XML kan ikke utarbeides for behandling %s, vilkårResultat ikke støttet: %s", logLevel = LogLevel.WARN)
    Feil manglerVilkårResultat(Long behandlingId, String vilkårResultat);

    @TekniskFeil(feilkode = "FP-260408", feilmelding = "Vedtak-XML for Engangsstønad kan ikke utarbeides i fagsak id %s som gjelder ytelse %s", logLevel = LogLevel.WARN)
    Feil feilYtelseType(Long fagsakId, String ytelseType);
}
