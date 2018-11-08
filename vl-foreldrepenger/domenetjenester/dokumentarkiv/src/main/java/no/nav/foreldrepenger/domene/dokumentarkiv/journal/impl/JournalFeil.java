package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;

//TOD (HUMLE): Splitt i flere klasser, en for hver av bruksområdene, da kan også metodenavnene forkortes
public interface JournalFeil extends DeklarerteFeil {

    JournalFeil FACTORY = FeilFactory.create(JournalFeil.class);

    @ManglerTilgangFeil(feilkode = "FP-751834", feilmelding = "Mangler tilgang til å utføre '%s' mot Journalsystemet", logLevel = LogLevel.ERROR)
    Feil journalUtilgjengeligSikkerhetsbegrensning(String operasjon, Exception e);

    @IntegrasjonFeil(feilkode = "FP-195433", feilmelding = "Journalpost ikke funnet", logLevel = LogLevel.WARN)
    Feil hentJournalpostIkkeFunnet(HentJournalpostJournalpostIkkeFunnet e);

    @IntegrasjonFeil(feilkode = "FP-276411", feilmelding = "Journalpost ugyldig input", logLevel = LogLevel.WARN)
    Feil journalpostUgyldigInput(HentJournalpostUgyldigInput e);

    @IntegrasjonFeil(feilkode = "FP-107540", feilmelding = "Journalpost ikke inngående", logLevel = LogLevel.WARN)
    Feil journalpostIkkeInngaaende(HentJournalpostJournalpostIkkeInngaaende e);
}
