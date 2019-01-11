package no.nav.foreldrepenger.web.app.tjenester.historikk.app;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.historikk.dto.HistorikkinnslagDto;

public interface HistorikkTjenesteAdapter {
    List<HistorikkinnslagDto> hentAlleHistorikkInnslagForSak(Saksnummer saksnummer);

    void lagInnslag(Historikkinnslag historikkinnslag);

    HistorikkInnslagTekstBuilder tekstBuilder();

    void opprettHistorikkInnslag(Behandling behandling, HistorikkinnslagType faktaEndret);
}
