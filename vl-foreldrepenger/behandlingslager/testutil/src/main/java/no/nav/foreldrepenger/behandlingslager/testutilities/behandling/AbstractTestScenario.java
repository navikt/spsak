package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import no.nav.foreldrepenger.behandlingslager.aktør.BrukerTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling.Builder;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapBehandlingsgrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRegistrertEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepositoryStub;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.InnsynRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLås;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.repositorystub.BeregningsgrunnlagRepositoryStub;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkFraJson;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Default test scenario builder for å definere opp testdata med enkle defaults.
 * <p>
 * Oppretter en default behandling, inkludert default grunnlag med søknad + tomt innangsvilkårresultat.
 * <p>
 * Kan bruke settere (evt. legge til) for å tilpasse utgangspunktet.
 * <p>
 * Mer avansert bruk er ikke gitt at kan bruke denne
 * klassen.
 */
public abstract class AbstractTestScenario<S extends AbstractTestScenario<S>> {

    public static final String ADOPSJON = "adopsjon";
    public static final String FØDSEL = "fødsel";
    public static final String TERMINBEKREFTELSE = "terminbekreftelse";
    private static final AtomicLong FAKE_ID = new AtomicLong(100999L);
    private final FagsakBuilder fagsakBuilder;
    private final Map<Behandling, PersonopplysningGrunnlag> personopplysningMap = new IdentityHashMap<>();
    private final Map<Behandling, Verge> vergeMap = new IdentityHashMap<>();
    private final Map<Long, MedlemskapBehandlingsgrunnlagEntitet> medlemskapgrunnlag = new HashMap<>();
    private List<TestScenarioTillegg> testScenarioTilleggListe = new ArrayList<>();
    private ArgumentCaptor<Behandling> behandlingCaptor = ArgumentCaptor.forClass(Behandling.class);
    private ArgumentCaptor<Fagsak> fagsakCaptor = ArgumentCaptor.forClass(Fagsak.class);
    private InntektArbeidYtelseScenario iayScenario;
    private Behandling behandling;

    private Behandlingsresultat.Builder behandlingresultatBuilder;

    private Fagsak fagsak;
    private SøknadEntitet.Builder søknadBuilder;
    private VergeBuilder vergeBuilder;

    private VurdertMedlemskapBuilder vurdertMedlemskapBuilder;
    private BehandlingVedtak.Builder behandlingVedtakBuilder;
    private OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder;
    private BehandlingStegType startSteg;

    private Map<AksjonspunktDefinisjon, BehandlingStegType> aksjonspunktDefinisjoner = new HashMap<>();
    private VilkårResultatType vilkårResultatType = VilkårResultatType.IKKE_FASTSATT;
    private Map<VilkårType, VilkårUtfallType> vilkårTyper = new HashMap<>();
    private Beregning beregning;
    private List<RegistrertMedlemskapPerioder> medlemskapPerioder = new ArrayList<>();
    private Long fagsakId = nyId();
    private LocalDate behandlingstidFrist;
    private LocalDateTime opplysningerOppdatertTidspunkt;
    private String behandlendeEnhet;
    private BehandlingRepository mockBehandlingRepository;
    private BehandlingVedtak behandlingVedtak;
    private BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
    private OppgittRettighet oppgittRettighet;
    private OppgittFordeling oppgittFordeling;
    private AvklarteUttakDatoer avklarteUttakDatoer;

    // Registret og overstyrt personinfo
    private List<PersonInformasjon> personer;

    private Behandling originalBehandling;
    private BehandlingÅrsakType behandlingÅrsakType;
    private BehandlingRepositoryProvider repositoryProvider;
    private PerioderUtenOmsorg perioderUtenOmsorg;
    private PerioderAleneOmsorg perioderMedAleneomsorg;
    private no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder personInformasjonBuilder;

    protected AbstractTestScenario(FagsakYtelseType fagsakYtelseType, RelasjonsRolleType brukerRolle,
                                   NavBrukerKjønn kjønn) {
        this.fagsakBuilder = FagsakBuilder
            .nyFagsak(fagsakYtelseType, brukerRolle)
            .medSaksnummer(new Saksnummer(nyId() + ""))
            .medBrukerKjønn(kjønn);
    }

    protected AbstractTestScenario(FagsakYtelseType fagsakYtelseType, RelasjonsRolleType brukerRolle,
                                   NavBrukerKjønn kjønn, AktørId aktørId) {
        this.fagsakBuilder = FagsakBuilder
            .nyFagsak(fagsakYtelseType, brukerRolle)
            .medSaksnummer(new Saksnummer(nyId() + ""))
            .medBruker(new NavBrukerBuilder().medAktørId(aktørId).medKjønn(kjønn).build());
    }

    protected AbstractTestScenario(FagsakYtelseType fagsakYtelseType, RelasjonsRolleType brukerRolle,
                                   NavBruker navBruker) {
        this.fagsakBuilder = FagsakBuilder
            .nyFagsak(fagsakYtelseType, brukerRolle)
            .medSaksnummer(new Saksnummer(nyId() + ""))
            .medBruker(navBruker);
    }

    static long nyId() {
        return FAKE_ID.getAndIncrement();
    }

    private BehandlingRepository lagBasicMockBehandlingRepository(BehandlingRepositoryProvider repositoryProvider) {
        BehandlingRepository behandlingRepository = mock(BehandlingRepository.class);

        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);

        AksjonspunktRepository aksjonspunktRepository = Mockito.spy(new AksjonspunktRepositoryImpl(null));

        // støtter ikke denne, da behandling mulig ikke har aksjonspunkt
        Mockito.doNothing().when(aksjonspunktRepository).setToTrinnsBehandlingKreves(Mockito.any(Behandling.class), Mockito.any());
        Mockito.doAnswer(new Answer<AksjonspunktDefinisjon>() {
            private List<AksjonspunktDefinisjon> defs;

            @Override
            public AksjonspunktDefinisjon answer(InvocationOnMock invocation) {
                String kode = invocation.getArgument(0);
                if (defs == null) {
                    defs = new KodeverkFraJson().lesKodeverkFraFil(AksjonspunktDefinisjon.class);
                }
                return defs.stream().filter(a -> a.getKode().equals(kode))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ukjent AksjonspunktDefinisjon kode=" + kode));
            }
        }).when(aksjonspunktRepository).finnAksjonspunktDefinisjon(Mockito.any());

