package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.impl;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdUgyldigInputException;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InfotrygdTjenesteFeil extends DeklarerteFeil {

    InfotrygdTjenesteFeil FACTORY = FeilFactory.create(InfotrygdTjenesteFeil.class);

    @IntegrasjonFeil(feilkode = "FP-514379", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = InfotrygdUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, FinnSakListeUgyldigInput årsak);

    @IntegrasjonFeil(feilkode = "FP-514380", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = InfotrygdUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, FinnGrunnlagListeUgyldigInput årsak);

    @TekniskFeil(feilkode = "FP-980125", feilmelding = "Infotrygd rapporterer PersonIkkeFunnet", logLevel = LogLevel.WARN)
    Feil personIkkeFunnet(Exception e);

    @TekniskFeil(feilkode = "FP-250917", feilmelding = "%s ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = LogLevel.WARN)
    Feil tjenesteUtilgjengeligSikkerhetsbegrensning(String tjeneste, Exception exceptionMessage);

    @TekniskFeil(feilkode = "FP-173623", feilmelding = "Teknisk feil i grensesnitt mot %s", logLevel = LogLevel.ERROR)
    Feil tekniskFeil(String tjeneste, DatatypeConfigurationException årsak);

    @TekniskFeil(feilkode = "FP-180124", feilmelding = "Tjeneste %s ikke tilgjengelig (nedetid)", logLevel = LogLevel.WARN)
    Feil nedetid(String tjeneste, IntegrasjonException årsak);

}
