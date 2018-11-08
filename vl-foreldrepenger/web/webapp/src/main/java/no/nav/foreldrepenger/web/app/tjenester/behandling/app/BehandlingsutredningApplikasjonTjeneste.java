package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface BehandlingsutredningApplikasjonTjeneste {
    /**
     * Hent behandlinger for angitt saksnummer (offisielt GSAK saksnummer)
     */
    List<Behandling> hentBehandlingerForSaksnummer(Saksnummer saksnummer);

    void settBehandlingPaVent(Long behandlingsId, LocalDate frist, Venteårsak ventearsak);

    void endreBehandlingPaVent(Long behandlingId, LocalDate frist, Venteårsak ventearsak);

    void byttBehandlendeEnhet(Long behandlingId, OrganisasjonsEnhet enhet, String begrunnelse, HistorikkAktør historikkAktør);

    void kanEndreBehandling(Long behandlingId, Long versjon);

    /** Opprett ny behandling. Returner Prosess Task gruppe for å ta den videre. */
    void opprettNyFørstegangsbehandling(Long fagsakId, Saksnummer saksnummer, boolean erEtterKlageBehandling);

    Behandling opprettInnsyn(Saksnummer saksnummer);

    Behandling opprettRevurdering(Fagsak fagsak, BehandlingÅrsakType behandlingÅrsakType);

}