        FagsakRepository mockFagsakRepository = mockFagsakRepository();
        InnsynRepository mockInnsynRepository = mockInnsynRepository();
        PersonopplysningRepository mockPersonopplysningRepository = lagMockPersonopplysningRepository();
        MedlemskapRepository mockMedlemskapRepository = lagMockMedlemskapRepository();
        BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository = mockBehandlingsgrunnlagKodeverkRepository();
        KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        VergeRepository mockVergeRepository = lagMockVergeRepository();
        SøknadRepository søknadRepository = mockSøknadRepository();
        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = getIayScenario().mockInntektArbeidYtelseRepository();
        VirksomhetRepository virksomhetRepository = InntektArbeidYtelseScenario.mockVirksomhetRepository();
        YtelsesFordelingRepository ytelsesFordelingRepository = mockYtelsesFordelingRepository();
        MottatteDokumentRepository mottatteDokumentRepository = mockMottatteDokumentRepository();
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = mockBeregningsgrunnlagRepository();
        BeregningRepository beregningRepository = mockBeregningRepository();
        OpptjeningRepository opptjeningRepository = Mockito.mock(OpptjeningRepository.class);
        BeregningsresultatFPRepository beregningsresultatFPRepository = mockBeregningsresultatFPRepository();
        FagsakLåsRepository fagsakLåsRepository = mockFagsakLåsRepository();

        BehandlingLåsRepository behandlingLåsReposiory = mockBehandlingLåsRepository();

        BehandlingVedtakRepository behandlingVedtakRepository = mockBehandlingVedtakRepository();
        // ikke ideelt å la mocks returnere mocks, men forenkler enormt mye test kode, forhindrer feil oppsett, så det
        // blir enklere å refactorere

        VilkårKodeverkRepository mockVilkårKodeverkRepository = mockVilkårKodeverkRepository(kodeverkRepository);
        when(repositoryProvider.getKodeverkRepository()).thenReturn(kodeverkRepository);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
        when(repositoryProvider.getFagsakRepository()).thenReturn(mockFagsakRepository);
        when(repositoryProvider.getBehandlingsgrunnlagKodeverkRepository()).thenReturn(behandlingsgrunnlagKodeverkRepository);
        when(repositoryProvider.getAksjonspunktRepository()).thenReturn(aksjonspunktRepository);
        when(repositoryProvider.getPersonopplysningRepository()).thenReturn(mockPersonopplysningRepository);
        when(repositoryProvider.getVilkårKodeverkRepository()).thenReturn(mockVilkårKodeverkRepository);
        when(repositoryProvider.getMedlemskapRepository()).thenReturn(mockMedlemskapRepository);
        when(repositoryProvider.getSøknadRepository()).thenReturn(søknadRepository);
        when(repositoryProvider.getVergeGrunnlagRepository()).thenReturn(mockVergeRepository);
        when(repositoryProvider.getBeregningRepository()).thenReturn(beregningRepository);
        when(repositoryProvider.getBehandlingVedtakRepository()).thenReturn(behandlingVedtakRepository);
        when(repositoryProvider.getInntektArbeidYtelseRepository()).thenReturn(inntektArbeidYtelseRepository);
        when(repositoryProvider.getVirksomhetRepository()).thenReturn(virksomhetRepository);
        when(repositoryProvider.getYtelsesFordelingRepository()).thenReturn(ytelsesFordelingRepository);
        when(repositoryProvider.getMottatteDokumentRepository()).thenReturn(mottatteDokumentRepository);
        when(repositoryProvider.getInnsynRepository()).thenReturn(mockInnsynRepository);
        when(repositoryProvider.getBeregningsgrunnlagRepository()).thenReturn(beregningsgrunnlagRepository);
        when(repositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
        when(repositoryProvider.getFagsakLåsRepository()).thenReturn(fagsakLåsRepository);
        when(repositoryProvider.getBehandlingLåsRepository()).thenReturn(behandlingLåsReposiory);
        when(repositoryProvider.getBeregningsresultatFPRepository()).thenReturn(beregningsresultatFPRepository);
        lagreTilleggsScenarier(repositoryProvider);

        return behandlingRepository;
    }

    private BehandlingLåsRepository mockBehandlingLåsRepository() {
        return new BehandlingLåsRepository() {

            @Override
            public BehandlingLås taLås(Long behandlingId) {
                return new BehandlingLås(behandlingId);
            }

            @Override
            public void oppdaterLåsVersjon(BehandlingLås behandlingLås) {
            }
        };
    }

    private FagsakLåsRepository mockFagsakLåsRepository() {
        return new FagsakLåsRepository() {
            @Override
            public FagsakLås taLås(Long fagsakId) {
                return new FagsakLås(fagsakId) {

                };
            }

            @Override
            public FagsakLås taLås(Fagsak fagsak) {
                return new FagsakLås(fagsak.getId()) {

                };
            }

            @Override
            public void oppdaterLåsVersjon(FagsakLås fagsakLås) {

            }
        };
    }

    private BeregningsgrunnlagRepository mockBeregningsgrunnlagRepository() {
        return new BeregningsgrunnlagRepositoryStub();
    }

    private BeregningsresultatFPRepository mockBeregningsresultatFPRepository() {
        return new BeregningsresultatFPRepositoryStub();
    }

    private BehandlingVedtakRepository mockBehandlingVedtakRepository() {
        BehandlingVedtakRepository behandlingVedtakRepository = mock(BehandlingVedtakRepository.class);
        BehandlingVedtak behandlingVedtak = mockBehandlingVedtak();
        when(behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(Mockito.any())).thenReturn(Optional.of(behandlingVedtak));

        return behandlingVedtakRepository;
    }

    public BehandlingVedtak mockBehandlingVedtak() {
        if (behandlingVedtak == null) {
            behandlingVedtak = Mockito.mock(BehandlingVedtak.class);
        }
        return behandlingVedtak;
    }

    private BeregningRepository mockBeregningRepository() {
        return mock(BeregningRepository.class);
    }

    private SøknadRepository mockSøknadRepository() {
        return new SøknadRepository() {

            private Søknad søknad;

            @Override
            public Søknad hentSøknad(Behandling behandling1) {
                return søknad;
            }

            @Override
            public Optional<Søknad> hentSøknadHvisEksisterer(Long behandlingId) {
                return Optional.ofNullable(søknad);
            }

            @Override
            public Søknad hentFørstegangsSøknad(Behandling behandling) {
                return søknad;
            }

            @Override
            public Søknad hentFørstegangsSøknad(Long behandlingId) {
                return søknad;
            }

            @Override
            public Søknad hentSøknad(Long behandlingId) {
                return søknad;
            }

            @Override
            public Optional<Søknad> hentSøknadHvisEksisterer(Behandling behandling1) {
                return Optional.ofNullable(søknad);
            }

            @Override
            public void lagreOgFlush(Behandling behandling, Søknad søknad1) {
                this.søknad = søknad1;
            }

        };
    }

    private YtelsesFordelingRepository mockYtelsesFordelingRepository() {
        YtelsesFordelingRepository ytelsesRepo = Mockito.mock(YtelsesFordelingRepository.class);
        YtelseFordelingAggregat mockAggregat = mockYtelseFordelingAggregat();
        when(ytelsesRepo.hentAggregatHvisEksisterer(Mockito.any())).thenReturn(Optional.of(mockAggregat));
        return ytelsesRepo;
    }

