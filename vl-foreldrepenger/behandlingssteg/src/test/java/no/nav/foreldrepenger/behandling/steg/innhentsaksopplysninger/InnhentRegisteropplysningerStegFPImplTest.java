package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;

import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
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
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
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

    private static final AktørId AKTØR_ID = new AktørId("3");
    private static final PersonIdent FNR = new PersonIdent("12345678901");
    private static final LocalDate FØDSELSDATO = LocalDate.now().minusYears(25);
    private static final String DURATION = "PT10H";

    private static final String UTBETALER1 = "Sykepenger";
    private static final BigDecimal UTBETALER1_MND1 = new BigDecimal(500);
    private static final BigDecimal UTBETALER1_MND2 = new BigDecimal(200);
    private static final YearMonth FOM = YearMonth.now().minusMonths(3);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

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


    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private Personinfo personinfoMor;

    @Before
    public void oppsett() {

        // Testobjekter
        Landkoder statsborgerskap = Landkoder.NOR;

        personinfoMor = new Personinfo.Builder()
            .medAktørId(AKTØR_ID)
            .medPersonIdent(FNR)
            .medNavn("Mors navn")
            .medAdresse("Mors adresse")
            .medFødselsdato(FØDSELSDATO)
            .medNavBrukerKjønn(KVINNE)
            .medLandkode(statsborgerskap)
            .medRegion(Region.NORDEN)
            .medSivilstandType(SivilstandType.UOPPGITT)
            .medPersonstatusType(PersonstatusType.BOSA)
            .build();

        when(inntektTjeneste.finnInntekt(any(FinnInntektRequest.class), any())).thenReturn(opprettTestdataForInntekt());
    }

    @Test
    public void skal_oppdatere_fagsak_og_kjøre_behandling() {
        // Arrange
        when(personinfoAdapter.innhentSaksopplysningerForSøker(eq(AKTØR_ID)))
            .thenReturn(personinfoMor);

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(AKTØR_ID, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);

        repositoryProvider.getFagsakRepository().oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.UNDER_BEHANDLING);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Fagsak fagsak = behandling.getFagsak();
        lagInnhentRegisterOpplysningerSteg().stream().forEach(a -> a.utførSteg(new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås)));

        // Assert
        verify(personinfoAdapter).innhentSaksopplysningerForSøker(AKTØR_ID);
    }

    private ArrayList<BehandlingSteg> lagInnhentRegisterOpplysningerSteg() {
        @SuppressWarnings("unchecked")
        Instance<String> durationInstance = mock(Instance.class);
        when(durationInstance.get()).thenReturn(DURATION);
        TpsAdapter tpsAdapter = mock(TpsAdapter.class);
        NavBrukerRepository navBrukerRepository = mock(NavBrukerRepository.class);

        final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
        behandlingskontrollTaskTjeneste = new BehandlingskontrollTaskTjenesteImpl(prosessTaskRepository);
        OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste = new OpplysningsPeriodeTjenesteImpl(skjæringstidspunktTjeneste, Period.of(1, 0, 0), Period.of(0, 4, 0));
        RegisterdataInnhenter registerdataInnhenter = new TestRegisterdataInnhenter(personinfoAdapter, medlemTjeneste,
            skjæringstidspunktTjeneste, behandlingskontrollTaskTjeneste, repositoryProvider, sigrunTjeneste,
            inntektArbeidYtelseTjeneste, opplysningsPeriodeTjeneste);

        FagsakTjeneste fagsakTjeneste = new FagsakTjenesteImpl(repositoryProvider, null);

        when(tpsTjeneste.hentFnrForAktør(AKTØR_ID)).thenReturn(FNR);
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

    @Test
    public void utleder_aksjonspunkt_for_verge_hvis_søker_er_under_18_år() {
        // Arrange
        AktørId aktørId = new AktørId("8888");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medBruker(aktørId, NavBrukerKjønn.KVINNE);

        scenario.medSøknad();
        Behandling behandling = scenario.lagre(repositoryProvider);
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
