package no.nav.foreldrepenger.migrering.api;

import java.util.Iterator;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;

public interface HistorikkMigreringRepository {
    /**
     * Henter alle historikkinnslag hvor tekst er definert
     *
     * @return En liste med Object-arrays som inneholder &lt;historikkinnslag.id, historikkinnslag.tekst&gt;
     */
    Iterator<Historikkinnslag> hentAlleHistorikkinnslag();

    void lagre(Historikkinnslag konvertertInnslag);

    void flush();
}