    private YtelseFordelingAggregat mockYtelseFordelingAggregat() {
        YtelseFordelingAggregat ytelseFordelingAggregat = Mockito.mock(YtelseFordelingAggregat.class);
        when(ytelseFordelingAggregat.getOppgittFordeling()).thenReturn(mockOppgittFordeling());
        return ytelseFordelingAggregat;
    }

    public OppgittFordeling mockOppgittFordeling() {
        return new OppgittFordeling() {
            @Override
            public List<OppgittPeriode> getOppgittePerioder() {
                return Collections.singletonList(OppgittPeriodeBuilder.ny()
                    .medPeriode(LocalDate.now(FPDateUtil.getOffset()), LocalDate.now(FPDateUtil.getOffset()).plusWeeks(6))
                    .medPeriodeType(UttakPeriodeType.MØDREKVOTE).build());
            }

            @Override
            public boolean getErAnnenForelderInformert() {
                return false;
            }
        };
    }

    private MottatteDokumentRepository mockMottatteDokumentRepository() {
        MottatteDokumentRepository dokumentRepository = Mockito.mock(MottatteDokumentRepository.class);
        return dokumentRepository;
    }


    /**
     * Hjelpe metode for å håndtere mock repository.
     */
    public BehandlingRepository mockBehandlingRepository() {
        if (mockBehandlingRepository != null) {
            return mockBehandlingRepository;
        }
        repositoryProvider = mock(BehandlingRepositoryProvider.class);
        BehandlingRepository behandlingRepository = lagBasicMockBehandlingRepository(repositoryProvider);

        when(behandlingRepository.hentBehandling(Mockito.any())).thenAnswer(a -> {
            return behandling;
        });
        when(behandlingRepository.finnUnikBehandlingForBehandlingId(Mockito.any())).thenAnswer(a -> Optional.of(behandling));
        when(behandlingRepository.hentSisteBehandlingForFagsakId(Mockito.any(), Mockito.any(BehandlingType.class)))
            .thenAnswer(a -> Optional.of(behandling));
        when(behandlingRepository.hentSisteBehandlingForFagsakId(Mockito.any()))
            .thenAnswer(a -> Optional.of(behandling));
        when(behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(Mockito.any()))
            .thenAnswer(a -> Optional.of(behandling));

        when(behandlingRepository.taSkriveLås(behandlingCaptor.capture())).thenAnswer((Answer<BehandlingLås>) invocation -> {
            Behandling beh = invocation.getArgument(0);
            return new BehandlingLås(beh.getId()) {
            };
        });

        when(behandlingRepository.hentSistOppdatertTidspunkt(Mockito.any(Behandling.class)))
            .thenAnswer(a -> Optional.ofNullable(opplysningerOppdatertTidspunkt));

        when(behandlingRepository.lagre(behandlingCaptor.capture(), Mockito.any()))
            .thenAnswer((Answer<Long>) invocation -> {
                Behandling beh = invocation.getArgument(0);
                Long id = beh.getId();
                if (id == null) {
                    id = nyId();
                    Whitebox.setInternalState(beh, "id", id);
                }

                beh.getAksjonspunkter().forEach(punkt -> Whitebox.setInternalState(punkt, "id", nyId()));

                return id;
            });

        mockBehandlingRepository = behandlingRepository;
        return behandlingRepository;
    }

    public BehandlingRepositoryProvider mockBehandlingRepositoryProvider() {
        mockBehandlingRepository();
        return repositoryProvider;
    }

    private VilkårKodeverkRepository mockVilkårKodeverkRepository(KodeverkRepository kodeverkRepository) {
        VilkårKodeverkRepositoryImpl repo = Mockito.spy(VilkårKodeverkRepositoryImpl.class);
        Whitebox.setInternalState(repo, "kodeverkRepository", kodeverkRepository);

        Mockito.doAnswer(a -> Arrays.asList(VilkårType.MEDLEMSKAPSVILKÅRET)).when(repo).finnVilkårTypeListe("1025");
        return repo;
    }

    private BehandlingsgrunnlagKodeverkRepository mockBehandlingsgrunnlagKodeverkRepository() {
        BehandlingsgrunnlagKodeverkRepository repository = Mockito.mock(BehandlingsgrunnlagKodeverkRepository.class);
        when(repository.finnHøyestRangertRegion(Mockito.any())).thenReturn(Region.UDEFINERT);
        return repository;
    }

    public PersonopplysningRepository mockPersonopplysningRepository() {
        return mockBehandlingRepositoryProvider().getPersonopplysningRepository();
    }

    public InntektArbeidYtelseRepository getMockInntektArbeidYtelseRepository() {
        return mockBehandlingRepositoryProvider().getInntektArbeidYtelseRepository();
    }

    public MedlemskapRepository mockMedlemskapRepository() {
        return mockBehandlingRepositoryProvider().getMedlemskapRepository();
    }

    private MedlemskapRepository lagMockMedlemskapRepository() {
        MedlemskapRepository dummy = new MedlemskapRepositoryImpl(null) {
            @Override
            public void lagreOgFlush(Optional<MedlemskapBehandlingsgrunnlagEntitet> eksisterendeGrunnlag,
                                     MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag) {
                Behandling b = nyttGrunnlag.getBehandling();
                assert b != null && b.getId() != null : "behandlingId er null!";
                medlemskapgrunnlag.put(b.getId(), nyttGrunnlag);
            }

            @Override
            public void lagreMedlemskapRegistrert(MedlemskapRegistrertEntitet ny) {
                // ignore, tracker kun grunnlag for mock
            }

            @Override
            public void lagreOppgittTilknytning(OppgittTilknytningEntitet ny) {
                // ignore, tracker kun grunnlag for mock
            }

            @Override
            public void lagreVurdertMedlemskap(VurdertMedlemskapEntitet ny) {
                // ignore, tracker kun grunnlag for mock
            }

            @Override
            protected BehandlingLås taLås(Behandling behandling) {
                return null;
            }

            @Override
            protected void oppdaterLås(BehandlingLås lås) {
                // NO-OP i mock
            }

            @Override
            public void slettAvklarteMedlemskapsdata(Behandling behandling, BehandlingLås lås) {
                // NO-OP i mock
            }

            @Override
            protected Optional<MedlemskapBehandlingsgrunnlagEntitet> getAktivtBehandlingsgrunnlag(Long behandlingId) {
                assert behandlingId != null : "behandlingId er null!";
                return Optional.ofNullable(medlemskapgrunnlag.get(behandlingId));
            }
        };
        return Mockito.spy(dummy);
    }

    private PersonopplysningRepository lagMockPersonopplysningRepository() {
        return new MockPersonopplysningRepository();
    }

    public void medDefaultInntektArbeidYtelse() {
        getIayScenario().medDefaultInntektArbeidYtelse();
    }

    private InntektArbeidYtelseScenario getIayScenario() {
        if (iayScenario == null) {
            iayScenario = new InntektArbeidYtelseScenario();
        }
        return iayScenario;
    }

