package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.impl;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface MeldekortFeil extends DeklarerteFeil {

    MeldekortFeil FACTORY = FeilFactory.create(MeldekortFeil.class);

    @TekniskFeil(feilkode = "FP-150919", feilmelding = "%s ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = LogLevel.WARN)
    Feil tjenesteUtilgjengeligSikkerhetsbegrensning(String tjeneste, Exception exceptionMessage);

    @IntegrasjonFeil(feilkode = "FP-615298", feilmelding = "%s fant ikke person for oppgitt aktørId", logLevel = LogLevel.WARN)
    Feil fantIkkePersonForAktorId(String tjeneste, Exception exceptionMessage);

    @IntegrasjonFeil(feilkode = "FP-615299", feilmelding = "%s ugyldig input", logLevel = LogLevel.WARN)
    Feil tjenesteUgyldigInput(String tjeneste, Exception exceptionMessage);

    @TekniskFeil(feilkode = "FP-073523", feilmelding = "Teknisk feil i grensesnitt mot %s", logLevel = LogLevel.ERROR)
    Feil tekniskFeil(String tjeneste, DatatypeConfigurationException årsak);

}
