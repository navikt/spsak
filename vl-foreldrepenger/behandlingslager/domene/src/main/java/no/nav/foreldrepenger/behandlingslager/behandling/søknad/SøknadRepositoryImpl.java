package no.nav.foreldrepenger.behandlingslager.behandling.søknad;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class SøknadRepositoryImpl implements SøknadRepository {

    private EntityManager entityManager;
    private BehandlingRepository behandlingRepository;

    public SøknadRepositoryImpl() {
    }

    @Inject
    public SøknadRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingRepository behandlingRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public Søknad hentSøknad(Behandling behandling) {
        Long behandlingId = behandling.getId();
        return hentSøknad(behandlingId);
    }

    @Override
    public Søknad hentSøknad(Long behandlingId) {
        if (behandlingId == null) {
            return null;
        }
        final TypedQuery<SøknadGrunnlagEntitet> query = entityManager.createQuery(
            "FROM SøknadGrunnlag s " +
                "WHERE s.behandling.id = :behandlingId AND s.aktiv = true", SøknadGrunnlagEntitet.class);

        query.setParameter("behandlingId", behandlingId);

        return HibernateVerktøy.hentUniktResultat(query).map(SøknadGrunnlagEntitet::getSøknad)
            .orElseGet(() -> {
                final Optional<Behandling> behandling = behandlingRepository.finnUnikBehandlingForBehandlingId(behandlingId)
                    .flatMap(Behandling::getOriginalBehandling);
                return behandling.map(this::hentSøknad).orElse(null);
            });
    }

    @Override
    public Optional<Søknad> hentSøknadHvisEksisterer(Behandling behandling) {
        return hentSøknadHvisEksisterer(behandling.getId());
    }

    @Override
    public Optional<Søknad> hentSøknadHvisEksisterer(Long behandlingId) {
        try {
            return Optional.ofNullable(hentSøknad(behandlingId));
        } catch (NoResultException ignore) {
            return Optional.empty();
        }
    }

    @Override
    public Søknad hentFørstegangsSøknad(Behandling behandling) {
        final Optional<Behandling> førstegangsSøknad = utledSisteFørstegangsbehandlingSomIkkeErHenlagt(behandling);
        if (førstegangsSøknad.isPresent()) {
            final Optional<Søknad> søknad = hentSøknadHvisEksisterer(førstegangsSøknad.get());
            if (søknad.isPresent()) {
                return søknad.get();
            }
        }
        return hentSøknad(behandling);
    }

    private Optional<Behandling> utledSisteFørstegangsbehandlingSomIkkeErHenlagt(Behandling behandling) {
        return behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(behandling.getFagsak().getSaksnummer())
            .stream()
            .filter(b -> b.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD))
            .filter(b -> b.getBehandlingsresultat() != null && !b.getBehandlingsresultat().isBehandlingHenlagt())
            .filter(this::harSøknad)
            .max(Comparator.comparing(Behandling::getOpprettetTidspunkt));
    }

    private boolean harSøknad(Behandling b) {
        return hentEksisterendeGrunnlag(b).map(SøknadGrunnlagEntitet::getSøknad).isPresent();
    }

    @Override
    public Søknad hentFørstegangsSøknad(Long behandlingId) {
        return hentFørstegangsSøknad(behandlingRepository.hentBehandling(behandlingId));
    }

    @Override
    public void lagreOgFlush(Behandling behandling, Søknad søknad) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        final Optional<SøknadGrunnlagEntitet> søknadGrunnlagEntitet = hentEksisterendeGrunnlag(behandling);
        if (søknadGrunnlagEntitet.isPresent()) {
            // deaktiver eksisterende grunnlag

            final SøknadGrunnlagEntitet søknadGrunnlagEntitet1 = søknadGrunnlagEntitet.get();
            søknadGrunnlagEntitet1.setAktiv(false);
            entityManager.persist(søknadGrunnlagEntitet1);
            entityManager.flush();
        }

        final SøknadGrunnlagEntitet grunnlagEntitet = new SøknadGrunnlagEntitet(behandling, søknad);
        entityManager.persist(søknad);
        entityManager.persist(grunnlagEntitet);
        entityManager.flush();
    }

    private Optional<SøknadGrunnlagEntitet> hentEksisterendeGrunnlag(Behandling behandling) {
        final TypedQuery<SøknadGrunnlagEntitet> query = entityManager.createQuery(
            "FROM SøknadGrunnlag s " +
                "WHERE s.behandling.id = :behandlingId AND s.aktiv = true", SøknadGrunnlagEntitet.class);

        query.setParameter("behandlingId", behandling.getId());

        return HibernateVerktøy.hentUniktResultat(query);
    }
}
