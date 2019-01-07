package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface BehandlingRepository extends BehandlingslagerRepository {

    /**
     * Hent Behandling med angitt id.
     */
    Behandling hentBehandling(Long behandlingId);

    /**
     * Hent Behandling, der det ikke er gitt at behandlingId er korrekt (eks. for validering av innsendte verdier)
     */
    Optional<Behandling> finnUnikBehandlingForBehandlingId(Long behandlingId);

    /**
     * Hent siste behandling for angitt {@link Fagsak#id}
     */
    Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId);

    /**
     * Hent siste behandling for angitt {@link Fagsak#id} og behandling type
     */
    Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId, BehandlingType behandlingType);

    /**
     * Hent siste behandling for angitt {@link Fagsak#id} men ekskluder behandlinger av behandling type
     */
    Optional<Behandling> hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(Long fagsakId, List<BehandlingType> behandlingType);

    /**
     * NB: Sikker på at du vil hente alle behandlinger, inklusiv de som er lukket?
     * <p>
     * Hent alle behandling for angitt saksnummer.
     * Dette er eksternt saksnummer angitt av GSAK.
     */
    List<Behandling> hentAbsoluttAlleBehandlingerForSaksnummer(Saksnummer saksnummer);

    /**
     * Lagrer behandling, sikrer at relevante parent-entiteter (Fagsak, FagsakRelasjon) også oppdateres.
     */
    Long lagre(Behandling behandling, BehandlingLås lås);

    /**
     * Lagrer vilkårResultat på en Behandling. Sørger for at samtidige oppdateringer på samme Behandling, eller
     * andre Behandlinger
     * på samme Fagsak ikke kan gjøres samtidig.
     *
     * @return id for {@link VilkårResultat} opprettet/endret.
     * @see BehandlingLås
     */
    Long lagre(VilkårResultat vilkårResultat, BehandlingLås lås);

    /**
     * Ta lås for oppdatering av behandling/fagsak. Påkrevd før lagring.
     * Convenience metode som tar hele entiteten.
     *
     * @see #taSkriveLås(Long, Long)
     */
    BehandlingLås taSkriveLås(Behandling behandling);

    /**
     * Hent alle behandlinger som ikke er avsluttet på fagsak.
     */
    List<Behandling> hentBehandlingerSomIkkeErAvsluttetForFagsakId(Long fagsakId);

    /**
     * Hent alle åpne behandlinger på fagsak.
     */
    List<Behandling> hentÅpneBehandlingerForFagsakId(Long fagsakId);

    /**
     * Hent alle behandlinger for en fagsak som har en av de angitte behandlingsårsaker
     */
    List<Behandling> hentBehandlingerMedÅrsakerForFagsakId(Long fagsakId, Set<BehandlingÅrsakType> årsaker);

    Optional<Behandling> finnSisteAvsluttedeIkkeHenlagteBehandling(Long fagsakId);

    BehandlingStegType finnBehandlingStegType(String kode);

    Boolean erVersjonUendret(Long behandlingId, Long versjon);

    /**
     * Lager en ny Behandling basert på en gammel, med samme grunnlag strukturer.
     */
    Behandling opprettNyBehandlingBasertPåTidligere(Behandling gammelBehandling, BehandlingType behandlingType, GrunnlagRepositoryProvider repositoryProvider);

    void verifiserBehandlingLås(BehandlingLås lås);

    void oppdaterSistOppdatertTidspunkt(Behandling behandling, LocalDateTime tidspunkt);

    Optional<LocalDateTime> hentSistOppdatertTidspunkt(Behandling behandling);

    List<BehandlingÅrsak> finnÅrsakerForBehandling(Behandling behandling);

    List<BehandlingÅrsakType> finnÅrsakTyperForBehandling(Behandling behandling);
}
