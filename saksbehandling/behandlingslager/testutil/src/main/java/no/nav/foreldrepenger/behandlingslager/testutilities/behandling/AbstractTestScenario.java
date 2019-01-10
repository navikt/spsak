package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapBehandlingsgrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRegistrertEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
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
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavPersoninfoBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.repositorystub.BeregningsgrunnlagRepositoryStub;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.repositorystub.BeregningsresultatFPRepositoryStub;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkFraJson;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.util.FPDateUtil;
import no.nav.vedtak.util.Tuple;

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
public abstract class AbstractTestScenario<S extends AbstractTestScenario<S>> implements TestScenario<S> {

    private static final AtomicLong FAKE_ID = new AtomicLong(100999L);
    private final FagsakBuilder fagsakBuilder;
    private final Map<Behandling, PersonopplysningGrunnlag> personopplysningMap = new HashMap<>();
    private final Map<Long, SykefraværGrunnlagEntitet> sykefraværGrunnlagMap = new HashMap<>();
    private final Map<Behandling, Verge> vergeMap = new HashMap<>();
    private final Map<Long, MedlemskapBehandlingsgrunnlagEntitet> medlemskapgrunnlag = new HashMap<>();
    private final Map<Long, BehandlingVedtak> behandlingvedtakMap = new HashMap<>();
    private final Map<Long, Behandlingsresultat> behandlingsresultatMap = new HashMap<>();
    private final Map<Long, Behandling> behandlingMap = new HashMap<>();
    private List<TestScenarioResultatTillegg> testScenarioResultatTilleggListe = new ArrayList<>();
    private ArgumentCaptor<Behandling> behandlingCaptor = ArgumentCaptor.forClass(Behandling.class);
    private ArgumentCaptor<Behandlingsresultat> behandlingsresultatCaptor = ArgumentCaptor.forClass(Behandlingsresultat.class);
    private ArgumentCaptor<BehandlingVedtak> behandlingVedtakCaptor = ArgumentCaptor.forClass(BehandlingVedtak.class);
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
    private List<RegistrertMedlemskapPerioder> medlemskapPerioder = new ArrayList<>();
    private Long fagsakId = nyId();
    private LocalDate behandlingstidFrist;
    private LocalDateTime opplysningerOppdatertTidspunkt;
    private String behandlendeEnhet;
    private BehandlingRepository mockBehandlingRepository;
    private BehandlingVedtak behandlingVedtak;
    private BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;

    // Registret og overstyrt personinfo
    private List<PersonInformasjon> personer;

    // Sykefravær start
    private SykefraværEntitet sykefravær;
    private SykemeldingerEntitet sykemeldinger;
    // Sykefravær slutt

    private Behandling originalBehandling;
    private BehandlingÅrsakType behandlingÅrsakType;
    private GrunnlagRepositoryProvider repositoryProvider;
    private ResultatRepositoryProvider resultatRepositoryProvider;
    private no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder personInformasjonBuilder;
    private Behandlingsresultat behandlingsresultat;

    protected AbstractTestScenario(NavBrukerKjønn kjønn, AktørId aktørId) {
        this.fagsakBuilder = FagsakBuilder
            .nyFagsak()
            .medSaksnummer(new Saksnummer(nyId() + ""))
            .medBruker(NavBruker.opprettNy(new NavPersoninfoBuilder().medAktørId(aktørId).medKjønn(kjønn).build()));
    }

    protected AbstractTestScenario(NavBruker navBruker) {
        this.fagsakBuilder = FagsakBuilder
            .nyFagsak()
            .medSaksnummer(new Saksnummer(nyId() + ""))
            .medBruker(navBruker);
    }

    static long nyId() {
        return FAKE_ID.getAndIncrement();
    }

