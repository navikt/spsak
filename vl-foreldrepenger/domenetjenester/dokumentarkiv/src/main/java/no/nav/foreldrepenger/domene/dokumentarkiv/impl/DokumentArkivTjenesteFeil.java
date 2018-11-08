package no.nav.foreldrepenger.domene.dokumentarkiv.impl;


import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;

public interface DokumentArkivTjenesteFeil extends DeklarerteFeil {

    DokumentArkivTjenesteFeil FACTORY = FeilFactory.create(DokumentArkivTjenesteFeil.class);

    @ManglerTilgangFeil(feilkode = "FP-751934", feilmelding = "Mangler tilgang til å utføre '%s' mot Journalsystemet", logLevel = LogLevel.ERROR)
    Feil journalUtilgjengeligSikkerhetsbegrensning(String operasjon, Exception e);

    @IntegrasjonFeil(feilkode = "FP-195533", feilmelding = "Journalpost ikke funnet", logLevel = LogLevel.WARN)
    Feil hentJournalpostIkkeFunnet(HentDokumentJournalpostIkkeFunnet e);

    @IntegrasjonFeil(feilkode = "FP-276511", feilmelding = "Journalpost ugyldig input", logLevel = LogLevel.WARN)
    Feil journalpostUgyldigInput(HentKjerneJournalpostListeUgyldigInput e);

    @IntegrasjonFeil(feilkode = "FP-249790", feilmelding = "Fant ikke journal dokument", logLevel = LogLevel.WARN)
    Feil hentDokumentIkkeFunnet(HentDokumentDokumentIkkeFunnet e);

}