    private VergeRepository lagMockVergeRepository() {
        VergeRepository vRepo = mock(VergeRepository.class);

        Mockito.doAnswer(invocation -> {
            Behandling behandling = invocation.getArgument(0);
            VergeBuilder builder = invocation.getArgument(1);
            vergeMap.remove(behandling);
            VergeEntitet verge = builder.build();
            vergeMap.put(behandling, verge);
            return null;
        }).when(vRepo)
            .lagreOgFlush(Mockito.any(Behandling.class), Mockito.any(VergeBuilder.class));

        Mockito.doAnswer(invocation -> {
            Behandling behandling = invocation.getArgument(0);
            if (vergeMap.containsKey(behandling)) {
                Verge verge = vergeMap.get(behandling);
                return Optional.of(new VergeAggregat(verge));
            }
            return Optional.empty();

        }).when(vRepo)
            .hentAggregat(Mockito.any(Behandling.class));

        return vRepo;
    }

    private InnsynRepository mockInnsynRepository() {
        InnsynRepository innsynRepository = mock(InnsynRepository.class);
        when(innsynRepository.hentForBehandling(Mockito.anyLong())).thenAnswer(a -> Collections.emptyList());
        when(innsynRepository.hentDokumenterForInnsyn(Mockito.anyLong())).thenAnswer(a -> Collections.emptyList());
        return innsynRepository;
    }

    public FagsakRepository mockFagsakRepository() {
        FagsakRepository fagsakRepository = mock(FagsakRepository.class);
        when(fagsakRepository.finnEksaktFagsak(Mockito.anyLong())).thenAnswer(a -> fagsak);
        when(fagsakRepository.finnUnikFagsak(Mockito.anyLong())).thenAnswer(a -> Optional.of(fagsak));
        when(fagsakRepository.hentSakGittSaksnummer(Mockito.any(Saksnummer.class))).thenAnswer(a -> Optional.of(fagsak));
        when(fagsakRepository.hentForBruker(Mockito.any(AktørId.class))).thenAnswer(a -> singletonList(fagsak));
        when(fagsakRepository.opprettNy(fagsakCaptor.capture())).thenAnswer(invocation -> {
            Fagsak fagsak = invocation.getArgument(0); // NOSONAR
            Long id = fagsak.getId();
            if (id == null) {
                id = fagsakId;
                Whitebox.setInternalState(fagsak, "id", id);
            }
            return id;
        });

        // oppdater fagsakstatus
        Mockito.doAnswer(invocation -> {
            FagsakStatus status = invocation.getArgument(1);
            Whitebox.setInternalState(fagsak, "fagsakStatus", status);
            return null;
        }).when(fagsakRepository)
            .oppdaterFagsakStatus(eq(fagsakId), Mockito.any(FagsakStatus.class));

        return fagsakRepository;
    }

    public Fagsak lagreFagsak(BehandlingRepositoryProvider repositoryProvider) {
        lagFagsak(repositoryProvider.getFagsakRepository());
        return fagsak;
    }

    public Behandling lagre(BehandlingRepositoryProvider repositoryProvider) {
        build(repositoryProvider.getBehandlingRepository(), repositoryProvider);
        return behandling;
    }

    BehandlingRepository lagMockedRepositoryForOpprettingAvBehandlingInternt() {
        if (mockBehandlingRepository != null && behandling != null) {
            return mockBehandlingRepository;
        }
        validerTilstandVedMocking();

        mockBehandlingRepository = mockBehandlingRepository();

        lagre(repositoryProvider); // NOSONAR //$NON-NLS-1$
        Whitebox.setInternalState(behandling.getType(), "ekstraData", "{ \"behandlingstidFristUker\" : 6, \"behandlingstidVarselbrev\" : \"N\" }");
        return mockBehandlingRepository;
    }

    public Behandling lagMocked() {
        lagMockedRepositoryForOpprettingAvBehandlingInternt();
        return behandling;
    }

    public void buildAvsluttet(BehandlingRepository behandlingRepo, BehandlingRepositoryProvider repositoryProvider) {
        Builder behandlingBuilder = grunnBuild(repositoryProvider);

        behandling = behandlingBuilder.medAvsluttetDato(LocalDateTime.now(FPDateUtil.getOffset())).build();
        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        lagrePersonopplysning(repositoryProvider, behandling);
        Whitebox.setInternalState(behandling, "status", BehandlingStatus.AVSLUTTET);

        Behandlingsresultat.Builder builder = Behandlingsresultat.builder();

        // opprett og lagre resulater på behandling
        lagreBehandlingsresultatOgVilkårResultat(repositoryProvider, lås);
        lagreBeregningsresultat(repositoryProvider.getBeregningRepository(), lås);
        builder.medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).medAvslagarsakFritekst("Testavslag")
            .medAvslagsårsak(Avslagsårsak.ENGANGSSTØNAD_ER_ALLEREDE_UTBETALT_TIL_FAR_MEDMOR).buildFor(behandling);

