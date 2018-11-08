package no.nav.foreldrepenger.behandlingslager.fagsak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FagsakRelasjonRepositoryImpl implements FagsakRelasjonRepository {

    private EntityManager entityManager;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FagsakLåsRepository fagsakLåsRepository;

    FagsakRelasjonRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public FagsakRelasjonRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, YtelsesFordelingRepository ytelsesFordelingRepository, FagsakLåsRepository fagsakLåsRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.ytelsesFordelingRepository = ytelsesFordelingRepository;
        this.fagsakLåsRepository = fagsakLåsRepository;
    }

    @Override
    public FagsakRelasjon finnRelasjonFor(Fagsak fagsak) {
        TypedQuery<FagsakRelasjon> query = entityManager.createQuery("from FagsakRelasjon where (fagsakNrEn=:fagsak or fagsakNrTo=:fagsak) AND aktiv = true", FagsakRelasjon.class);
        query.setParameter("fagsak", fagsak); // NOSONAR $NON-NLS-1$
        return hentEksaktResultat(query);
    }

    @Override
    public Optional<FagsakRelasjon> finnRelasjonForHvisEksisterer(Fagsak fagsak) {
        TypedQuery<FagsakRelasjon> query = entityManager.createQuery("from FagsakRelasjon where (fagsakNrEn=:fagsak or fagsakNrTo=:fagsak) AND aktiv = true", FagsakRelasjon.class);
        query.setParameter("fagsak", fagsak); // NOSONAR $NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public void lagre(Behandling behandling, Stønadskontoberegning stønadskontoberegning) {
        Objects.requireNonNull(stønadskontoberegning, "stønadskontoberegning");

        final FagsakLås fagsak1Lås = fagsakLåsRepository.taLås(behandling.getFagsakId());
        FagsakLås fagsak2Lås = null;
        final FagsakRelasjon fagsakRelasjon = hentEllerOpprett(behandling);
        final Optional<Fagsak> fagsakNrTo = fagsakRelasjon.getFagsakNrTo();
        if (fagsakNrTo.isPresent()) {
            fagsak2Lås = fagsakLåsRepository.taLås(fagsakNrTo.get().getId());
        }
        final boolean forskjellige = differ().areDifferent(fagsakRelasjon.getStønadskontoberegning().orElse(null), stønadskontoberegning);
        if (forskjellige) {
            defaktiverEksisterendeRelasjon(fagsakRelasjon);
            entityManager.persist(stønadskontoberegning);
            for (Stønadskonto stønadskonto : stønadskontoberegning.getStønadskontoer()) {
                entityManager.persist(stønadskonto);
            }

            final FagsakRelasjon nyFagsakRelasjon = new FagsakRelasjon(fagsakRelasjon.getFagsakNrEn(),
                fagsakRelasjon.getFagsakNrTo().orElse(null),
                stønadskontoberegning,
                fagsakRelasjon.getDekningsgrad());

            entityManager.persist(nyFagsakRelasjon);
        }
        fagsakLåsRepository.oppdaterLåsVersjon(fagsak1Lås);
        if (fagsak2Lås != null) {
            fagsakLåsRepository.oppdaterLåsVersjon(fagsak2Lås);
        }
        entityManager.flush();
    }

    private FagsakRelasjon hentEllerOpprett(Behandling behandling) {
        final Optional<FagsakRelasjon> optionalFagsakRelasjon = finnRelasjonForHvisEksisterer(behandling.getFagsak());
        if (!optionalFagsakRelasjon.isPresent()) {
            YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
            opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(ytelseFordelingAggregat.getOppgittDekningsgrad().getDekningsgrad()));
        }
        return finnRelasjonFor(behandling.getFagsak());
    }

    @Override
    public void opprettRelasjon(Fagsak fagsak, Dekningsgrad dekningsgrad) {
        Objects.requireNonNull(fagsak, "fagsak"); // NOSONAR $NON-NLS-1$
        final FagsakLås fagsakLås = fagsakLåsRepository.taLås(fagsak);
        opprettRelasjon(fagsak, dekningsgrad, fagsakLås);
    }

    private void opprettRelasjon(Fagsak fagsak, Dekningsgrad dekningsgrad, FagsakLås fagsakLås) {
        Objects.requireNonNull(fagsak, "fagsak"); // NOSONAR $NON-NLS-1$

        final FagsakRelasjon nyFagsakRelasjon = new FagsakRelasjon(fagsak,
            null,
            null, dekningsgrad);

        entityManager.persist(nyFagsakRelasjon);
        fagsakLåsRepository.oppdaterLåsVersjon(fagsakLås);
        entityManager.flush();
    }

    @Override
    public void kobleFagsaker(Fagsak fagsakEn, Fagsak fagsakTo) {
        Objects.requireNonNull(fagsakEn, "fagsakEn");
        Objects.requireNonNull(fagsakTo, "fagsakTo");
        final FagsakLås fagsak1Lås = fagsakLåsRepository.taLås(fagsakEn.getId());
        final FagsakLås fagsak2Lås = fagsakLåsRepository.taLås(fagsakTo.getId());

        if (!fagsakEn.getYtelseType().equals(fagsakTo.getYtelseType())) {
            throw FagsakFeil.FACTORY.kanIkkeKobleSammenSakerMedUlikYtelseType(fagsakEn.getId(), fagsakEn.getYtelseType(),
                fagsakTo.getId(), fagsakTo.getYtelseType()).toException();
        }
        if (fagsakEn.getId().equals(fagsakTo.getId())) {
            throw FagsakFeil.FACTORY.kanIkkeKobleMedSegSelv(fagsakEn.getSaksnummer()).toException();
        }
        if (fagsakEn.getAktørId().equals(fagsakTo.getAktørId())) {
            throw FagsakFeil.FACTORY.kanIkkeKobleSammenToSakerMedSammeAktørId(fagsakEn.getSaksnummer(),
                fagsakTo.getSaksnummer(), fagsakEn.getAktørId()).toException();
        }

        final Optional<FagsakRelasjon> fagsakRelasjon1 = finnRelasjonForHvisEksisterer(fagsakEn);
        fagsakRelasjon1.ifPresent(this::defaktiverEksisterendeRelasjon);
        final Optional<FagsakRelasjon> fagsakRelasjon = finnRelasjonForHvisEksisterer(fagsakTo);
        fagsakRelasjon.ifPresent(this::defaktiverEksisterendeRelasjon);

        final FagsakRelasjon nyFagsakRelasjon = new FagsakRelasjon(fagsakEn,
            fagsakTo,
            fagsakRelasjon1.flatMap(FagsakRelasjon::getStønadskontoberegning).orElse(null),
            fagsakRelasjon1.orElse(fagsakRelasjon1.get()).getDekningsgrad());

        entityManager.persist(nyFagsakRelasjon);
        fagsakLåsRepository.oppdaterLåsVersjon(fagsak1Lås);
        fagsakLåsRepository.oppdaterLåsVersjon(fagsak2Lås);
        entityManager.flush();
    }

    private void defaktiverEksisterendeRelasjon(FagsakRelasjon relasjon) {
        relasjon.setAktiv(false);
        entityManager.persist(relasjon);
        entityManager.flush();
    }

    private DiffEntity differ() {
        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build(true);
        traverser.addLeafClasses(Fagsak.class);
        return new DiffEntity(traverser);
    }
}
