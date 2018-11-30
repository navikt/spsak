package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.impl;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InntektFeil extends DeklarerteFeil {

    InntektFeil FACTORY = FeilFactory.create(InntektFeil.class);

    @IntegrasjonFeil(feilkode = "FP-535194", feilmelding = "Fikk følgende sikkerhetsavvik ved kall til inntektstjenesten: %s.", logLevel = LogLevel.ERROR)
    Feil fikkSikkerhetsavvikFraInntekt(String avvikene);

    @TekniskFeil(feilkode = "FP-263743", feilmelding = "Feil ved opprettelse av request mot inntektstjenesten.", logLevel = LogLevel.ERROR)
    Feil feilVedOpprettelseAvInntektRequest(DatatypeConfigurationException e);

    @IntegrasjonFeil(feilkode = "FP-824246", feilmelding = "Feil ved kall til inntektstjenesten.", logLevel = LogLevel.ERROR)
    Feil feilVedKallTilInntekt(Exception e);

    @TekniskFeil(feilkode = "FP-722674", feilmelding = "Kunne ikke serialisere response fra Inntektskomponenten.", logLevel = LogLevel.ERROR)
    Feil kunneIkkeSerialisereResponse(JAXBException e);
}