        behandlingRepo.lagre(behandling, lås);
        lagreTilleggsScenarier(repositoryProvider);
    }

    private void lagrePersonopplysning(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();

        if (personer != null && !personer.isEmpty()) {
            personer.stream().filter(e -> e.getType().equals(PersonopplysningVersjonType.REGISTRERT))
                .findFirst().ifPresent(e -> lagrePersoninfo(behandling, e, personopplysningRepository));

            personer.stream().filter(a -> a.getType().equals(PersonopplysningVersjonType.OVERSTYRT))
                .findFirst().ifPresent(b -> {
                if (personer.stream().noneMatch(c -> c.getType().equals(PersonopplysningVersjonType.REGISTRERT))) {
                    // Sjekker om overstyring er ok, mao om registeropplysninger finnes
                    personopplysningRepository.opprettBuilderForOverstyring(behandling);
                }
                lagrePersoninfo(behandling, b, personopplysningRepository);
            });

        } else {
            PersonInformasjon registerInformasjon = PersonInformasjon.builder(PersonopplysningVersjonType.REGISTRERT)
                .leggTilPersonopplysninger(
                    no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning.builder()
                        .aktørId(behandling.getAktørId())
                        .navn("Forelder")
                        .brukerKjønn(getKjønnFraFagsak())
                        .fødselsdato(LocalDate.now().minusYears(25))
                        .sivilstand(SivilstandType.UOPPGITT)
                        .region(Region.NORDEN))
                .build();
            lagrePersoninfo(behandling, registerInformasjon, personopplysningRepository);
        }
    }

    private void lagrePersoninfo(Behandling behandling, PersonInformasjon personInformasjon, PersonopplysningRepository repository) {
        Objects.nonNull(behandling);
        Objects.nonNull(personInformasjon);

        if (personInformasjon.getType().equals(PersonopplysningVersjonType.REGISTRERT)) {
            lagreRegisterPersoninfo(behandling, personInformasjon, repository);
        } else {
            lagreOverstyrtPersoninfo(behandling, personInformasjon, repository);
        }
    }

    private void lagreRegisterPersoninfo(Behandling behandling, PersonInformasjon personInformasjon, PersonopplysningRepository repository) {
        lagrePersoninfo(behandling, repository.opprettBuilderForRegisterdata(behandling), personInformasjon, repository);
    }

    private void lagreOverstyrtPersoninfo(Behandling behandling, PersonInformasjon personInformasjon, PersonopplysningRepository repository) {
        lagrePersoninfo(behandling, repository.opprettBuilderForOverstyring(behandling), personInformasjon, repository);
    }

    private void lagrePersoninfo(Behandling behandling, PersonInformasjonBuilder personInformasjonBuilder, PersonInformasjon personInformasjon,
                                 PersonopplysningRepository repository) {
        personInformasjon.getPersonopplysninger().forEach(e -> {
            PersonInformasjonBuilder.PersonopplysningBuilder builder = personInformasjonBuilder.getPersonopplysningBuilder(e.getAktørId());
            builder.medNavn(e.getNavn())
                .medFødselsdato(e.getFødselsdato())
                .medDødsdato(e.getDødsdato())
                .medKjønn(e.getBrukerKjønn())
                .medRegion(e.getRegion())
                .medSivilstand(e.getSivilstand());

            personInformasjonBuilder.leggTil(builder);
        });

        personInformasjon.getAdresser().forEach(e -> {
            PersonInformasjonBuilder.AdresseBuilder builder = personInformasjonBuilder.getAdresseBuilder(e.getAktørId(), e.getPeriode(), e.getAdresseType());
            builder.medAdresselinje1(e.getAdresselinje1())
                .medAdresselinje2(e.getAdresselinje2())
                .medAdresselinje3(e.getAdresselinje3())
                .medAdresselinje4(e.getAdresselinje4())
                .medLand(e.getLand())
                .medPostnummer(e.getPostnummer())
                .medPoststed(e.getPoststed());

            personInformasjonBuilder.leggTil(builder);
        });

        personInformasjon.getPersonstatuser().forEach(e -> {
            PersonInformasjonBuilder.PersonstatusBuilder builder = personInformasjonBuilder.getPersonstatusBuilder(e.getAktørId(), e.getPeriode());
            builder.medPersonstatus(e.getPersonstatus());
            personInformasjonBuilder.leggTil(builder);
        });

        personInformasjon.getStatsborgerskap().forEach(e -> {
            BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository = mockBehandlingsgrunnlagKodeverkRepository();
            Region region = behandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(Arrays.asList(e.getStatsborgerskap().getKode()));
            PersonInformasjonBuilder.StatsborgerskapBuilder builder = personInformasjonBuilder.getStatsborgerskapBuilder(e.getAktørId(), e.getPeriode(),
                e.getStatsborgerskap(), region);
            personInformasjonBuilder.leggTil(builder);
        });

        personInformasjon.getRelasjoner().forEach(e -> {
            PersonInformasjonBuilder.RelasjonBuilder builder = personInformasjonBuilder.getRelasjonBuilder(e.getAktørId(), e.getTilAktørId(),
                e.getRelasjonsrolle());
            builder.harSammeBosted(e.getHarSammeBosted());
            personInformasjonBuilder.leggTil(builder);
        });

        repository.lagre(behandling, personInformasjonBuilder);
    }

    private void lagreVerge(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        if (vergeBuilder != null) {
            VergeRepository vergeRepo = repositoryProvider.getVergeGrunnlagRepository();
            vergeRepo.lagreOgFlush(behandling, vergeBuilder);
        }
    }

    protected void validerTilstandVedMocking() {
        if (startSteg != null) {
            throw new IllegalArgumentException(
                "Kan ikke sette startSteg ved mocking siden dette krever Kodeverk.  Bruk ManipulerInternBehandling til å justere etterpå.");
        }
    }

    @SuppressWarnings("unchecked")
    public S medSøknadDato(LocalDate søknadsdato) {
        medSøknad().medSøknadsdato(søknadsdato);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medDefaultVerge() {
        vergeBuilder = new VergeBuilder();
        vergeBuilder.medBruker(new NavBrukerBuilder().build());
        vergeBuilder.medBrevMottaker(BrevMottaker.VERGE);
        vergeBuilder.medVergeType(VergeType.BARN);
        return (S) this;
    }

    private void build(BehandlingRepository behandlingRepo, BehandlingRepositoryProvider repositoryProvider) {
        if (behandling != null) {
            throw new IllegalStateException("build allerede kalt.  Hent Behandling via getBehandling eller opprett nytt scenario.");
        }
        Builder behandlingBuilder = grunnBuild(repositoryProvider);

        this.behandling = behandlingBuilder.build();

        if (startSteg != null) {
            new InternalManipulerBehandlingImpl(repositoryProvider).forceOppdaterBehandlingSteg(behandling, startSteg);
        }

        leggTilAksjonspunkter(behandling, repositoryProvider);

        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        lagrePersonopplysning(repositoryProvider, behandling);
        lagreMedlemskapOpplysninger(repositoryProvider, behandling);
        lagreVerge(repositoryProvider, behandling);
        if (iayScenario != null) {
            iayScenario.lagreVirksomhet(repositoryProvider);
            iayScenario.lagreOppgittOpptjening(repositoryProvider, behandling);
            iayScenario.lagreOpptjening(repositoryProvider, behandling);
        }
        lagreYtelseFordelingOpplysninger(repositoryProvider, behandling);
        lagreSøknad(repositoryProvider);
        // opprett og lagre resulater på behandling
        lagreBehandlingsresultatOgVilkårResultat(repositoryProvider, lås);
        lagreBeregningsresultat(repositoryProvider.getBeregningRepository(), lås);
        lagreTilleggsScenarier(repositoryProvider);

        if (this.opplysningerOppdatertTidspunkt != null) {
            behandlingRepo.oppdaterSistOppdatertTidspunkt(this.behandling, this.opplysningerOppdatertTidspunkt);
        }
    }

    private void leggTilAksjonspunkter(Behandling behandling, BehandlingRepositoryProvider repositoryProvider) {
        aksjonspunktDefinisjoner.forEach(
            (apDef, stegType) -> {
                if (stegType != null) {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef, stegType);
                } else {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef);
                }
            });
    }

    private void lagreSøknad(BehandlingRepositoryProvider repositoryProvider) {
        if (søknadBuilder != null) {
            final SøknadRepository søknadRepository = repositoryProvider.getSøknadRepository();
            søknadRepository.lagreOgFlush(behandling, søknadBuilder.build());
        }
    }

    private void lagreMedlemskapOpplysninger(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        repositoryProvider.getMedlemskapRepository().lagreMedlemskapRegisterOpplysninger(behandling, medlemskapPerioder);

        VurdertMedlemskap vurdertMedlemskap = medMedlemskap().build();
        repositoryProvider.getMedlemskapRepository().lagreMedlemskapVurdering(behandling, vurdertMedlemskap);
        if (oppgittTilknytningBuilder != null) {
            final OppgittTilknytning oppgittTilknytning = medOppgittTilknytning().build();
            repositoryProvider.getMedlemskapRepository().lagreOppgittTilkytning(behandling, oppgittTilknytning);
            final Optional<MedlemskapAggregat> medlemskapAggregat = repositoryProvider.getMedlemskapRepository().hentMedlemskap(behandling);
            final Optional<OppgittTilknytning> oppgittTilknytningOptional = medlemskapAggregat
                .flatMap(MedlemskapAggregat::getOppgittTilknytning);
            if (søknadBuilder != null) {
                oppgittTilknytningOptional
                    .ifPresent(oppgittTilknytning1 -> medSøknad().medOppgittTilknytning(oppgittTilknytning1));
            }
        }
    }

    private void lagreYtelseFordelingOpplysninger(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        lagreOppgittRettighet(repositoryProvider, behandling);
        if (oppgittFordeling != null) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);

            YtelseFordelingAggregat ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);
            OppgittFordeling oppgittFordeling = ytelseFordelingAggregat.getOppgittFordeling();
            if (søknadBuilder != null) {
                medSøknad().medFordeling(oppgittFordeling);
            }
        }
        if (avklarteUttakDatoer != null) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, avklarteUttakDatoer);
        }
        if (perioderUtenOmsorg != null) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, perioderUtenOmsorg);
        }
        if (perioderMedAleneomsorg != null) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, perioderMedAleneomsorg);
        }
    }

    private void lagreOppgittRettighet(BehandlingRepositoryProvider repositoryProvider, Behandling behandling) {
        if (oppgittRettighet != null) {
            repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittRettighet);

            YtelseFordelingAggregat ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);
            OppgittRettighet oppgittRettighet = ytelseFordelingAggregat.getOppgittRettighet();
            if (søknadBuilder != null) {
                medSøknad().medRettighet(oppgittRettighet);
            }
        }
    }

    private Builder grunnBuild(BehandlingRepositoryProvider repositoryProvider) {
        FagsakRepository fagsakRepo = repositoryProvider.getFagsakRepository();

        lagFagsak(fagsakRepo);

        // oppprett og lagre behandling
        Builder behandlingBuilder;
        if (originalBehandling == null) {
            behandlingBuilder = Behandling.nyBehandlingFor(fagsak, behandlingType);
        } else {
            behandlingBuilder = Behandling.fraTidligereBehandling(originalBehandling, behandlingType)
                .medBehandlingÅrsak(BehandlingÅrsak.builder(behandlingÅrsakType)
                    .medOriginalBehandling(originalBehandling));
        }

        if (behandlingstidFrist != null) {
            behandlingBuilder.medBehandlingstidFrist(behandlingstidFrist);
        }

        if (behandlendeEnhet != null) {
            behandlingBuilder.medBehandlendeEnhet(new OrganisasjonsEnhet(behandlendeEnhet, null));
        }

        return behandlingBuilder;

    }

    protected void lagFagsak(FagsakRepository fagsakRepo) {
        // opprett og lagre fagsak. Må gjøres før kan opprette behandling
        if (!Mockito.mockingDetails(fagsakRepo).isMock()) {
            final EntityManager entityManager = (EntityManager) Whitebox.getInternalState(fagsakRepo, "entityManager");
            if (entityManager != null) {
                BrukerTjeneste brukerTjeneste = new BrukerTjeneste(new NavBrukerRepositoryImpl(entityManager));
                final Personinfo personinfo = new Personinfo.Builder()
                    .medFødselsdato(LocalDate.now())
                    .medPersonIdent(PersonIdent.fra("123451234123"))
                    .medNavn("asdf")
                    .medAktørId(fagsakBuilder.getBrukerBuilder().getAktørId())
                    .medNavBrukerKjønn(getKjønnFraFagsak())
                    .medForetrukketSpråk(
                        fagsakBuilder.getBrukerBuilder().getSpråkkode() != null ? fagsakBuilder.getBrukerBuilder().getSpråkkode() : Språkkode.nb)
                    .build();
                final NavBruker navBruker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
                fagsakBuilder.medBruker(navBruker);
            }
        }
        fagsak = fagsakBuilder.build();
        Long fagsakId = fagsakRepo.opprettNy(fagsak); // NOSONAR //$NON-NLS-1$
        fagsak.setId(fagsakId);
    }

    private NavBrukerKjønn getKjønnFraFagsak() {
        return fagsakBuilder.getBrukerBuilder().getKjønn() != null ? fagsakBuilder.getBrukerBuilder().getKjønn()
            : (RelasjonsRolleType.erMor(fagsakBuilder.getRolle()) || RelasjonsRolleType.erMedmor(fagsakBuilder.getRolle()) ? NavBrukerKjønn.KVINNE
            : NavBrukerKjønn.MANN);
    }

    private void lagreBehandlingsresultatOgVilkårResultat(BehandlingRepositoryProvider repoProvider, BehandlingLås lås) {
        // opprett og lagre behandlingsresultat med VilkårResultat og BehandlingVedtak
        Behandlingsresultat behandlingsresultat = (behandlingresultatBuilder == null ? Behandlingsresultat.builderForInngangsvilkår()
            : behandlingresultatBuilder).buildFor(behandling);

        VilkårResultat.Builder inngangsvilkårBuilder = VilkårResultat
            .builderFraEksisterende(behandlingsresultat.getVilkårResultat())
            .medVilkårResultatType(vilkårResultatType);

        vilkårTyper.forEach((vilkårType, vilkårUtfallType) -> {
            inngangsvilkårBuilder.leggTilVilkår(vilkårType, vilkårUtfallType);
            inngangsvilkårBuilder.buildFor(behandling);
        });

        repoProvider.getBehandlingRepository().lagre(behandlingsresultat.getVilkårResultat(), lås);

        if (behandlingVedtakBuilder != null) {
            // Må lagre Behandling for at Behandlingsresultat ikke skal være transient når BehandlingVedtak blir lagret:
            repoProvider.getBehandlingRepository().lagre(behandling, lås);
            behandlingVedtak = behandlingVedtakBuilder.medBehandlingsresultat(behandlingsresultat).build();
            repoProvider.getBehandlingVedtakRepository().lagre(behandlingVedtak, lås);
        }
    }

    private void lagreBeregningsresultat(BeregningRepository beregningRepo, BehandlingLås lås) {
        if (beregning == null) {
            return;
        }

        BeregningResultat beregningResultat = BeregningResultat.builder()
            .medBeregning(beregning)
            .buildFor(behandling);
        beregningRepo.lagre(beregningResultat, lås);
    }

    public Fagsak getFagsak() {
        if (fagsak == null) {
            throw new IllegalStateException("Kan ikke hente Fagsak før denne er bygd");
        }
        return fagsak;
    }

    public AktørId getDefaultBrukerAktørId() {
        return fagsakBuilder.getBrukerBuilder().getAktørId();
    }

    public Behandling getBehandling() {
        if (behandling == null) {
            throw new IllegalStateException("Kan ikke hente Behandling før denne er bygd");
        }
        return behandling;
    }

    @SuppressWarnings("unchecked")
    public S medSaksnummer(Saksnummer saksnummer) {
        fagsakBuilder.medSaksnummer(saksnummer);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medFagsakId(Long id) {
        this.fagsakId = id;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medOppgittTilknytning(OppgittTilknytningEntitet.Builder builder) {
        this.oppgittTilknytningBuilder = builder;
        return (S) this;
    }

    public BehandlingVedtak.Builder medBehandlingVedtak() {
        if (behandlingVedtakBuilder == null) {
            behandlingVedtakBuilder = BehandlingVedtak.builder()
                // Setter defaultverdier
                .medVedtaksdato(LocalDate.now().minusDays(1))
                .medAnsvarligSaksbehandler("Nav Navesen");
        }
        return behandlingVedtakBuilder;
    }

    public void medBehandlingsresultat(Behandlingsresultat.Builder builder) {
        if (behandlingresultatBuilder == null) {
            behandlingresultatBuilder = builder;
        }
    }

    public OppgittTilknytningEntitet.Builder medOppgittTilknytning() {
        if (oppgittTilknytningBuilder == null) {
            oppgittTilknytningBuilder = new OppgittTilknytningEntitet.Builder();
        }
        return oppgittTilknytningBuilder;
    }

    public OppgittTilknytningEntitet.Builder medDefaultOppgittTilknytning() {
        if (oppgittTilknytningBuilder == null) {
            oppgittTilknytningBuilder = new OppgittTilknytningEntitet.Builder();
        }
        OppgittLandOpphold oppholdNorgeSistePeriode = new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(true)
            .medLand(Landkoder.NOR)
            .medPeriode(
                LocalDate.now(FPDateUtil.getOffset()).minusYears(1),
                LocalDate.now(FPDateUtil.getOffset()))
            .build();
        OppgittLandOpphold oppholdNorgeNestePeriode = new OppgittLandOppholdEntitet.Builder()
            .erTidligereOpphold(false)
            .medLand(Landkoder.NOR)
            .medPeriode(
                LocalDate.now(FPDateUtil.getOffset()),
                LocalDate.now(FPDateUtil.getOffset()).plusYears(1))
            .build();
        List<OppgittLandOpphold> oppholdNorge = Arrays.asList(oppholdNorgeNestePeriode, oppholdNorgeSistePeriode);

        oppgittTilknytningBuilder.medOpphold(oppholdNorge).medOppholdNå(true).medOppgittDato(LocalDate.now(FPDateUtil.getOffset()));
        return oppgittTilknytningBuilder;
    }

    public SøknadEntitet.Builder medSøknad() {
        if (søknadBuilder == null) {
            søknadBuilder = new SøknadEntitet.Builder();
        }
        return søknadBuilder;
    }

    public VurdertMedlemskapBuilder medMedlemskap() {
        if (vurdertMedlemskapBuilder == null) {
            vurdertMedlemskapBuilder = new VurdertMedlemskapBuilder();
        }
        return vurdertMedlemskapBuilder;
    }

    @SuppressWarnings("unchecked")
    public S medBehandlingType(BehandlingType behandlingType) {
        this.behandlingType = behandlingType;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S leggTilVilkår(VilkårType vilkårType, VilkårUtfallType vilkårUtfallType) {
        vilkårTyper.put(vilkårType, vilkårUtfallType);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medVilkårResultatType(VilkårResultatType vilkårResultatType) {
        this.vilkårResultatType = vilkårResultatType;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medBeregning(Beregning beregning) {
        this.beregning = beregning;
        return (S) this;
    }

    public void leggTilAksjonspunkt(AksjonspunktDefinisjon apDef, BehandlingStegType stegType) {
        aksjonspunktDefinisjoner.put(apDef, stegType);
    }

    public void leggTilMedlemskapPeriode(RegistrertMedlemskapPerioder medlemskapPeriode) {
        this.medlemskapPerioder.add(medlemskapPeriode);
    }

    @SuppressWarnings("unchecked")
    public S medBruker(AktørId aktørId, NavBrukerKjønn kjønn) {
        fagsakBuilder
            .medBrukerAktørId(aktørId)
            .medBrukerKjønn(kjønn);

        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medBrukerKjønn(NavBrukerKjønn kjønn) {
        fagsakBuilder
            .medBrukerKjønn(kjønn);

        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medBehandlingStegStart(BehandlingStegType startSteg) {
        this.startSteg = startSteg;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medTilleggsopplysninger(String tilleggsopplysninger) {
        medSøknad().medTilleggsopplysninger(tilleggsopplysninger);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medOppgittRettighet(OppgittRettighet oppgittRettighet) {
        this.oppgittRettighet = oppgittRettighet;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medFordeling(OppgittFordeling oppgittFordeling) {
        this.oppgittFordeling = oppgittFordeling;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medAvklarteUttakDatoer(AvklarteUttakDatoer avklarteUttakDatoer) {
        this.avklarteUttakDatoer = avklarteUttakDatoer;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medPerioderUtenOmsorg(PerioderUtenOmsorg perioderUtenOmsorg) {
        this.perioderUtenOmsorg = perioderUtenOmsorg;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medPeriodeMedAleneomsorg(PerioderAleneOmsorg perioderAleneOmsorg) {
        this.perioderMedAleneomsorg = perioderAleneOmsorg;
        return (S) this;
    }

    public ArgumentCaptor<Behandling> getBehandlingCaptor() {
        return behandlingCaptor;
    }

    public ArgumentCaptor<Fagsak> getFagsakCaptor() {
        return fagsakCaptor;
    }

    @SuppressWarnings("unchecked")
    public S medBehandlingstidFrist(LocalDate behandlingstidFrist) {
        this.behandlingstidFrist = behandlingstidFrist;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medBehandlendeEnhet(String behandlendeEnhet) {
        this.behandlendeEnhet = behandlendeEnhet;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medOpplysningerOppdatertTidspunkt(LocalDateTime opplysningerOppdatertTidspunkt) {
        this.opplysningerOppdatertTidspunkt = opplysningerOppdatertTidspunkt;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medRegisterOpplysninger(PersonInformasjon personinfo) {
        Objects.nonNull(personinfo);
        if (!personinfo.getType().equals(PersonopplysningVersjonType.REGISTRERT)) {
            throw new IllegalStateException("Feil versjontype, må være PersonopplysningVersjonType.REGISTRERT");
        }
        if (this.personer == null) {
            this.personer = new ArrayList<>();
            this.personer.add(personinfo);
        }
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public S medOverstyrteOpplysninger(PersonInformasjon personinfo) {
        Objects.nonNull(personinfo);
        if (!personinfo.getType().equals(PersonopplysningVersjonType.OVERSTYRT)) {
            throw new IllegalStateException("Feil versjontype, må være PersonopplysningVersjonType.OVERSTYRT");
        }
        if (this.personer == null) {
            this.personer = new ArrayList<>();
            this.personer.add(personinfo);
        }
        return (S) this;
    }

    public PersonInformasjon.Builder opprettBuilderForRegisteropplysninger() {
        if (personInformasjonBuilder == null) {
            personInformasjonBuilder = PersonInformasjon.builder(PersonopplysningVersjonType.REGISTRERT);
        }
        return personInformasjonBuilder;
    }

    @SuppressWarnings("unchecked")
    public S medOriginalBehandling(Behandling originalBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        this.originalBehandling = originalBehandling;
        this.behandlingÅrsakType = behandlingÅrsakType;
        return (S) this;
    }

    /**
     * Resetter scenario med en annen behandling, men ivaretar mocks etc.
     */
    public void resetBehandling(Behandling behandling) {
        this.behandling = behandling;
        this.fagsak = behandling.getFagsak();
    }

    @SuppressWarnings("unchecked")
    public S leggTilScenario(TestScenarioTillegg testScenarioTillegg) {
        testScenarioTilleggListe.add(testScenarioTillegg);
        return (S) this;
    }

    private void lagreTilleggsScenarier(BehandlingRepositoryProvider repositoryProvider) {
        testScenarioTilleggListe.forEach(tillegg -> tillegg.lagre(behandling, repositoryProvider));
    }

    /**
     * temporær metode til vi får fjernet gammel entitet helt.
     * Gjør en begrenset mapping av Søker data (uten adresse, relasjoner)
     *
     * @deprecated bruk {@link #medRegisterOpplysninger(PersonInformasjon)}
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public S medSøker(Personinfo søker) {
        final PersonInformasjon.Builder builder = opprettBuilderForRegisteropplysninger();
        PersonopplysningPersoninfoAdapter.mapPersonopplysningTilPerson(builder, søker);
        medRegisterOpplysninger(builder.build());
        medBruker(søker.getAktørId(), søker.getKjønn());
        return (S) this;
    }

    public InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder getInntektArbeidYtelseScenarioTestBuilder() {
        return getIayScenario().getInntektArbeidYtelseScenarioTestBuilder();
    }

    @SuppressWarnings("unchecked")
    public S medOppgittOpptjening(OppgittOpptjeningBuilder oppgittOpptjeningBuilder) {
        getIayScenario().medOppgittOpptjening(oppgittOpptjeningBuilder);
        return (S) this;
    }

    public void removeDodgyDefaultInntektArbeidYTelse() {
        if (iayScenario != null) {
            iayScenario.removeDodgyDefaultInntektArbeidYTelse();
        }
    }

    private final class MockPersonopplysningRepository implements PersonopplysningRepository {
        @Override
        public void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling eksisterendeBehandling, Behandling nyBehandling) {
            final PersonopplysningGrunnlagBuilder oppdatere = PersonopplysningGrunnlagBuilder.oppdatere(
                Optional.ofNullable((PersonopplysningGrunnlagEntitet) personopplysningMap.getOrDefault(eksisterendeBehandling, null)));

            personopplysningMap.put(nyBehandling, oppdatere.build());
        }

        @Override
        public Optional<PersonopplysningGrunnlag> hentPersonopplysningerHvisEksisterer(Behandling behandling) {
            return Optional.ofNullable(personopplysningMap.getOrDefault(behandling, null));
        }

        @Override
        public PersonopplysningGrunnlag hentPersonopplysninger(Behandling behandling) {
            if (personopplysningMap.isEmpty() || personopplysningMap.get(behandling) == null || !personopplysningMap.containsKey(behandling)) {
                throw new IllegalStateException("Fant ingen personopplysninger for angitt behandling");
            }

            return personopplysningMap.getOrDefault(behandling, null);
        }

        @Override
        public DiffResult diffResultat(PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2, FagsakYtelseType fagsakYtelseType,
                                       boolean kunSporedeEndringer) {
            return null;
        }

        @Override
        public void lagre(Behandling behandling, PersonInformasjonBuilder builder) {
            final PersonopplysningGrunnlagBuilder oppdatere = PersonopplysningGrunnlagBuilder.oppdatere(
                Optional.ofNullable((PersonopplysningGrunnlagEntitet) personopplysningMap.getOrDefault(behandling, null)));
            if (builder.getType().equals(PersonopplysningVersjonType.REGISTRERT)) {
                oppdatere.medRegistrertVersjon(builder);
            }
            if (builder.getType().equals(PersonopplysningVersjonType.OVERSTYRT)) {
                oppdatere.medOverstyrtVersjon(builder);
            }
            personopplysningMap.put(behandling, oppdatere.build());
        }

        @Override
        public PersonInformasjonBuilder opprettBuilderForOverstyring(Behandling behandling) {
            final Optional<PersonopplysningGrunnlag> grunnlag = Optional.ofNullable(personopplysningMap.getOrDefault(behandling, null));
            return PersonInformasjonBuilder.oppdater(grunnlag.flatMap(PersonopplysningGrunnlag::getOverstyrtVersjon), PersonopplysningVersjonType.OVERSTYRT);
        }

        @Override
        public PersonInformasjonBuilder opprettBuilderForRegisterdata(Behandling behandling) {
            final Optional<PersonopplysningGrunnlag> grunnlag = Optional.ofNullable(personopplysningMap.getOrDefault(behandling, null));
            return PersonInformasjonBuilder.oppdater(grunnlag.map(PersonopplysningGrunnlag::getRegisterVersjon), PersonopplysningVersjonType.REGISTRERT);
        }

        @Override
        public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling) {
            final PersonopplysningGrunnlagBuilder oppdatere = PersonopplysningGrunnlagBuilder.oppdatere(
                Optional.ofNullable((PersonopplysningGrunnlagEntitet) personopplysningMap.getOrDefault(gammelBehandling, null)));

            personopplysningMap.put(nyBehandling, oppdatere.build());
        }

        @Override
        public PersonopplysningGrunnlag hentFørsteVersjonAvPersonopplysninger(Behandling behandling) {
            throw new java.lang.UnsupportedOperationException("Ikke implementert");
        }

        @Override
        public Optional<Long> hentIdPåAktivPersonopplysninger(Behandling behandling) {
            final Optional<PersonopplysningGrunnlagEntitet> grunnlag = Optional
                .ofNullable((PersonopplysningGrunnlagEntitet) personopplysningMap.getOrDefault(behandling, null));
            return grunnlag.map(PersonopplysningGrunnlagEntitet::getId);
        }

        @Override
        public PersonopplysningGrunnlag hentPersonopplysningerPåId(Long aggregatId) {
            throw new java.lang.UnsupportedOperationException("Ikke implementert");
        }
    }
}
