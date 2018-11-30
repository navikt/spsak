package no.nav.foreldrepenger.kodeverk;

import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface KodeverkFeil extends DeklarerteFeil {

    KodeverkFeil FACTORY = FeilFactory.create(KodeverkFeil.class);

    @IntegrasjonFeil(feilkode = "FP-868813", feilmelding = "Kodeverk ikke funnet", logLevel = LogLevel.ERROR)
    Feil hentKodeverkKodeverkIkkeFunnet(HentKodeverkHentKodeverkKodeverkIkkeFunnet ex);

    @IntegrasjonFeil(feilkode = "FP-402870", feilmelding = "Kodeverktype ikke støttet: %s", logLevel = LogLevel.ERROR)
    Feil hentKodeverkKodeverkTypeIkkeStøttet(String kodeverkType);

    @IntegrasjonFeil(feilkode = "FP-563155", feilmelding = "Synkronisering med kodeverk feilet: %s", logLevel = LogLevel.WARN)
    Feil synkronoseringAvKodeverkFeilet(String kodeverkKode, IntegrasjonException e);

    @IntegrasjonFeil(feilkode = "FP-840390", feilmelding = "Eksisterende koderelasjon ikke mottatt: %s %s -> %s %s", logLevel = LogLevel.WARN)
    Feil eksisterendeKodeRelasjonIkkeMottatt(String kodeverk1, String kode1, String kodeverk2, String kode2);

    @IntegrasjonFeil(feilkode = "FP-075896", feilmelding = "Eksisterende kode ikke mottatt: %s %s", logLevel = LogLevel.INFO)
    Feil eksisterendeKodeIkkeMottatt(String kodeverk, String kode);

    @IntegrasjonFeil(feilkode = "FP-924461", feilmelding = "Kan ikke opprette koderelasjon med kode som ikke eksisterer: %s %s", logLevel = LogLevel.WARN)
    Feil nyKodeRelasjonMedIkkeEksisterendeKode(String kodeverk, String kode);
}
