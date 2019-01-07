package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær.FraværResultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær.FraværResultatRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Provider for å hente ut repository for resultater
 */
@ApplicationScoped
public class ResultatRepositoryProviderImpl implements ResultatRepositoryProvider {

    private BehandlingRepository behandlingRepository;

    private KodeverkRepository kodeverkRepository;
    private FraværResultatRepository fraværResultatRepository;
    private OpptjeningRepository opptjeningRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BeregningsresultatRepository beregningsresultatRepository;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private UttakRepository uttakRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    ResultatRepositoryProviderImpl() {

    }

    @Inject
    public ResultatRepositoryProviderImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$

        BehandlingLåsRepositoryImpl behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
        this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        this.fraværResultatRepository = new FraværResultatRepositoryImpl(entityManager);
        this.behandlingRepository = new BehandlingRepositoryImpl(entityManager);
        this.opptjeningRepository = new OpptjeningRepositoryImpl(entityManager, behandlingRepository, kodeverkRepository);
        this.behandlingVedtakRepository = new BehandlingVedtakRepositoryImpl(entityManager, behandlingRepository);
        this.beregningsresultatRepository = new BeregningsresultatRepositoryImpl(entityManager);
        this.medlemskapVilkårPeriodeRepository = new MedlemskapVilkårPeriodeRepositoryImpl(entityManager);
        this.beregningsgrunnlagRepository = new BeregningsgrunnlagRepositoryImpl(entityManager, behandlingLåsRepository);
        this.uttakRepository = new UttakRepositoryImpl(entityManager, behandlingLåsRepository);
    }

    @Override
    public KodeverkRepository getKodeverkRepository() {
        return kodeverkRepository;
    }

    @Override
    public FraværResultatRepository getFraværResultatRepository() {
        return fraværResultatRepository;
    }

    @Override
    public BehandlingVedtakRepository getVedtakRepository() {
        return behandlingVedtakRepository;
    }

    @Override
    public OpptjeningRepository getOpptjeningRepository() {
        return opptjeningRepository;
    }

    @Override
    public BeregningsresultatRepository getBeregningsresultatRepository() {
        return beregningsresultatRepository;
    }

    @Override
    public MedlemskapVilkårPeriodeRepository getMedlemskapVilkårPeriodeRepository() {
        return medlemskapVilkårPeriodeRepository;
    }

    @Override
    public BeregningsgrunnlagRepository getBeregningsgrunnlagRepository() {
        return beregningsgrunnlagRepository;
    }

    @Override
    public UttakRepository getUttakRepository() {
        return uttakRepository;
    }

    @Override
    public BehandlingRepository getBehandlingRepository() {
        return behandlingRepository;
    }
}
