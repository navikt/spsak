package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface HistorikkRepository extends BehandlingslagerRepository {

    void lagre(Historikkinnslag historikkinnslag);

    List<Historikkinnslag> hentHistorikk(Long behandlingId);

    List<Historikkinnslag> hentHistorikkForSak(Long fagsakId);

    List<Historikkinnslag> hentHistorikkForSaksnummer(Saksnummer saksnummer);
}
