package no.nav.foreldrepenger.domene.virksomhet;

import javax.xml.bind.JAXBException;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;


public interface OrganisasjonTjenesteFeil extends DeklarerteFeil{

    OrganisasjonTjenesteFeil FACTORY = FeilFactory.create(OrganisasjonTjenesteFeil.class);

    @IntegrasjonFeil(feilkode = "FP-254132", feilmelding = "Fant ikke organisasjon for orgNummer %s", logLevel = LogLevel.WARN, exceptionClass = OrganisasjonIkkeFunnetException.class)
    Feil organisasjonIkkeFunnet(String orgnr, HentOrganisasjonOrganisasjonIkkeFunnet årsak);

    @IntegrasjonFeil(feilkode= "FP-934726", feilmelding = "Funksjonell feil i grensesnitt mot %s, med orgnr %s", logLevel = LogLevel.WARN, exceptionClass = OrganisasjonUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, String orgnr, HentOrganisasjonUgyldigInput årsak);

    @TekniskFeil(feilkode = "FP-348126", feilmelding = "Kunne ikke serialisere response fra %s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeSerialisereResponse(String tjeneste, JAXBException årsak);
}
