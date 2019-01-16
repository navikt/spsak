package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ArbeidsforholdTjenesteFeil extends DeklarerteFeil {

    ArbeidsforholdTjenesteFeil FACTORY = FeilFactory.create(ArbeidsforholdTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-843592", feilmelding = "%s ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = LogLevel.WARN)
    Feil tjenesteUtilgjengeligSikkerhetsbegrensning(String tjeneste, Exception exceptionMessage);

    @IntegrasjonFeil(feilkode = "FP-762485", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = ArbeidsforholdUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, FinnArbeidsforholdPrArbeidstakerUgyldigInput årsak);

    @IntegrasjonFeil(feilkode = "FP-927182", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = ArbeidsforholdUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet årsak);

    @TekniskFeil(feilkode = "FP-793428", feilmelding = "Teknisk feil i grensesnitt mot %s", logLevel = LogLevel.ERROR)
    Feil tekniskFeil(String tjeneste, DatatypeConfigurationException årsak);

}
