package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.MANN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.FagsakTjenesteImpl;
import no.nav.foreldrepenger.behandling.impl.OpplysningsPeriodeTjenesteImpl;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingESTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InnhentingSamletTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag.MeldekortTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.impl.BasisPersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.personopplysning.impl.PersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.registerinnhenting.impl.RegisterdataInnhenterImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class InnhentRegisteropplysningerStegFPImplTest {

    private static final AktørId AKTØR_ID_MOR = new AktørId("3");
    private static final AktørId AKTØR_ID_FAR = new AktørId("4");
    private static final AktørId AKTØR_ID_BARN1 = new AktørId("5");
    private static final AktørId AKTØR_ID_BARN2 = new AktørId("6");
    private static final PersonIdent FNR_MOR = new PersonIdent("12345678901");
    private static final PersonIdent FNR_FAR = new PersonIdent("06016518156");
    private static final PersonIdent FNR_BARN1 = new PersonIdent("01345678901");
    private static final PersonIdent FNR_BARN2 = new PersonIdent("01445678901");
    private static final PersonIdent FDATNR_BARN = new PersonIdent("01071800001");
    private static final LocalDate FØDSELSDATO_MOR = LocalDate.of(1992, Month.OCTOBER, 13);
    private static final LocalDate FØDSELSDATO_FAR = LocalDate.of(1965, Month.JANUARY, 6);
    private static final LocalDate FØDSELSDATO_BARN1 = LocalDate.of(2017, Month.JANUARY, 1);
    private static final LocalDate FØDSELSDATO_BARN2 = LocalDate.of(2016, Month.JANUARY, 1);
    private static final LocalDate FØDSELSDATO_FDAT = LocalDate.now().minusDays(7);
    private static final String DURATION = "PT10H";

    private static final String UTBETALER1 = "Sykepenger";
    private static final String UTBETALER1_NAVN = "Det offentlige";
    private static final BigDecimal UTBETALER1_MND1 = new BigDecimal(500);
    private static final BigDecimal UTBETALER1_MND2 = new BigDecimal(200);
    private static final YearMonth FOM = YearMonth.now().minusMonths(3);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Personinfo personinfoBarn1;
    private Personinfo personinfoBarn2;

    @Mock
    private PersoninfoAdapter personinfoAdapter;
    @Mock
    private InntektTjeneste inntektTjeneste;
    @Mock
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    @Mock
    private InfotrygdTjeneste infotrygdTjeneste;
    @Mock
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    @Mock
    private MeldekortTjeneste meldekortTjeneste;
    @Mock
    private VirksomhetTjeneste virksomhetTjeneste;
    @Mock
    private MedlemTjeneste medlemTjeneste;
    @Mock
    private TpsTjeneste tpsTjeneste;
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private IAYRegisterInnhentingTjeneste iayTjeneste;
    @Mock
    private SigrunTjeneste sigrunTjeneste;
    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    @Mock
    private MedlemskapRepository medlemskapRepository;
    @Mock
    private BasisPersonopplysningTjeneste personopplysningTjenesteBasic;
    @Mock
    private PersonopplysningTjeneste personopplysningTjeneste;


    private InntektArbeidYtelseRepository opptjeningRepository;
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private Personinfo personinfoMor;
    private Personinfo personinfoFar;
    private Personinfo personinfoMorFdat;
    private FødtBarnInfo fdatBarnInfo;

    @Before
    public void oppsett() {

        // Testobjekter
        Landkoder statsborgerskap = Landkoder.NOR;
        Familierelasjon familierelasjonMorBarn1 = new Familierelasjon(FNR_BARN1, RelasjonsRolleType.BARN, FØDSELSDATO_BARN1,
            "Adresse", true);
        Familierelasjon familierelasjonMorBarn2 = new Familierelasjon(FNR_BARN2, RelasjonsRolleType.BARN, FØDSELSDATO_BARN2,
            "Adresse", true);
        Familierelasjon familierelasjonMorFdatBarn = new Familierelasjon(FDATNR_BARN, RelasjonsRolleType.BARN, FØDSELSDATO_FDAT,
            "Adresse", true);

        personinfoMor = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_MOR)
            .medPersonIdent(FNR_MOR)
            .medNavn("Mors navn")
            .medAdresse("Mors adresse")
            .medFødselsdato(FØDSELSDATO_MOR)
            .medNavBrukerKjønn(KVINNE)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .medFamilierelasjon(new HashSet<>(asList(familierelasjonMorBarn1, familierelasjonMorBarn2)))
            .build();

        personinfoMorFdat = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_MOR)
            .medPersonIdent(FNR_MOR)
            .medNavn("Mors navn")
            .medAdresse("Mors adresse")
            .medFødselsdato(FØDSELSDATO_MOR)
            .medNavBrukerKjønn(KVINNE)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .medFamilierelasjon(new HashSet<>(asList(familierelasjonMorBarn1, familierelasjonMorFdatBarn)))
            .build();

        fdatBarnInfo = new FødtBarnInfo.Builder().medIdent(FDATNR_BARN).medNavn("Uten").medFødselsdato(FØDSELSDATO_FDAT).medDødsdato(FØDSELSDATO_FDAT).build();


        Familierelasjon familierelasjonFarBarn1 = new Familierelasjon(FNR_BARN1, RelasjonsRolleType.BARN, FØDSELSDATO_BARN1,
            "Adresse", true);
        Familierelasjon familierelasjonFarBarn2 = new Familierelasjon(FNR_BARN2, RelasjonsRolleType.BARN, FØDSELSDATO_BARN2,
            "Adresse", true);

        personinfoFar = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_FAR)
            .medPersonIdent(FNR_FAR)
            .medNavn("Fars navn")
            .medAdresse("Fars adresse")
            .medFødselsdato(FØDSELSDATO_FAR)
            .medNavBrukerKjønn(MANN)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .medFamilierelasjon(new HashSet<>(asList(familierelasjonFarBarn1, familierelasjonFarBarn2)))
            .build();

        Familierelasjon familierelasjonBarnMora = new Familierelasjon(FNR_MOR, RelasjonsRolleType.MORA, FØDSELSDATO_MOR,
            "Adresse", true);
        Familierelasjon familierelasjonBarnFara = new Familierelasjon(FNR_FAR, RelasjonsRolleType.FARA, FØDSELSDATO_FAR,
            "Adresse", true);
        personinfoBarn1 = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_BARN1)
            .medPersonIdent(FNR_BARN1)
            .medNavn("Barn 1")
            .medAdresse("Barns adresse")
            .medFødselsdato(FØDSELSDATO_BARN1)
            .medNavBrukerKjønn(MANN)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .medFamilierelasjon(new HashSet<>(asList(familierelasjonBarnMora, familierelasjonBarnFara)))
            .build();

        personinfoBarn2 = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_BARN2)
            .medPersonIdent(FNR_BARN2)
            .medNavn("Barn 2")
            .medAdresse("Barns adresse")
            .medFødselsdato(FØDSELSDATO_BARN2)
            .medNavBrukerKjønn(MANN)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .medFamilierelasjon(new HashSet<>(asList(familierelasjonBarnMora, familierelasjonBarnFara)))
            .build();

        when(inntektTjeneste.finnInntekt(any(FinnInntektRequest.class), any())).thenReturn(opprettTestdataForInntekt());
    }

    @Test
    public void skal_oppdatere_fagsak_og_kjøre_behandling() {
        // Arrange
        when(personinfoAdapter.innhentSaksopplysningerForSøker(eq(AKTØR_ID_MOR)))
            .thenReturn(personinfoMor);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(AKTØR_ID_MOR, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(FØDSELSDATO_BARN1, null));
        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);
        Fagsak fagsak = behandling.getFagsak();
        // Act
        lagInnhentRegisterOpplysningerSteg().stream().forEach(a -> a.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling))));

        // Assert
        verify(personinfoAdapter).innhentSaksopplysningerForBarn(eq(FNR_BARN1));
    }

    @Test
    public void skal_oppdatere_fagsak_og_kjøre_behandling_ved_fdat() {
        // Arrange
        List<FødtBarnInfo> barnet = Collections.singletonList(fdatBarnInfo);
        when(personinfoAdapter.innhentSaksopplysningerForSøker(eq(AKTØR_ID_MOR)))
            .thenReturn(personinfoMorFdat);
        when(personinfoAdapter.innhentSaksopplysningerForBarn(eq(FDATNR_BARN))).thenReturn(Optional.empty());

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(AKTØR_ID_MOR, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(FØDSELSDATO_FDAT.plusDays(1), null));

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);


        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Fagsak fagsak = behandling.getFagsak();
        lagInnhentRegisterOpplysningerSteg().stream().forEach(a -> a.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås)));

        // Assert
        verify(personinfoAdapter).innhentSaksopplysningerForBarn(eq(FDATNR_BARN));
    }


    @Test
    public void skal_oppdatere_fagsak_og_kjøre_behandling_selv_om_fødselsdato_fra_søknad_ikke_matcher() {
        // Arrange
        when(personinfoAdapter.innhentSaksopplysningerForSøker(eq(AKTØR_ID_MOR)))
            .thenReturn(personinfoMor);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(AKTØR_ID_MOR, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(FØDSELSDATO_BARN1.plusDays(1), null));

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Fagsak fagsak = behandling.getFagsak();
        lagInnhentRegisterOpplysningerSteg().stream().forEach(a -> a.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås)));

        // Assert
        verify(personinfoAdapter).innhentSaksopplysningerForBarn(eq(FNR_BARN1));
    }

    private ArrayList<BehandlingSteg> lagInnhentRegisterOpplysningerSteg() {
        @SuppressWarnings("unchecked")
        Instance<String> durationInstance = mock(Instance.class);
        when(durationInstance.get()).thenReturn(DURATION);
        TpsAdapter tpsAdapter = mock(TpsAdapter.class);
        NavBrukerRepository navBrukerRepository = mock(NavBrukerRepository.class);

        final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0));
        behandlingskontrollTaskTjeneste = new BehandlingskontrollTaskTjenesteImpl(prosessTaskRepository);
        OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste = new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste, Period.of(1, 0, 0), Period.of(0, 4, 0));
        RegisterdataInnhenter registerdataInnhenter = new TestRegisterdataInnhenter(personinfoAdapter, medlemTjeneste,
            skjæringstidspunktTjeneste, behandlingskontrollTaskTjeneste, repositoryProvider, sigrunTjeneste,
            inntektArbeidYtelseTjeneste, opplysningsPeriodeTjeneste);

        FagsakTjeneste fagsakTjeneste = new FagsakTjenesteImpl(repositoryProvider, null);

        when(tpsTjeneste.hentFnrForAktør(AKTØR_ID_FAR)).thenReturn(FNR_FAR);
        when(tpsTjeneste.hentFnrForAktør(AKTØR_ID_MOR)).thenReturn(FNR_MOR);
        when(tpsTjeneste.hentFnrForAktør(AKTØR_ID_BARN1)).thenReturn(FNR_BARN1);
        when(tpsTjeneste.hentFnrForAktør(AKTØR_ID_BARN2)).thenReturn(FNR_BARN2);
        opptjeningRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        personopplysningTjenesteBasic = new BasisPersonopplysningTjenesteImpl(repositoryProvider, skjæringstidspunktTjeneste);
        personopplysningTjeneste = new PersonopplysningTjenesteImpl(repositoryProvider, tpsAdapter, navBrukerRepository, skjæringstidspunktTjeneste);

        InnhentingSamletTjenesteImpl innhentingSamletTjeneste = new InnhentingSamletTjenesteImpl(arbeidsforholdTjeneste, tpsTjeneste, inntektTjeneste, infotrygdTjeneste, infotrygdBeregningsgrunnlagTjeneste,
            meldekortTjeneste);

        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste2 = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, arbeidsforholdTjeneste, null, null, skjæringstidspunktTjeneste, apOpptjening);
        iayTjeneste = new IAYRegisterInnhentingESTjenesteImpl(
            inntektArbeidYtelseTjeneste2,
            repositoryProvider, virksomhetTjeneste, skjæringstidspunktTjeneste,
            innhentingSamletTjeneste,
            personopplysningTjenesteBasic, opplysningsPeriodeTjeneste);

        ArrayList<BehandlingSteg> behandlingStegs = new ArrayList<>();
        behandlingStegs.add(new InnhentRegisteropplysningerStegFPImpl(repositoryProvider, registerdataInnhenter));
        behandlingStegs.add(new InnhentRegisteropplysningerResterendeOppgaverStegImpl(repositoryProvider, fagsakTjeneste, personopplysningTjeneste));
        return behandlingStegs;
    }

    @Ignore // FIXME (diamant): Refaktorer som følge av at logikk ligger i prosesstask. Ikke relevant.
    @Test
    public void skal_innhente_inntektsopplysninger_for_søker_og_annen_forelder_og_oppdatere_behandlingsgrunnlaget() {
        // Arrange
        when(personinfoAdapter.innhentSaksopplysningerForEktefelle(Optional.of(AKTØR_ID_FAR)))
            .thenReturn(Optional.ofNullable(personinfoFar));
        when(personinfoAdapter.innhentSaksopplysningerForSøker(AKTØR_ID_MOR))
            .thenReturn(personinfoMor);

        when(personinfoAdapter.innhentSaksopplysninger(FNR_BARN1))
            .thenReturn(Optional.of(personinfoBarn1));

        when(personinfoAdapter.innhentSaksopplysninger(FNR_BARN2))
            .thenReturn(Optional.of(personinfoBarn2));

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(AKTØR_ID_MOR, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);

        when(arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(any(), any())).thenReturn(opprettListeAvArbeidsforhold());
        when(virksomhetTjeneste.hentOgLagreOrganisasjon(UTBETALER1)).thenReturn(opprettVirksomhet1());
        // when(virksomhetTjeneste.hentOgLagreOrganisasjon(behandling, UTBETALER2)).thenReturn(opprettVirksomhet2());
        Fagsak fagsak = behandling.getFagsak();
        // Act
        lagInnhentRegisterOpplysningerSteg().stream().forEach(a -> a.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling))));

        Optional<InntektArbeidYtelseGrunnlag> opptjeningAggregat = opptjeningRepository.hentAggregatHvisEksisterer(behandling, null);

        // Assert
        verify(inntektTjeneste, times(2)).finnInntekt(any(), any());
        assertThat(opptjeningAggregat).isPresent();
        assertThat(opptjeningAggregat.get().getAktørInntektForFørStp()).isNotEmpty();
    }

    private Virksomhet opprettVirksomhet1() {
        return new VirksomhetEntitet.Builder()
            .medOrgnr(UTBETALER1)
            .medNavn(UTBETALER1_NAVN)
            .medRegistrert(LocalDate.now().minusYears(3L))
            .medOppstart(LocalDate.now().minusYears(2L))
            .oppdatertOpplysningerNå()
            .build();
    }

    private Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> opprettListeAvArbeidsforhold() {
        List<Arbeidsforhold> arbeidsforhold = new ArrayList<>();

        arbeidsforhold.add(new Arbeidsforhold.Builder()
            .medArbeidsgiver(new Organisasjon(UTBETALER1))
            .medArbeidsforholdId(UTBETALER1)
            .build());
        /*
         * arbeidsforhold.add(new Arbeidsforhold.Builder()
         * .medArbeidsgiver(new Organisasjon(UTBETALER2))
         * .build());
         */
        return arbeidsforhold.stream().collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
    }

    @Test
    public void utleder_aksjonspunkt_for_verge_hvis_søker_er_under_18_år() {
        // Arrange
        AktørId aktørId = new AktørId("8888");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(aktørId, NavBrukerKjønn.KVINNE);

        LocalDate termindato = LocalDate.now().plusMonths(3);
        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, new AvklarteUttakDatoerEntitet(FØDSELSDATO_BARN1, null));
        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);

        PersonIdent søkerFnr = new PersonIdent("01020312345");
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(aktørId)
            .medPersonIdent(søkerFnr)
            .medNavn("Mors navn")
            .medAdresse("Mors adresse")
            .medFødselsdato(LocalDate.now().minusYears(18).plusWeeks(1))
            .medNavBrukerKjønn(KVINNE)
            .medLandkode(Landkoder.NOR)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .build();

        when(personinfoAdapter.innhentSaksopplysningerForSøker(aktørId)).thenReturn(personinfo);

        when(tpsTjeneste.hentFnrForAktør(eq(aktørId))).thenReturn(søkerFnr);

        lagInnhentRegisterOpplysningerSteg();
        BehandleStegResultat behandleStegResultat = null;
        Fagsak fagsak = behandling.getFagsak();
        for (BehandlingSteg behandlingSteg : lagInnhentRegisterOpplysningerSteg()) {
            behandleStegResultat = behandlingSteg.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingRepository.taSkriveLås(behandling)));
        }

        // Assert
        assertThat(behandleStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        List<AksjonspunktDefinisjon> aksjonspunktListe = behandleStegResultat.getAksjonspunktListe();
        assertThat(aksjonspunktListe.size()).isEqualTo(1);
        assertThat(aksjonspunktListe.get(0)).isEqualTo(AksjonspunktDefinisjon.AVKLAR_VERGE);
    }

    private InntektsInformasjon opprettTestdataForInntekt() {
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();

        månedsinntekter.add(new Månedsinntekt.Builder()
            .medArbeidsgiver(UTBETALER1)
            .medBeløp(UTBETALER1_MND1)
            .medMåned(FOM)
            .medYtelseKode("foreldrepenger")
            .medYtelse(true)
            .build());

        månedsinntekter.add(new Månedsinntekt.Builder()
            .medArbeidsgiver(UTBETALER1)
            .medBeløp(UTBETALER1_MND2)
            .medMåned(FOM.plusMonths(1))
            .medYtelse(true)
            .medYtelseKode("foreldrepenger")
            .build());
        return new InntektsInformasjon(månedsinntekter, new ArrayList<>(), InntektsKilde.INNTEKT_OPPTJENING);
    }

    private class TestRegisterdataInnhenter extends RegisterdataInnhenterImpl {

        TestRegisterdataInnhenter(PersoninfoAdapter personinfoAdapter, MedlemTjeneste medlemTjeneste, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                  BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste, BehandlingRepositoryProvider repositoryProvider,
                                  SigrunTjeneste sigrunTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                  OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste) {
            super(personinfoAdapter, medlemTjeneste, skjæringstidspunktTjeneste, behandlingskontrollTaskTjeneste, repositoryProvider, sigrunTjeneste,
                inntektArbeidYtelseTjeneste, opplysningsPeriodeTjeneste);
        }

        @Override
        protected IAYRegisterInnhentingTjeneste getIAYRegisterInnhenterFor(Behandling behandling) {
            return iayTjeneste;
        }
    }

}