    private BehandlingRepository lagBasicMockBehandlingRepository(GrunnlagRepositoryProvider repositoryProvider) {
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
        PersonopplysningRepository mockPersonopplysningRepository = lagMockPersonopplysningRepository();
        MedlemskapRepository mockMedlemskapRepository = lagMockMedlemskapRepository();
        BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository = mockBehandlingsgrunnlagKodeverkRepository();
        KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        VergeRepository mockVergeRepository = lagMockVergeRepository();
        SøknadRepository søknadRepository = mockSøknadRepository();
        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = getIayScenario().mockInntektArbeidYtelseRepository();
        VirksomhetRepository virksomhetRepository = InntektArbeidYtelseScenario.mockVirksomhetRepository();
        FagsakLåsRepository fagsakLåsRepository = mockFagsakLåsRepository();

        BehandlingLåsRepository behandlingLåsReposiory = mockBehandlingLåsRepository();


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
        when(repositoryProvider.getInntektArbeidYtelseRepository()).thenReturn(inntektArbeidYtelseRepository);
        when(repositoryProvider.getVirksomhetRepository()).thenReturn(virksomhetRepository);
        when(repositoryProvider.getFagsakLåsRepository()).thenReturn(fagsakLåsRepository);
        when(repositoryProvider.getBehandlingLåsRepository()).thenReturn(behandlingLåsReposiory);
        when(repositoryProvider.getSykefraværRepository()).thenReturn(mockSykefraværRepository());

        return behandlingRepository;
    }

    private SykefraværRepository mockSykefraværRepository() {
        return new SykefraværRepository() {
            @Override
            public SykemeldingerBuilder oppretBuilderForSykemeldinger(Long behandlingId) {
                Optional<SykefraværGrunnlagEntitet> grunnlag = Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(behandlingId, null));
                return SykemeldingerBuilder.oppdater(grunnlag.map(SykefraværGrunnlagEntitet::getSykemeldinger));
            }

            @Override
            public SykefraværBuilder oppretBuilderForSykefravær(Long behandlingId) {
                Optional<SykefraværGrunnlagEntitet> grunnlag = Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(behandlingId, null));
                return SykefraværBuilder.oppdater(grunnlag.map(SykefraværGrunnlagEntitet::getSykefravær));
            }

