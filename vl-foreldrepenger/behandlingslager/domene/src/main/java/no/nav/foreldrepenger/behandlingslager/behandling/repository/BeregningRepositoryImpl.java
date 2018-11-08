package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BeregningRepositoryImpl implements BeregningRepository {

    private EntityManager entityManager;
    private BehandlingRepository behandlingRepository;

    BeregningRepositoryImpl() {
        // for CDI proxy
    }

    public BeregningRepositoryImpl(EntityManager entityManager) {
        // for test
        this(entityManager, new BehandlingRepositoryImpl(entityManager));
    }

    @Inject
    public BeregningRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingRepository behandlingRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.behandlingRepository = behandlingRepository;
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long lagre(BeregningResultat beregningResultat, BehandlingLås lås) {

        Behandling originalBehandling = beregningResultat.getOriginalBehandling();

        if (originalBehandling == null || originalBehandling.getId() == null) {
            throw new IllegalStateException("Glemt å lagre " // NOSONAR //$NON-NLS-1$
                + Behandling.class.getSimpleName()
                + "? Denne må lagres separat siden " // NOSONAR //$NON-NLS-1$
                + BeregningResultat.class.getSimpleName()
                + " er et separat aggregat delt mellom flere behandlinger"); //$NON-NLS-1$ // NOSONAR
        }

        getEntityManager().persist(beregningResultat);
        beregningResultat.getBeregninger().forEach(beregning -> getEntityManager().persist(beregning));
        verifiserBehandlingLås(lås);
        getEntityManager().flush();
        return beregningResultat.getId();
    }

    @Override
    public Sats finnEksaktSats(SatsType satsType, LocalDate dato) {
        TypedQuery<Sats> query = entityManager.createQuery("from Sats where satsType=:satsType" + //$NON-NLS-1$
                " and periode.fomDato<=:dato" + //$NON-NLS-1$
                " and periode.tomDato>=:dato", Sats.class); //$NON-NLS-1$

        query.setParameter("satsType", satsType); //$NON-NLS-1$
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        query.getResultList();
        return hentEksaktResultat(query);
    }

    @Override
    public Optional<Beregning> getSisteBeregning(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return getSisteBeregning(behandling);
    }

    private Optional<Beregning> getSisteBeregning(Behandling behandling) {
        Optional<Beregning> beregning = Optional.ofNullable(behandling.getBehandlingsresultat()).map(behandlingsresultat -> behandlingsresultat.getBeregningResultat()).map(beregningResultat -> beregningResultat.getSisteBeregning()).orElse(Optional.empty());
        return beregning;
    }

    @Override
    public void lagreBeregning(Long behandlingId, Beregning nyBeregning) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Beregning sisteBeregning = getSisteBeregning(behandling).orElse(null);

        BeregningResultat beregningResultat = (sisteBeregning == null ? BeregningResultat.builder()
                : BeregningResultat.builderFraEksisterende(sisteBeregning.getBeregningResultat()))
                        .medBeregning(nyBeregning)
                        .buildFor(behandling);

        BehandlingLås skriveLås = taSkriveLås(behandling);
        lagre(beregningResultat, skriveLås);
    }

    protected BehandlingLås taSkriveLås(Behandling behandling) {
        return behandlingRepository.taSkriveLås(behandling);
    }

    // sjekk lås og oppgrader til skriv
    protected void verifiserBehandlingLås(BehandlingLås lås) {
        BehandlingLåsRepositoryImpl låsHåndterer = new BehandlingLåsRepositoryImpl(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }
}
