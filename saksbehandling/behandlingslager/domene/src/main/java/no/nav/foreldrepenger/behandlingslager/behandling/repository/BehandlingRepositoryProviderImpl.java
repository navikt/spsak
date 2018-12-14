package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Provider for å enklere å kunne hente ut ulike repository uten for mange injection points.
 */
@ApplicationScoped
public class BehandlingRepositoryProviderImpl implements BehandlingRepositoryProvider {

    private SykefraværRepositoryImpl sykefraværRepository;
    private EntityManager entityManager;
    private BehandlingLåsRepositoryImpl behandlingLåsRepository;
    private FagsakRepository fagsakRepository;
    private KodeverkRepositoryImpl kodeverkRepository;
    private AksjonspunktRepositoryImpl aksjonspunktRepository;
    private PersonopplysningRepository personopplysningRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private BehandlingsgrunnlagKodeverkRepositoryImpl behandlingsgrunnlagKodeverkRepository;
    private MedlemskapRepository medlemskapRepository;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;
    private HistorikkRepository historikkRepository;
    private SøknadRepository søknadRepository;
    private VergeRepository vergeRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningRepository beregningRepository;
    private UttakRepository uttakRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private VirksomhetRepository virksomhetRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private OpptjeningRepositoryImpl opptjeningRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private BehandlingRevurderingRepository behandlingRevurderingRepository;

    private BehandlingRepository behandlingRepository;
    private FagsakLåsRepository fagsakLåsRepository;
    private BehandlingskontrollRepositoryImpl behandlingskontrollRepository;

    BehandlingRepositoryProviderImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingRepositoryProviderImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;

        // FIXME (FC) Denne klassen må brytes opp.
        // I tillegg bør da repositories settes opp lazy. Vennligst ikke legg inn nye repo uten å avklare
        // først eller vurdere konsolidering.

        // kodeverk
        this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        this.vilkårKodeverkRepository = new VilkårKodeverkRepositoryImpl(entityManager, kodeverkRepository);
        this.behandlingsgrunnlagKodeverkRepository = new BehandlingsgrunnlagKodeverkRepositoryImpl(entityManager);

        // behandling repositories
        this.behandlingRepository = new BehandlingRepositoryImpl(entityManager);
        this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
        this.fagsakRepository = new FagsakRepositoryImpl(entityManager);
        this.aksjonspunktRepository = new AksjonspunktRepositoryImpl(entityManager, this.kodeverkRepository);
        this.fagsakLåsRepository = new FagsakLåsRepositoryImpl(entityManager);
        this.behandlingskontrollRepository = new BehandlingskontrollRepositoryImpl(this.behandlingRepository, this.behandlingLåsRepository, this.kodeverkRepository, entityManager);

        // behandling aggregater
        this.medlemskapRepository = new MedlemskapRepositoryImpl(entityManager);
        this.medlemskapVilkårPeriodeRepository = new MedlemskapVilkårPeriodeRepositoryImpl(entityManager);
        this.opptjeningRepository = new OpptjeningRepositoryImpl(entityManager, this.behandlingRepository, this.kodeverkRepository);
        this.personopplysningRepository = new PersonopplysningRepositoryImpl(entityManager);
        this.søknadRepository = new SøknadRepositoryImpl(entityManager, this.behandlingRepository);
        this.uttakRepository = new UttakRepositoryImpl(entityManager);
        this.vergeRepository = new VergeRepositoryImpl(entityManager);

        // inntekt arbeid ytelser
        this.inntektArbeidYtelseRepository = new InntektArbeidYtelseRepositoryImpl(entityManager);
        this.virksomhetRepository = new VirksomhetRepositoryImpl(entityManager);

        // behandling resultat aggregater
        this.beregningsgrunnlagRepository = new BeregningsgrunnlagRepositoryImpl(entityManager, behandlingLåsRepository);
        this.beregningRepository = new BeregningRepositoryImpl(entityManager, getBehandlingRepository());
        this.beregningsresultatFPRepository = new BeregningsresultatFPRepositoryImpl(entityManager);

        // behandling støtte repositories
        this.historikkRepository = new HistorikkRepositoryImpl(entityManager);
        this.behandlingVedtakRepository = new BehandlingVedtakRepositoryImpl(entityManager, behandlingRepository);
        this.behandlingRevurderingRepository = new BehandlingRevurderingRepositoryImpl(entityManager, this);

        // Sykefravær repository
        this.sykefraværRepository = new SykefraværRepositoryImpl(entityManager);

    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public BehandlingRepository getBehandlingRepository() {
        return behandlingRepository;
    }

    @Override
    public BehandlingskontrollRepository getBehandlingskontrollRepository() {
        return this.behandlingskontrollRepository;
    }
    
    @Override
    public KodeverkRepository getKodeverkRepository() {
        return kodeverkRepository;
    }

    @Override
    public PersonopplysningRepository getPersonopplysningRepository() {
        return personopplysningRepository;
    }

    @Override
    public AksjonspunktRepository getAksjonspunktRepository() {
        return aksjonspunktRepository;
    }

    @Override
    public VilkårKodeverkRepository getVilkårKodeverkRepository() {
        return vilkårKodeverkRepository;
    }

    @Override
    public BehandlingsgrunnlagKodeverkRepository getBehandlingsgrunnlagKodeverkRepository() {
        return behandlingsgrunnlagKodeverkRepository;
    }

    @Override
    public MedlemskapRepository getMedlemskapRepository() {
        return medlemskapRepository;
    }

    @Override
    public MedlemskapVilkårPeriodeRepository getMedlemskapVilkårPeriodeRepository() {
        return medlemskapVilkårPeriodeRepository;
    }

    @Override
    public BehandlingLåsRepositoryImpl getBehandlingLåsRepository() {
        return behandlingLåsRepository;
    }

    @Override
    public FagsakRepository getFagsakRepository() {
        // bridge metode før sammenkobling medBehandling
        return fagsakRepository;
    }

    @Override
    public HistorikkRepository getHistorikkRepository() {
        return historikkRepository;
    }

    @Override
    public VergeRepository getVergeGrunnlagRepository() {
        return vergeRepository;
    }

    @Override
    public SøknadRepository getSøknadRepository() {
        return søknadRepository;
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
    public InntektArbeidYtelseRepository getInntektArbeidYtelseRepository() {
        return inntektArbeidYtelseRepository;
    }

    @Override
    public VirksomhetRepository getVirksomhetRepository() {
        return virksomhetRepository;
    }

    @Override
    public BeregningRepository getBeregningRepository() {
        return beregningRepository;
    }

    @Override
    public BehandlingVedtakRepository getBehandlingVedtakRepository() {
        return behandlingVedtakRepository;
    }

    @Override
    public OpptjeningRepository getOpptjeningRepository() {
        return opptjeningRepository;
    }

    @Override
    public BeregningsresultatFPRepository getBeregningsresultatFPRepository() {
        return beregningsresultatFPRepository;
    }

    @Override
    public BehandlingRevurderingRepository getBehandlingRevurderingRepository() {
        return behandlingRevurderingRepository;
    }

    @Override
    public FagsakLåsRepository getFagsakLåsRepository() {
        return fagsakLåsRepository;
    }

    @Override
    public SykefraværRepository getSykefraværRepository() {
        return sykefraværRepository;
    }
}
