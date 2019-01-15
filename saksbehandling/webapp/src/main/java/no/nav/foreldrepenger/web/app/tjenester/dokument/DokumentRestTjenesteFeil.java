package no.nav.foreldrepenger.web.app.tjenester.dokument;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.TomtResultatException;

public interface DokumentRestTjenesteFeil extends DeklarerteFeil {
    DokumentRestTjenesteFeil FACTORY = FeilFactory.create(DokumentRestTjenesteFeil.class);

    @ManglerTilgangFeil(feilkode = "FP-909799", feilmelding = "Applikasjon har ikke tilgang til tjeneste.", logLevel = ERROR)
    Feil applikasjonHarIkkeTilgangTilHentJournalpostListeTjeneste(ManglerTilgangException sikkerhetsbegrensning);

    @ManglerTilgangFeil(feilkode = "FP-463438", feilmelding = "Applikasjon har ikke tilgang til tjeneste.", logLevel = ERROR)
    Feil applikasjonHarIkkeTilgangTilHentDokumentTjeneste(ManglerTilgangException sikkerhetsbegrensning);

    @TekniskFeil(feilkode = "FP-595861", feilmelding = "Dokument Ikke Funnet for journalpostId= %s dokumentId= %s", logLevel = WARN, exceptionClass = TomtResultatException.class)
    Feil dokumentIkkeFunnet(String journalpostId, String dokumentId, TekniskException dokumentIkkeFunnet);
}