            @Override
            public void lagre(Behandling behandling, SykemeldingerBuilder builder) {
                Long id = behandling.getId();
                SykefraværGrunnlagBuilder oppdater = SykefraværGrunnlagBuilder.oppdater(Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(id, null)));
                oppdater.medSykemeldinger(builder);
                sykefraværGrunnlagMap.put(id, (SykefraværGrunnlagEntitet) oppdater.build());
            }

            @Override
            public void lagre(Behandling behandling, SykefraværBuilder builder) {
                Long id = behandling.getId();
                SykefraværGrunnlagBuilder oppdater = SykefraværGrunnlagBuilder.oppdater(Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(id, null)));
                oppdater.medSykefravær(builder);
                sykefraværGrunnlagMap.put(id, (SykefraværGrunnlagEntitet) oppdater.build());
            }

            @Override
            public Optional<SykefraværGrunnlag> hentHvisEksistererFor(Long behandlingId) {
                return Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(behandlingId, null));
            }

            @Override
            public SykefraværGrunnlag hentFor(Long behandlingId) {
                return Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(behandlingId, null)).orElseThrow(IllegalStateException::new);
            }

            @Override
            public void kopierGrunnlagFraEksisterendeBehandling(Behandling behandling, Behandling revudering) {
                SykefraværGrunnlagBuilder oppdater = SykefraværGrunnlagBuilder
                    .oppdater(Optional.ofNullable(sykefraværGrunnlagMap.getOrDefault(behandling.getId(), null)));
                sykefraværGrunnlagMap.put(revudering.getId(), (SykefraværGrunnlagEntitet) oppdater.build());
            }
        };
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

    private BeregningsresultatRepository mockBeregningsresultatFPRepository() {
        return new BeregningsresultatFPRepositoryStub();
    }

    private BehandlingVedtakRepository mockBehandlingVedtakRepository() {
        BehandlingVedtakRepository behandlingVedtakRepository = mock(BehandlingVedtakRepository.class);
        when(behandlingVedtakRepository.hentVedtakFor(Mockito.any())).thenReturn(Optional.ofNullable(behandlingVedtak));
        when(behandlingVedtakRepository.lagre(behandlingVedtakCaptor.capture(), Mockito.any())).thenAnswer((Answer<Long>) invocation -> {
            BehandlingVedtak beh = invocation.getArgument(0);
            Long id = beh.getId();
            if (id == null) {
                id = nyId();
                Whitebox.setInternalState(beh, "id", id);
            }
            if (beh.getBehandlingsresultat() == null) {
                Whitebox.setInternalState(beh, "behandlingsresultat", behandlingsresultat);
                Whitebox.setInternalState(behandlingsresultat, "behandlingVedtak", beh);
            } else {
                Whitebox.setInternalState(beh.getBehandlingsresultat(), "behandlingVedtak", beh);
            }
            behandlingVedtak = beh;

            return id;
        });

        return behandlingVedtakRepository;
    }

    public BehandlingVedtak mockBehandlingVedtak() {
        if (behandlingVedtak == null) {
            behandlingVedtak = Mockito.mock(BehandlingVedtak.class);
        }
        return behandlingVedtak;
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

    /**
     * Hjelpe metode for å håndtere mock repository.
     */
    @Override
    public BehandlingRepository mockBehandlingRepository() {
        if (resultatRepositoryProvider == null && mockBehandlingRepository != null) {
            resultatRepositoryProvider = mock(ResultatRepositoryProvider.class);
            lagBasicMockResultatProvider(resultatRepositoryProvider);
        }
        if (mockBehandlingRepository != null) {
            return mockBehandlingRepository;
        }
        repositoryProvider = mock(GrunnlagRepositoryProvider.class);
        BehandlingRepository behandlingRepository = lagMockBehandlingRepository();
        resultatRepositoryProvider = mock(ResultatRepositoryProvider.class);
        lagBasicMockResultatProvider(resultatRepositoryProvider);
        return behandlingRepository;
    }

    private BehandlingRepository lagMockBehandlingRepository() {
        BehandlingRepository behandlingRepository = lagBasicMockBehandlingRepository(repositoryProvider);

        when(behandlingRepository.hentResultatHvisEksisterer(Mockito.any())).thenAnswer(a -> {
            Long argument = a.getArgument(0);
            if (behandlingsresultatMap.containsKey(argument)) {
                return Optional.ofNullable(behandlingsresultatMap.get(argument));
            }
            return Optional.empty();
        });
        when(behandlingRepository.hentResultat(Mockito.any(Long.class))).thenAnswer(a -> {
            Long argument = a.getArgument(0);
            return behandlingsresultatMap.get(argument);
        });
        when(behandlingRepository.hentBehandling(Mockito.any())).thenAnswer(a -> {
            Long id = a.getArgument(0);
            return behandlingMap.getOrDefault(id, null);
        });
        when(behandlingRepository.finnUnikBehandlingForBehandlingId(Mockito.any())).thenAnswer(a -> {
            Long id = a.getArgument(0);
            return Optional.ofNullable(behandlingMap.getOrDefault(id, null));
        });
        when(behandlingRepository.hentSisteBehandlingForFagsakId(Mockito.any(), Mockito.any(BehandlingType.class)))
            .thenAnswer(a -> {
                Long id = a.getArgument(0);
                BehandlingType type = a.getArgument(1);
                return behandlingMap.values().stream().filter(b -> type.equals(b.getType()) && b.getFagsakId().equals(id)).sorted().findFirst();
            });
        when(behandlingRepository.hentSisteBehandlingForFagsakId(Mockito.any()))
            .thenAnswer(a -> {
                Long id = a.getArgument(0);
                return behandlingMap.values().stream().filter(b -> b.getFagsakId().equals(id)).sorted().findFirst();
            });
        when(behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(Mockito.any()))
            .thenAnswer(a -> {
                Long id = a.getArgument(0);
                return behandlingMap.values().stream().filter(b -> b.getFagsakId().equals(id) && behandlingsresultatMap.containsKey(b.getId()) && !behandlingsresultatMap.get(b.getId()).isBehandlingHenlagt()).sorted().findFirst();
            });

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
                behandlingMap.put(id, beh);
                return id;
            });

        when(behandlingRepository.lagre(behandlingsresultatCaptor.capture(), Mockito.any()))
            .thenAnswer((Answer<Long>) invocation -> {
                Behandlingsresultat beh = invocation.getArgument(0);
                Long id = beh.getId();
                if (id == null) {
                    id = nyId();
                    Whitebox.setInternalState(beh, "id", id);
                } else {
                    behandlingsresultatMap.remove(beh.getBehandling().getId());
                }
                behandlingsresultatMap.put(beh.getBehandling().getId(), beh);
                behandlingsresultat = beh;

                return id;
            });

        mockBehandlingRepository = behandlingRepository;
        return behandlingRepository;
    }

    private void lagBasicMockResultatProvider(ResultatRepositoryProvider resultatRepositoryProvider) {
        OpptjeningRepository opptjeningRepository = Mockito.mock(OpptjeningRepository.class);
        BeregningsresultatRepository beregningsresultatFPRepository = mockBeregningsresultatFPRepository();
        BehandlingVedtakRepository behandlingVedtakRepository = mockBehandlingVedtakRepository();
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = mockBeregningsgrunnlagRepository();
        when(resultatRepositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
        when(resultatRepositoryProvider.getBeregningsresultatRepository()).thenReturn(beregningsresultatFPRepository);
        when(resultatRepositoryProvider.getVedtakRepository()).thenReturn(behandlingVedtakRepository);
        when(resultatRepositoryProvider.getBeregningsgrunnlagRepository()).thenReturn(beregningsgrunnlagRepository);
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        when(resultatRepositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepository);
    }

    @Override
    public Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> mockBehandlingRepositoryProvider() {
        mockBehandlingRepository();
        return new Tuple<>(repositoryProvider, resultatRepositoryProvider);
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
        return mockBehandlingRepositoryProvider().getElement1().getPersonopplysningRepository();
    }

    public InntektArbeidYtelseRepository getMockInntektArbeidYtelseRepository() {
        return mockBehandlingRepositoryProvider().getElement1().getInntektArbeidYtelseRepository();
    }

    @Override
    public MedlemskapRepository mockMedlemskapRepository() {
        return mockBehandlingRepositoryProvider().getElement1().getMedlemskapRepository();
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

    @Override
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

    @Override
    public Fagsak lagreFagsak(GrunnlagRepositoryProvider repositoryProvider) {
        lagFagsak(repositoryProvider.getFagsakRepository());
        return fagsak;
    }

    @Override
    public Behandling lagre(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider) {
        build(repositoryProvider, resultatRepositoryProvider);
        return behandling;
    }

    BehandlingRepository lagMockedRepositoryForOpprettingAvBehandlingInternt() {
        if (mockBehandlingRepository != null && behandling != null) {
            return mockBehandlingRepository;
        }
        validerTilstandVedMocking();

        mockBehandlingRepository = mockBehandlingRepository();

        lagre(repositoryProvider, resultatRepositoryProvider); // NOSONAR //$NON-NLS-1$
        Whitebox.setInternalState(behandling.getType(), "ekstraData", "{ \"behandlingstidFristUker\" : 6, \"behandlingstidVarselbrev\" : \"N\" }");
        return mockBehandlingRepository;
    }

    @Override
    public Behandling lagMocked() {
        lagMockedRepositoryForOpprettingAvBehandlingInternt();
        return behandling;
    }

    private void lagrePersonopplysning(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling) {
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
                        .brukerKjønn(NavBrukerKjønn.MANN) // FIXME SP : ta som input eller dropp helt
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

        repository.lagre(behandling, personInformasjonBuilder);
    }

    private void lagreVerge(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling) {
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
    public S medDefaultVerge() {
        vergeBuilder = new VergeBuilder();
        vergeBuilder.medBruker(new NavBrukerBuilder().build());
        vergeBuilder.medBrevMottaker(BrevMottaker.VERGE);
        vergeBuilder.medVergeType(VergeType.BARN);
        return (S) this;
    }

    private void build(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider) {
        if (behandling != null) {
            throw new IllegalStateException("build allerede kalt.  Hent Behandling via getBehandling eller opprett nytt scenario.");
        }
        Builder behandlingBuilder = grunnBuild(repositoryProvider);

        this.behandling = behandlingBuilder.build();

        if (startSteg != null) {
            new InternalManipulerBehandlingImpl(repositoryProvider).forceOppdaterBehandlingSteg(behandling, startSteg);
        }

        leggTilAksjonspunkter(behandling, repositoryProvider);

        BehandlingRepository behandlingRepo = repositoryProvider.getBehandlingRepository();
        BehandlingLås lås = behandlingRepo.taSkriveLås(behandling);
        behandlingRepo.lagre(behandling, lås);

        lagreSykefravær(repositoryProvider, behandling);
        lagrePersonopplysning(repositoryProvider, behandling);
        lagreMedlemskapOpplysninger(repositoryProvider, behandling);
        lagreVerge(repositoryProvider, behandling);
        if (iayScenario != null) {
            iayScenario.lagreVirksomhet(repositoryProvider);
            iayScenario.lagreOppgittOpptjening(repositoryProvider, behandling);
            iayScenario.lagreOpptjening(repositoryProvider, behandling);
        }
        lagreSøknad(repositoryProvider);
        // opprett og lagre resulater på behandling
        lagreBehandlingsresultatOgVilkårResultat(resultatRepositoryProvider, lås);
        lagreTilleggsScenarier(resultatRepositoryProvider);
        behandlingRepo.lagre(behandling, lås);

        if (this.opplysningerOppdatertTidspunkt != null) {
            behandlingRepo.oppdaterSistOppdatertTidspunkt(this.behandling, this.opplysningerOppdatertTidspunkt);
        }
    }

    private void lagreSykefravær(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling) {
        if (sykefravær != null || sykemeldinger != null) {
            SykefraværRepository repository = repositoryProvider.getSykefraværRepository();
            if (sykefravær != null) {
                repository.lagre(behandling, SykefraværBuilder.oppdater(Optional.of(sykefravær)));
            }
            if (sykemeldinger != null) {
                repository.lagre(behandling, SykemeldingerBuilder.oppdater(Optional.of(sykemeldinger)));
            }
        }
    }

    @Override
    public SykefraværBuilder getSykefraværBuilder() {
        return SykefraværBuilder.oppdater(Optional.ofNullable(sykefravær));
    }

    @Override
    public SykemeldingerBuilder getSykemeldingerBuilder() {
        return SykemeldingerBuilder.oppdater(Optional.ofNullable(sykemeldinger));
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medSykefravær(SykefraværBuilder sykefraværBuilder) {
        sykefravær = (SykefraværEntitet) sykefraværBuilder.build();
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medSykemeldinger(SykemeldingerBuilder sykemeldingerBuilder) {
        sykemeldinger = (SykemeldingerEntitet) sykemeldingerBuilder.build();
        return (S) this;
    }

    private void leggTilAksjonspunkter(Behandling behandling, GrunnlagRepositoryProvider repositoryProvider) {
        aksjonspunktDefinisjoner.forEach(
            (apDef, stegType) -> {
                if (stegType != null) {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef, stegType);
                } else {
                    repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, apDef);
                }
            });
    }

    private void lagreSøknad(GrunnlagRepositoryProvider repositoryProvider) {
        if (søknadBuilder != null) {
            final SøknadRepository søknadRepository = repositoryProvider.getSøknadRepository();
            søknadRepository.lagreOgFlush(behandling, søknadBuilder.build());
        }
    }

    private void lagreMedlemskapOpplysninger(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling) {
        repositoryProvider.getMedlemskapRepository().lagreMedlemskapRegisterOpplysninger(behandling, medlemskapPerioder);

        VurdertMedlemskap vurdertMedlemskap = medMedlemskap().build();
        repositoryProvider.getMedlemskapRepository().lagreMedlemskapVurdering(behandling, vurdertMedlemskap);
    }

    private Builder grunnBuild(GrunnlagRepositoryProvider repositoryProvider) {
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
                    .medNavBrukerKjønn(NavBrukerKjønn.MANN) // FIXME SP : Ta som input eller dropp helt
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

    private void lagreBehandlingsresultatOgVilkårResultat(ResultatRepositoryProvider repoProvider, BehandlingLås lås) {
        // opprett og lagre behandlingsresultat med VilkårResultat og BehandlingVedtak
        Behandlingsresultat behandlingsresultat = (behandlingresultatBuilder == null ? Behandlingsresultat.builderForInngangsvilkår()
            : behandlingresultatBuilder).buildFor(behandling);

        VilkårResultat.Builder inngangsvilkårBuilder = VilkårResultat
            .builderFraEksisterende(behandlingsresultat.getVilkårResultat())
            .medVilkårResultatType(vilkårResultatType);

        vilkårTyper.forEach(inngangsvilkårBuilder::leggTilVilkår);
        inngangsvilkårBuilder.buildFor(behandlingsresultat);

        repoProvider.getBehandlingRepository().lagre(behandlingsresultat.getVilkårResultat(), lås);
        repoProvider.getBehandlingRepository().lagre(behandlingsresultat, lås);

        if (behandlingVedtakBuilder != null) {
            // Må lagre Behandling for at Behandlingsresultat ikke skal være transient når BehandlingVedtak blir lagret:
            repoProvider.getBehandlingRepository().lagre(behandling, lås);
            behandlingVedtak = behandlingVedtakBuilder.medBehandlingsresultat(behandlingsresultat).build();
            repoProvider.getVedtakRepository().lagre(behandlingVedtak, lås);
        }
    }

    @Override
    public Fagsak getFagsak() {
        if (fagsak == null) {
            throw new IllegalStateException("Kan ikke hente Fagsak før denne er bygd");
        }
        return fagsak;
    }

    public AktørId getDefaultBrukerAktørId() {
        return fagsakBuilder.getBrukerBuilder().getAktørId();
    }

    @Override
    public Behandling getBehandling() {
        if (behandling == null) {
            throw new IllegalStateException("Kan ikke hente Behandling før denne er bygd");
        }
        return behandling;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medSaksnummer(Saksnummer saksnummer) {
        fagsakBuilder.medSaksnummer(saksnummer);
        return (S) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medFagsakId(Long id) {
        this.fagsakId = id;
        return (S) this;
    }

    @Override
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

    @Override
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

    @Override
    public VurdertMedlemskapBuilder medMedlemskap() {
        if (vurdertMedlemskapBuilder == null) {
            vurdertMedlemskapBuilder = new VurdertMedlemskapBuilder();
        }
        return vurdertMedlemskapBuilder;
    }

    @Override
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

    public void leggTilAksjonspunkt(AksjonspunktDefinisjon apDef, BehandlingStegType stegType) {
        aksjonspunktDefinisjoner.put(apDef, stegType);
    }

    @Override
    public void leggTilMedlemskapPeriode(RegistrertMedlemskapPerioder medlemskapPeriode) {
        this.medlemskapPerioder.add(medlemskapPeriode);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medBruker(AktørId aktørId, NavBrukerKjønn kjønn) {
        fagsakBuilder
            .medBruker(NavBruker.opprettNy(new NavPersoninfoBuilder().medAktørId(aktørId).medKjønn(kjønn).build()));

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

    @Override
    public ArgumentCaptor<Behandling> getBehandlingCaptor() {
        return behandlingCaptor;
    }

    @Override
    public ArgumentCaptor<Fagsak> getFagsakCaptor() {
        return fagsakCaptor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S medBehandlingstidFrist(LocalDate behandlingstidFrist) {
        this.behandlingstidFrist = behandlingstidFrist;
        return (S) this;
    }

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public S medOriginalBehandling(Behandling originalBehandling, BehandlingÅrsakType behandlingÅrsakType) {
        this.originalBehandling = originalBehandling;
        this.behandlingÅrsakType = behandlingÅrsakType;
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public S leggTilScenario(TestScenarioResultatTillegg testScenarioResultatTillegg) {
        testScenarioResultatTilleggListe.add(testScenarioResultatTillegg);
        return (S) this;
    }

    private void lagreTilleggsScenarier(ResultatRepositoryProvider repositoryProvider) {
        testScenarioResultatTilleggListe.forEach(tillegg -> tillegg.lagre(behandling, repositoryProvider));
    }

    public InntektArbeidYtelseScenario.InntektArbeidYtelseScenarioTestBuilder getInntektArbeidYtelseScenarioTestBuilder() {
        return getIayScenario().getInntektArbeidYtelseScenarioTestBuilder();
    }

    @SuppressWarnings("unchecked")
    public S medOppgittOpptjening(OppgittOpptjeningBuilder oppgittOpptjeningBuilder) {
        getIayScenario().medOppgittOpptjening(oppgittOpptjeningBuilder);
        return (S) this;
    }

    @Override
    public void avsluttBehandling() {
        avsluttBehandling(mockBehandlingRepositoryProvider().getElement1(), getBehandling());
    }

    @Override
    public void avsluttBehandling(GrunnlagRepositoryProvider repositoryProvider, Behandling behandling) {
        Long behandlingId = behandling.getId();
        repositoryProvider.getBehandlingskontrollRepository().avsluttBehandling(behandlingId);
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
