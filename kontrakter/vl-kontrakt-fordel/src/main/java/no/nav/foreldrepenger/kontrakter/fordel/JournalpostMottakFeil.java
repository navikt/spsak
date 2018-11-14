package no.nav.foreldrepenger.kontrakter.fordel;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface JournalpostMottakFeil extends DeklarerteFeil {

    JournalpostMottakFeil FACTORY = FeilFactory.create(JournalpostMottakFeil.class);

    @TekniskFeil(feilkode = "F-217605", feilmelding = "Input-validering-feil: Avsender sendte payload, men oppgav ikke lengde på innhold", logLevel = WARN)
    Feil manglerPayloadLength();

    @TekniskFeil(feilkode = "F-483098", feilmelding = "Input-validering-feil: Avsender oppgav at lengde på innhold var %s, men lengden var egentlig %s", logLevel = WARN)
    Feil feilPayloadLength(Integer oppgitt, Integer faktisk);
}
