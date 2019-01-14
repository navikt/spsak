package no.nav.foreldrepenger.behandling.historikk;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface HistorikkTjenesteAdapter {
    List<HistorikkinnslagDto> hentAlleHistorikkInnslagForSak(Saksnummer saksnummer);

    void lagInnslag(Historikkinnslag historikkinnslag);

    HistorikkInnslagTekstBuilder tekstBuilder();

    void opprettHistorikkInnslag(Behandling behandling, HistorikkinnslagType faktaEndret);
}
