package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.util.Objects.check;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingRevurderingRepositoryImpl implements BehandlingRevurderingRepository {

    private EntityManager entityManager;
    private BehandlingRepository behandlingRepository;
    private SøknadRepository søknadRepository;
    private BehandlingLåsRepository behandlingLåsRepository;

    BehandlingRevurderingRepositoryImpl() {
    }

    @Inject
    public BehandlingRevurderingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager,
                                               GrunnlagRepositoryProvider repositoryProvider) {

        Objects.requireNonNull(entityManager, "entityManager");
        Objects.requireNonNull(repositoryProvider, "repositoryProvider");
        this.entityManager = entityManager;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.behandlingLåsRepository = repositoryProvider.getBehandlingLåsRepository();
    }

    EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public List<Behandling> finnHenlagteBehandlingerEtterSisteInnvilgedeIkkeHenlagteBehandling(Long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$

        Optional<Behandling> sisteInnvilgede = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsakId);

        if (sisteInnvilgede.isPresent()) {
            final List<Long> behandlingsIder = finnHenlagteBehandlingerEtter(fagsakId, sisteInnvilgede.get());
            for (Long behandlingId : behandlingsIder) {
                behandlingLåsRepository.taLås(behandlingId);
            }
            return behandlingsIder.stream()
                .map(behandlingId -> behandlingRepository.hentBehandling(behandlingId))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Behandling> hentSisteYtelsesbehandling(Long fagsakId) {
        return behandlingRepository.hentSisteBehandlingForFagsakId(fagsakId);
    }

    private List<Long> finnHenlagteBehandlingerEtter(Long fagsakId, Behandling sisteInnvilgede) {
        TypedQuery<Long> query = getEntityManager().createQuery(
            "SELECT b.id FROM Behandling b WHERE b.fagsak.id=:fagsakId " +
                " AND b.behandlingType=:type" +
                " AND b.opprettetTidspunkt >= :etterTidspunkt " +
                " AND EXISTS (SELECT r FROM Behandlingsresultat r" +
                "    WHERE r.behandling=b " +
                "    AND r.behandlingResultatType IN :henlagtKoder)" +
                " ORDER BY b.opprettetTidspunkt ASC", //$NON-NLS-1$
            Long.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("type", BehandlingType.REVURDERING);
        query.setParameter("henlagtKoder", BehandlingResultatType.getAlleHenleggelseskoder());
        query.setParameter("etterTidspunkt", sisteInnvilgede.getOpprettetDato());
        return query.getResultList();
    }

    @Override
    public Optional<Behandling> finnÅpenYtelsesbehandling(Long fagsakId) {
        List<Behandling> åpenBehandling = finnÅpenogKøetYtelsebehandling(fagsakId).stream()
            .filter(beh -> !beh.erKøet())
            .collect(Collectors.toList());
        check(åpenBehandling.size() <= 1, "Kan maks ha én åpen ytelsesbehandling"); //$NON-NLS-1$
        return optionalFirst(åpenBehandling);
    }

    @Override
    public Optional<Behandling> finnKøetYtelsesbehandling(Long fagsakId) {
        List<Behandling> køetBehandling = finnÅpenogKøetYtelsebehandling(fagsakId).stream()
            .filter(Behandling::erKøet)
            .collect(Collectors.toList());
        check(køetBehandling.size() <= 1, "Kan maks ha én køet ytelsesbehandling"); //$NON-NLS-1$
        return optionalFirst(køetBehandling);
    }

    private List<Behandling> finnÅpenogKøetYtelsebehandling(Long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$

        TypedQuery<Long> query = getEntityManager().createQuery(
            "SELECT b.id " +
                "from Behandling b " +
                "where fagsak.id=:fagsakId " +
                "and status not in (:avsluttet) " +
                "order by opprettetTidspunkt desc", //$NON-NLS-1$
            Long.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("avsluttet", Arrays.asList(BehandlingStatus.AVSLUTTET, BehandlingStatus.IVERKSETTER_VEDTAK)); //$NON-NLS-1$

        List<Long> behandlingIder = query.getResultList();
        for (Long behandlingId : behandlingIder) {
            behandlingLåsRepository.taLås(behandlingId);
        }
        final List<Behandling> behandlinger = behandlingIder.stream()
            .map(behandlingId -> behandlingRepository.hentBehandling(behandlingId))
            .collect(Collectors.toList());
        check(behandlinger.size() <= 2, "Kan maks ha én åpen og én køet ytelsesbehandling"); //$NON-NLS-1$
        check(behandlinger.stream().filter(Behandling::erKøet).count() <= 1, "Kan maks ha én køet ytelsesbehandling"); //$NON-NLS-1$
        check(behandlinger.stream().filter(it -> !it.erKøet()).count() <= 1, "Kan maks ha én åpen ytelsesbehandling"); //$NON-NLS-1$

        return behandlinger;
    }

    @Override
    public Optional<LocalDate> finnSøknadsdatoFraHenlagtBehandling(Behandling behandling) {
        List<Behandling> henlagteBehandlinger = finnHenlagteBehandlingerEtterSisteInnvilgedeIkkeHenlagteBehandling(behandling.getFagsak().getId());
        Optional<Søknad> søknad = finnFørsteSøknadBlantBehandlinger(henlagteBehandlinger);
        if (søknad.isPresent()) {
            return Optional.ofNullable(søknad.get().getSøknadsdato());
        }
        return Optional.empty();
    }

    private Optional<Søknad> finnFørsteSøknadBlantBehandlinger(List<Behandling> behandlinger) {
        return behandlinger.stream()
            .map(behandling -> søknadRepository.hentSøknadHvisEksisterer(behandling))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    private static Optional<Behandling> optionalFirst(List<Behandling> behandlinger) {
        return behandlinger.isEmpty() ? Optional.empty() : Optional.of(behandlinger.get(0));
    }
}
