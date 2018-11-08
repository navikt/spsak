package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FamilieHendelseRepositoryImpl implements FamilieHendelseRepository {

    private EntityManager entityManager;
    private BehandlingLåsRepository behandlingLåsRepository;

    FamilieHendelseRepositoryImpl() {
        // CDI
    }

    @Inject
    public FamilieHendelseRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingLåsRepository behandlingLåsRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingLåsRepository = behandlingLåsRepository;
    }

    public FamilieHendelseRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
        }
    }

    @Override
    public FamilieHendelseGrunnlag hentAggregat(Behandling behandling) {
        final Optional<FamilieHendelseGrunnlagEntitet> aktivtFamilieHendelseGrunnlag = getAktivtFamilieHendelseGrunnlag(behandling);
        if (aktivtFamilieHendelseGrunnlag.isPresent()) {
            return aktivtFamilieHendelseGrunnlag.get();
        }
        throw FamilieHendelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandling.getId()).toException();
    }

    @Override
    public Optional<FamilieHendelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        return hentAggregatHvisEksisterer(behandling.getId());
    }

    @Override
    public Optional<FamilieHendelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId) {
        final Optional<FamilieHendelseGrunnlagEntitet> aktivtFamilieHendelseGrunnlag = getAktivtFamilieHendelseGrunnlag(behandlingId);
        return aktivtFamilieHendelseGrunnlag.isPresent() ? Optional.of(aktivtFamilieHendelseGrunnlag.get()) : Optional.empty();
    }

    @Override
    public DiffResult diffResultat(FamilieHendelseGrunnlag grunnlag1, FamilieHendelseGrunnlag grunnlag2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    private Optional<FamilieHendelseGrunnlagEntitet> getAktivtFamilieHendelseGrunnlag(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        Long behandlingId = behandling.getId();
        return getAktivtFamilieHendelseGrunnlag(behandlingId);
    }

    private Optional<FamilieHendelseGrunnlagEntitet> getAktivtFamilieHendelseGrunnlag(Long behandlingId) {
        final TypedQuery<FamilieHendelseGrunnlagEntitet> query = entityManager.createQuery("FROM FamilieHendelseGrunnlag gr " + // NOSONAR //$NON-NLS-1$
            "WHERE gr.behandling.id = :behandlingId " + //$NON-NLS-1$
            "AND gr.aktiv = :aktivt", FamilieHendelseGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        query.setParameter("aktivt", true); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }

    private void lagreOgFlush(Behandling behandling, FamilieHendelseGrunnlag nyttGrunnlag) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        if (nyttGrunnlag == null) {
            return;
        }
        final BehandlingLås lås = behandlingLåsRepository.taLås(behandling.getId());
        final Optional<FamilieHendelseGrunnlagEntitet> tidligereAggregat = getAktivtFamilieHendelseGrunnlag(behandling);

        if (tidligereAggregat.isPresent()) {
            final FamilieHendelseGrunnlagEntitet aggregat = tidligereAggregat.get();
            if (!diffResultat(aggregat, nyttGrunnlag, behandling.getFagsakYtelseType(), true).isEmpty()) {
                aggregat.setAktiv(false);
                entityManager.persist(aggregat);
                entityManager.flush();
                lagreGrunnlag(nyttGrunnlag, behandling);
            }
        } else {
            lagreGrunnlag(nyttGrunnlag, behandling);
        }
        verifiserBehandlingLås(lås);
        entityManager.flush();
    }

    private void lagreGrunnlag(FamilieHendelseGrunnlag nyttGrunnlag, Behandling behandling) {
        ((FamilieHendelseGrunnlagEntitet) nyttGrunnlag).setBehandling(behandling);
        lagreHendelse(nyttGrunnlag.getSøknadVersjon());

        if (nyttGrunnlag.getBekreftetVersjon().isPresent()) {
            lagreHendelse(nyttGrunnlag.getBekreftetVersjon().get());
        }

        if (nyttGrunnlag.getOverstyrtVersjon().isPresent()) {
            final FamilieHendelse entity = nyttGrunnlag.getOverstyrtVersjon().get();
            lagreHendelse(entity);
        }

        entityManager.persist(nyttGrunnlag);
    }

    private void lagreHendelse(FamilieHendelse entity) {
        entityManager.persist(entity);
        if (entity.getTerminbekreftelse().isPresent()) {
            entityManager.persist(entity.getTerminbekreftelse().get());
        }
        if (entity.getAdopsjon().isPresent()) {
            entityManager.persist(entity.getAdopsjon().get());
        }
        for (UidentifisertBarn uidentifisertBarn : entity.getBarna()) {
            entityManager.persist(uidentifisertBarn);
        }
    }

    @Override
    public void lagre(Behandling behandling, FamilieHendelseBuilder hendelseBuilder) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        Objects.requireNonNull(hendelseBuilder, "hendelseBuilder"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$

        final FamilieHendelseGrunnlagBuilder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        switch (hendelseBuilder.getType()) {
            case SØKNAD:
                aggregatBuilder.medSøknadVersjon(hendelseBuilder);
                break;
            case BEKREFTET:
                aggregatBuilder.medBekreftetVersjon(hendelseBuilder);
                break;
            case OVERSTYRT:
                aggregatBuilder.medOverstyrtVersjon(hendelseBuilder);
                break;
        }
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagreRegisterHendelse(Behandling behandling, FamilieHendelseBuilder hendelse) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        Objects.requireNonNull(hendelse, "hendelse"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$

        final FamilieHendelseGrunnlagBuilder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medBekreftetVersjon(hendelse);
        // nullstill overstyring ved overgang fra termin til fødsel
        if (harOverstyrtTerminOgOvergangTilFødsel(aggregatBuilder.getKladd())) {
            aggregatBuilder.medOverstyrtVersjon(null);
        }
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    private boolean harOverstyrtTerminOgOvergangTilFødsel(FamilieHendelseGrunnlag kladd) {
        final FamilieHendelseType overstyrtHendelseType = kladd.getOverstyrtVersjon()
            .map(FamilieHendelse::getType).orElse(FamilieHendelseType.UDEFINERT);
        return kladd.getHarOverstyrteData() && overstyrtHendelseType.equals(FamilieHendelseType.TERMIN)
            && kladd.getBekreftetVersjon().map(FamilieHendelse::getType).orElse(FamilieHendelseType.UDEFINERT).equals(FamilieHendelseType.FØDSEL);
    }

    @Override
    public void lagreOverstyrtHendelse(Behandling behandling, FamilieHendelseBuilder hendelse) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        Objects.requireNonNull(hendelse, "hendelse"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$

        final FamilieHendelseGrunnlagBuilder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medOverstyrtVersjon(hendelse);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void fjernBekreftetData(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        final Optional<FamilieHendelseGrunnlag> grunnlag = hentAggregatHvisEksisterer(behandling);
        if (!grunnlag.isPresent()) {
            return;
        }
        final FamilieHendelseGrunnlagBuilder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        if (grunnlag.get().getOverstyrtVersjon().isPresent()) {
            aggregatBuilder.medOverstyrtVersjon(null);
        }
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling) {
        final Optional<FamilieHendelseGrunnlagEntitet> familieHendelseGrunnlag = getAktivtFamilieHendelseGrunnlag(gammelBehandling);
        if (familieHendelseGrunnlag.isPresent()) {
            final FamilieHendelseGrunnlagEntitet entitet = new FamilieHendelseGrunnlagEntitet(familieHendelseGrunnlag.get());

            lagreOgFlush(nyBehandling, entitet);
        }
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling gammelBehandling, Behandling nyBehandling) {
        final Optional<FamilieHendelseGrunnlagEntitet> familieHendelseGrunnlag = getAktivtFamilieHendelseGrunnlag(gammelBehandling);
        if (familieHendelseGrunnlag.isPresent()) {
            final FamilieHendelseGrunnlagEntitet entitet = new FamilieHendelseGrunnlagEntitet(familieHendelseGrunnlag.get());
            entitet.setBekreftetHendelse(null);
            entitet.setOverstyrtHendelse(null);

            lagreOgFlush(nyBehandling, entitet);
        }
    }

    @Override
    public void slettAvklarteData(Behandling behandling, BehandlingLås lås) {
        fjernBekreftetData(behandling);

        verifiserBehandlingLås(lås);
        getEntityManager().flush();
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

    // sjekk lås og oppgrader til skriv
    protected void verifiserBehandlingLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }

    @Override
    public Optional<FamilieHendelseGrunnlag> hentFørsteVersjonAvAggregatHvisEksisterer(Behandling behandling) {
        final Optional<FamilieHendelseGrunnlagEntitet> førsteVersjonFamilieHendelseGrunnlag = getInitielVersjonAvFamilieHendelseGrunnlag(behandling);
        return førsteVersjonFamilieHendelseGrunnlag.isPresent() ? Optional.of(førsteVersjonFamilieHendelseGrunnlag.get()) : Optional.empty();
    }

    private Optional<FamilieHendelseGrunnlagEntitet> getInitielVersjonAvFamilieHendelseGrunnlag(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        final TypedQuery<FamilieHendelseGrunnlagEntitet> query = entityManager.createQuery("FROM FamilieHendelseGrunnlag gr " + // NOSONAR //$NON-NLS-1$
            "WHERE gr.behandling.id = :behandlingId " + //$NON-NLS-1$
            "ORDER BY gr.opprettetTidspunkt", FamilieHendelseGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandling.getId()); //$NON-NLS-1$
        return query.getResultList().stream().findFirst();
    }

    private FamilieHendelseGrunnlagBuilder opprettAggregatBuilderFor(Behandling behandling) {
        Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = hentAggregatHvisEksisterer(behandling);
        return FamilieHendelseGrunnlagBuilder.oppdatere(familieHendelseAggregat);
    }

    @Override
    public FamilieHendelseBuilder opprettBuilderFor(Behandling behandling) {
        Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = hentAggregatHvisEksisterer(behandling);
        final FamilieHendelseGrunnlagBuilder oppdatere = FamilieHendelseGrunnlagBuilder.oppdatere(familieHendelseAggregat);

        return opprettBuilderForBuilder(oppdatere);
    }

    /**
     * Baserer seg på typen under seg hvis det ikke finnes en tilsvarende.
     * F.eks Ber du om Overstyrt så vil denne basere seg på Hendelse i følgende rekkefølge
     * 1. Overstyrt
     * 2. Bekreftet
     * 3. Søknad
     *
     * @param aggregat nåværende aggregat
     * @return Builder
     */
    private FamilieHendelseBuilder opprettBuilderFor(Optional<FamilieHendelseGrunnlag> aggregat) {
        Objects.requireNonNull(aggregat, "aggregat"); //$NON-NLS-1$
        if (aggregat.isPresent()) {
            HendelseVersjonType type = utledTypeFor(aggregat);
            final FamilieHendelseGrunnlag hendelseAggregat = aggregat.get();
            final FamilieHendelseBuilder hendelseAggregat1 = getFamilieHendelseBuilderForType(hendelseAggregat, type);
            if (hendelseAggregat1 != null) {
                hendelseAggregat1.setType(type);
                return hendelseAggregat1;
            }
            throw FamilieHendelseFeil.FACTORY.ukjentVersjonstype().toException();
        }
        throw FamilieHendelseFeil.FACTORY.aggregatKanIkkeVæreNull().toException();
    }

    private FamilieHendelseBuilder getFamilieHendelseBuilderForType(FamilieHendelseGrunnlag aggregat, HendelseVersjonType type) {
        switch (type) {
            case SØKNAD:
                return FamilieHendelseBuilder.oppdatere(Optional.ofNullable(aggregat.getSøknadVersjon()), type);
            case BEKREFTET:
                if (!aggregat.getBekreftetVersjon().isPresent()) {
                    return getFamilieHendelseBuilderForType(aggregat, HendelseVersjonType.SØKNAD);
                } else {
                    return FamilieHendelseBuilder.oppdatere(aggregat.getBekreftetVersjon(), type);
                }
            case OVERSTYRT:
                if (!aggregat.getOverstyrtVersjon().isPresent()) {
                    return getFamilieHendelseBuilderForType(aggregat, HendelseVersjonType.BEKREFTET);
                } else {
                    return FamilieHendelseBuilder.oppdatere(aggregat.getOverstyrtVersjon(), type);
                }
        }
        return null;
    }

    private HendelseVersjonType utledTypeFor(Optional<FamilieHendelseGrunnlag> aggregat) {
        if (aggregat.isPresent()) {
            if (aggregat.get().getHarOverstyrteData()) {
                return HendelseVersjonType.OVERSTYRT;
            } else if (aggregat.get().getHarBekreftedeData() || aggregat.get().getSøknadVersjon() != null) {
                return HendelseVersjonType.BEKREFTET;
            } else if (aggregat.get().getSøknadVersjon() == null) {
                return HendelseVersjonType.SØKNAD;
            }
            throw new IllegalStateException("Utvikler feil."); //$NON-NLS-1$
        }
        return HendelseVersjonType.SØKNAD;
    }

    private FamilieHendelseBuilder opprettBuilderForBuilder(FamilieHendelseGrunnlagBuilder aggregatBuilder) {
        Objects.requireNonNull(aggregatBuilder, "aggregatBuilder"); //$NON-NLS-1$
        return opprettBuilderFor(Optional.ofNullable(aggregatBuilder.getKladd()));
    }

    @Override
    public Optional<Long> hentIdPåAktivFamiliehendelse(Behandling behandling) {
        return getAktivtFamilieHendelseGrunnlag(behandling)
            .map(FamilieHendelseGrunnlagEntitet::getId);
    }

    @Override
    public FamilieHendelseGrunnlag hentFamilieHendelserPåId(Long aggregatId) {
        Optional<FamilieHendelseGrunnlagEntitet> optGrunnlag = getVersjonAvFamiliehendelseGrunnlagPåId(
            aggregatId);
        return optGrunnlag.isPresent() ? optGrunnlag.get() : null;
    }

    private Optional<FamilieHendelseGrunnlagEntitet> getVersjonAvFamiliehendelseGrunnlagPåId(
        Long aggregatId) {
        Objects.requireNonNull(aggregatId, "aggregatId"); // NOSONAR $NON-NLS-1$ //$NON-NLS-1$
        final TypedQuery<FamilieHendelseGrunnlagEntitet> query = entityManager.createQuery("FROM FamilieHendelseGrunnlag gr " + // NOSONAR //$NON-NLS-1$
            "WHERE gr.id = :aggregatId ", FamilieHendelseGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("aggregatId", aggregatId); //$NON-NLS-1$
        return query.getResultList().stream().findFirst();
    }
}
