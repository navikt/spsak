package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface FamilieHendelseRepository extends BehandlingslagerRepository {

    FamilieHendelseGrunnlag hentAggregat(Behandling behandling);

    Optional<FamilieHendelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling);

    Optional<FamilieHendelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId);

    DiffResult diffResultat(FamilieHendelseGrunnlag grunnlag1, FamilieHendelseGrunnlag grunnlag2, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    void lagre(Behandling behandling, FamilieHendelseBuilder hendelse);

    void lagreRegisterHendelse(Behandling behandling, FamilieHendelseBuilder hendelse);

    void lagreOverstyrtHendelse(Behandling behandling, FamilieHendelseBuilder hendelse);

    void fjernBekreftetData(Behandling behandling);

    /**
     * Kopierer grunnlag fra en tidligere behandling.  Endrer ikke aggregater, en skaper nye referanser til disse.
     */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling);

    /**
     * Kopierer data fra gammel behandling til ny behandling.
     *
     * Fjerner bekreftede og overstyrte data som var for
     *
     * @param gammelBehandling behandlingen det opprettes revurdering på
     * @param nyBehandling revurderings behandlingen
     */
    void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling gammelBehandling, Behandling nyBehandling);

    FamilieHendelseBuilder opprettBuilderFor(Behandling behandling);

    /**
     * Slette avklart data på en Behandling. Sørger for at samtidige oppdateringer på samme Behandling,
     * eller andre Behandlinger
     * på samme Fagsak ikke kan gjøres samtidig.
     *
     * @see BehandlingLås
     */
    void slettAvklarteData(Behandling behandling, BehandlingLås lås);

    Optional<FamilieHendelseGrunnlag> hentFørsteVersjonAvAggregatHvisEksisterer(Behandling behandling);

    Optional<Long> hentIdPåAktivFamiliehendelse(Behandling behandling);

    FamilieHendelseGrunnlag hentFamilieHendelserPåId(Long aggregatId);
}
