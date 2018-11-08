package no.nav.foreldrepenger.web.app.tjenester.fagsak.app;

import java.util.Optional;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;

public interface FagsakApplikasjonTjeneste {
    FagsakSamlingForBruker hentSaker(String søkestreng);

    FagsakSamlingForBruker hentFagsakForSaksnummer(Saksnummer saksnummer);

    Optional<AsyncPollingStatus> sjekkProsessTaskPågår(Saksnummer saksnummer, String gruppe);
}
