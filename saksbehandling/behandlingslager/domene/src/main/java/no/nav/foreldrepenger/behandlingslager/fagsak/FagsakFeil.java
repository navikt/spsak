package no.nav.foreldrepenger.behandlingslager.fagsak;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface FagsakFeil extends DeklarerteFeil {

    FagsakFeil FACTORY = FeilFactory.create(FagsakFeil.class);

    @TekniskFeil(feilkode = "FP-343387", feilmelding = "Det var flere enn en Fagsak for bruker: %s", logLevel = LogLevel.WARN)
    Feil flereEnnEnFagsakForBruker(AktørId aktørID);

    @TekniskFeil(feilkode = "FP-429883", feilmelding = "Det var flere enn en Fagsak for saksnummer: %s", logLevel = LogLevel.WARN)
    Feil flereEnnEnFagsakForSaksnummer(Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-081717", feilmelding = "Bruker har skiftet rolle fra '%s' til '%s'", logLevel = LogLevel.WARN)
    Feil brukerHarSkiftetRolle(String gammelKode, String nyKode);

    @TekniskFeil(feilkode = "FP-831923", feilmelding = "Prøver å koble fagsak med saksnummer %s sammen med seg selv", logLevel = LogLevel.WARN)
    Feil kanIkkeKobleMedSegSelv(Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-983410", feilmelding = "Kan ikke koble sammen saker med forskjellig ytelse type. Prøver å koble sammen fagsakene %s (%s) og %s (%s).", logLevel = LogLevel.WARN)
    Feil kanIkkeKobleSammenSakerMedUlikYtelseType(Long fagsakEn, FagsakYtelseType sakEnType, Long fagsakTo, FagsakYtelseType sakToType);

    @TekniskFeil(feilkode = "FP-102432", feilmelding = "Kan ikke koble sammen to saker med identisk aktørid. Prøver å koble sammen fagsakene %s og %s, aktør %s.", logLevel = LogLevel.WARN)
    Feil kanIkkeKobleSammenToSakerMedSammeAktørId(Saksnummer saksnummer, Saksnummer saksnummer1, AktørId aktørId);
}
