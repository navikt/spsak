package no.nav.foreldrepenger.beregningsgrunnlag;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.grunnbeløp.GrunnbeløpForTest;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BeregningsgrunnlagFraTilstøtendeYtelseTjenesteImplTest {

    private static final BigDecimal MÅNEDSBELØP_TILSTØTENDE_YTELSE = BigDecimal.valueOf(10000L);
    private static final BigDecimal ÅRSBELØP_TILSTØTENDE_YTELSE = BigDecimal.valueOf(12).multiply(MÅNEDSBELØP_TILSTØTENDE_YTELSE);
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, Month.MARCH, 1);
    private static final LocalDate TWO_YEARS_AGO = SKJÆRINGSTIDSPUNKT.minusYears(2);
    private static final AktørId AKTØR_ID = new AktørId(80000L);
    private static final String ORGNR_MED_NYTT_ARBEIDSFORHOLD = "21542512";
    private static final String ORGNR_UTEN_NYTT_ARBEIDSFORHOLD = "21542513";
    private static final String ORGNR_UTEN_NYTT_ARBEIDSFORHOLD2 = "21542514";
    private static final BigDecimal ÅRSSBELØP_TILSTØTENDE_YTELSE_2 = BigDecimal.valueOf(234000);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private BehandlingRepositoryProvider realRepositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel = new MapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel(realRepositoryProvider.getBeregningRepository());

    @Inject
    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL;

    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;

    private BeregningsgrunnlagFraTilstøtendeYtelseTjeneste beregningsgrunnlagFraTilstøtendeYtelseTjeneste;

    private Behandling behandling;
    private Beregningsgrunnlag beregningsgrunnlag;

    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;

    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    @Mock
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private VirksomhetEntitet virksomhetMedNyttArbeidsforhold;
    private VirksomhetEntitet virksomhetUtenNyttArbeidsforhold;
    private VirksomhetEntitet virksomhetUtenNyttArbeidsforhold2;
    private BehandlingRepositoryProvider repositoryProvider;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste;

    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);

    @Before
    public void setup() {
        virksomhetMedNyttArbeidsforhold = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR_MED_NYTT_ARBEIDSFORHOLD)
            .medNavn("Virksomhet1")
            .medRegistrert(TWO_YEARS_AGO)
            .medOppstart(TWO_YEARS_AGO)
            .oppdatertOpplysningerNå()
            .build();
        virksomhetUtenNyttArbeidsforhold = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR_UTEN_NYTT_ARBEIDSFORHOLD)
            .medNavn("Virksomhet2")
            .medRegistrert(TWO_YEARS_AGO)
            .medOppstart(TWO_YEARS_AGO)
            .oppdatertOpplysningerNå()
            .build();
        realRepositoryProvider.getVirksomhetRepository().lagre(virksomhetUtenNyttArbeidsforhold);
        virksomhetUtenNyttArbeidsforhold2 = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR_UTEN_NYTT_ARBEIDSFORHOLD2)
            .medNavn("Virksomhet3")
            .medRegistrert(TWO_YEARS_AGO)
            .medOppstart(TWO_YEARS_AGO)
            .oppdatertOpplysningerNå()
            .build();
        realRepositoryProvider.getVirksomhetRepository().lagre(virksomhetUtenNyttArbeidsforhold2);
    }

    private void mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType relatertYtelseType, int dekningsgrad, Arbeidskategori arbeidskategori, VirksomhetEntitet virksomhet) {
        mockBehandlingOgBeregningsgrunnlag(relatertYtelseType, dekningsgrad, arbeidskategori, virksomhet, false, null);
    }

    private void mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType relatertYtelseType, int dekningsgrad, Arbeidskategori arbeidskategori, VirksomhetEntitet virksomhet,
                                                    boolean medEkstraAndel, Virksomhet virksomhet2) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        mockTidligereYtelse(scenario, relatertYtelseType, dekningsgrad, arbeidskategori, virksomhet, medEkstraAndel, virksomhet2);
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
                .medAktivitetStatus(AktivitetStatus.TILSTØTENDE_YTELSE))
            .leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
                .medBeregningsgrunnlagPeriode(LocalDate.now(), null));
        beregningsgrunnlag = beregningsgrunnlagBuilder.build();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        when(repositoryProvider.getVirksomhetRepository()).thenReturn(realRepositoryProvider.getVirksomhetRepository());
        mockOpptjeningRepository(repositoryProvider, SKJÆRINGSTIDSPUNKT);
        behandling = scenario.lagMocked();
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
        opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);
        beregningsgrunnlagFraTilstøtendeYtelseTjeneste = new BeregningsgrunnlagFraTilstøtendeYtelseTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, mapBeregningsgrunnlagFraTilstøtendeYtelseFraVLTilRegel, mapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL, skjæringstidspunktTjeneste);
        hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
    }

    private void mockTidligereYtelse(ScenarioMorSøkerForeldrepenger scenario, RelatertYtelseType relatertYtelseType, int dekningsgrad, Arbeidskategori arbeidskategori, VirksomhetEntitet virksomhet, boolean medEkstraAndel, Virksomhet virksomhet2) {
        YtelseBuilder ytelseBuilder = YtelseBuilder.oppdatere(Optional.empty())
            .medYtelseType(relatertYtelseType)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusDays(6), SKJÆRINGSTIDSPUNKT.plusDays(8)))
            .medKilde(Fagsystem.INFOTRYGD);

        YtelseStørrelse ytelseStørrelse = YtelseStørrelseBuilder.ny()
            .medBeløp(MÅNEDSBELØP_TILSTØTENDE_YTELSE)
            .medHyppighet(InntektPeriodeType.MÅNEDLIG)
            .medVirksomhet(virksomhet)
            .build();
        YtelseGrunnlagBuilder ytelseGrunnlagBuilder = ytelseBuilder.getGrunnlagBuilder()
            .medArbeidskategori(arbeidskategori);
        if (medEkstraAndel) {
            YtelseStørrelse ekstraYtelseStørrelse = YtelseStørrelseBuilder.ny()
                .medBeløp(ÅRSSBELØP_TILSTØTENDE_YTELSE_2)
                .medHyppighet(InntektPeriodeType.FASTSATT25PAVVIK)
                .medVirksomhet(virksomhet2)
                .build();
            ytelseGrunnlagBuilder.medYtelseStørrelse(ekstraYtelseStørrelse);
        }
        ytelseGrunnlagBuilder.medYtelseStørrelse(ytelseStørrelse);
        if (RelatertYtelseType.FORELDREPENGER.equals(relatertYtelseType)) {
            ytelseGrunnlagBuilder.medDekningsgradProsent(BigDecimal.valueOf(dekningsgrad));
        }
        if (RelatertYtelseType.SYKEPENGER.equals(relatertYtelseType)) {
            ytelseGrunnlagBuilder.medInntektsgrunnlagProsent(BigDecimal.valueOf(dekningsgrad));
        }
        YtelseGrunnlag ytelseGrunnlag = ytelseGrunnlagBuilder
            .build();
        ytelseBuilder.medYtelseGrunnlag(ytelseGrunnlag);
        scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd().leggTilAktørYtelse(
            InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty())
                .leggTilYtelse(ytelseBuilder)
                .medAktørId(AKTØR_ID)
        );
    }

    private void mockOpptjeningRepository(BehandlingRepositoryProvider repositoryProvider, LocalDate opptjeningTom) {
        OpptjeningRepository opptjeningRepository = mock(OpptjeningRepository.class);
        Opptjening opptjening = mock(Opptjening.class);
        when(opptjening.getTom()).thenReturn(opptjeningTom);
        when(opptjeningRepository.finnOpptjening(any(Behandling.class))).thenReturn(Optional.of(opptjening));
        when(repositoryProvider.getOpptjeningRepository()).thenReturn(opptjeningRepository);
    }


    @Test
    public void skalOppretteAndelerForTYForeldrepengerForFisker() {

        // ARBKAT 00

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.FISKER, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.FISKER, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForArbeidstaker() {

        // ARBKAT 01 08 09 12 27

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForArbeidstakerMedToArbeidsforhold() {

        // ARBKAT 01 08 09 12 27

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.ARBEIDSTAKER,
            virksomhetUtenNyttArbeidsforhold, true, virksomhetUtenNyttArbeidsforhold2);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold2, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForSelvstendigNæringsdrivende() {

        // ARBKAT 02 15 16

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForSjømann() {

        // ARBKAT 04 10

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.SJØMANN, virksomhetUtenNyttArbeidsforhold);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.SJØMANN, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForJordbruker() {

        // ARBKAT 05

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.JORDBRUKER, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.JORDBRUKER, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForDagpenger() {

        // ARBKAT 06

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.DAGPENGER, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForInaktiv() {

        // ARBKAT 07

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.INAKTIV, virksomhetUtenNyttArbeidsforhold);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForFrilanser() {

        // ARBKAT 19 24

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.FRILANSER, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForDagmamma() {

        // ARBKAT 19 24

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.DAGMAMMA, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.DAGMAMMA, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForKombinasjonArbeidstakerOgSelvstendigNæringsdrivende() {

        // ARBKAT 03

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100,
            Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE,
            virksomhetUtenNyttArbeidsforhold,
            true, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForKombinasjonArbeidstakerOgJordbruker() {

        // ARBKAT 13

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER, virksomhetUtenNyttArbeidsforhold, true, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.JORDBRUKER, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForKombinasjonArbeidstakerOgFisker() {

        // ARBKAT 17

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FISKER, virksomhetUtenNyttArbeidsforhold, true, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.FISKER, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForKombinasjonArbeidstakerOgFrilanser() {

        // ARBKAT 20 25

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER, virksomhetUtenNyttArbeidsforhold, true, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForKombinasjonArbeidstakerOgDagpenger() {

        // ARBKAT 23

        // arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER, virksomhetUtenNyttArbeidsforhold, true, null);

        // act
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetUtenNyttArbeidsforhold, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1),
            AktivitetStatus.DAGPENGER, Inntektskategori.DAGPENGER, RelatertYtelseType.FORELDREPENGER, ÅRSSBELØP_TILSTØTENDE_YTELSE_2, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForSNMedNyttArbeidsforhold() {
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE, virksomhetMedNyttArbeidsforhold);

        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYSykepengerForArbeidstaker() {
        // Arrange
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.SYKEPENGER, 100, Arbeidskategori.ARBEIDSTAKER, virksomhetMedNyttArbeidsforhold);

        // Act 1
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // Assert
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetMedNyttArbeidsforhold, RelatertYtelseType.SYKEPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);

        // Act 2
        final BehandlingRepositoryProviderImpl repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, mock(SkjæringstidspunktTjeneste.class), hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(realRepositoryProvider, inntektArbeidYtelseTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, realRepositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);
        // Assert 2
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, virksomhetMedNyttArbeidsforhold, RelatertYtelseType.SYKEPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYSykepenger65prosentInaktiv() {
        // Arrange 1
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.SYKEPENGER, 65, Arbeidskategori.INAKTIV, null);

        // Act 1
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // Assert 1
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017 * 0.65));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, null, RelatertYtelseType.SYKEPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);

        // Arrange 2
        final BehandlingRepositoryProviderImpl repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, mock(SkjæringstidspunktTjeneste.class), hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(realRepositoryProvider, inntektArbeidYtelseTjeneste);
        // Act 2
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, realRepositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);
        // Assert 2
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, null, RelatertYtelseType.SYKEPENGER, ÅRSBELØP_TILSTØTENDE_YTELSE, null);
    }

    @Test
    public void skalOppretteAndelerForTYForeldrepengerForSNmedEkstraAndel() {
        // Arrange 1
        BigDecimal brutto1 = ÅRSBELØP_TILSTØTENDE_YTELSE;
        BigDecimal brutto2 = BigDecimal.valueOf(520000L);
        mockBehandlingOgBeregningsgrunnlag(RelatertYtelseType.FORELDREPENGER, 100, Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE, null);

        // Act 1
        Beregningsgrunnlag beregningsgrunnlag = beregningsgrunnlagFraTilstøtendeYtelseTjeneste.opprettBeregningsgrunnlagFraTilstøtendeYtelse(behandling, this.beregningsgrunnlag);

        // Assert 1
        assertThat(beregningsgrunnlag.getGrunnbeløp().getVerdi()).as("grunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getRedusertGrunnbeløp().getVerdi()).as("redusertGrunnbeløp").isEqualByComparingTo(BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017));
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);

        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, brutto1, null);

        // Arrange 2
        BeregningsgrunnlagPrStatusOgAndel.builder(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medBeregnetPrÅr(brutto1)
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBeregnetPrÅr(brutto2)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(periode);

        // Act 2
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(realRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, mock(SkjæringstidspunktTjeneste.class), hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(realRepositoryProvider, inntektArbeidYtelseTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, realRepositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);
        beregningsgrunnlag = resultat.getBeregningsgrunnlag();

        // Assert 2
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, brutto1, null);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER, RelatertYtelseType.UDEFINERT, brutto2, null);

        // Act 3
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel);
        Beregningsgrunnlag fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullføreBeregningsgrunnlag, Collections.emptyList());
        beregningsgrunnlag = resultat.getBeregningsgrunnlag();

        // Assert 3
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        BigDecimal avkortet1 = ÅRSBELØP_TILSTØTENDE_YTELSE;
        BigDecimal avkortet2 = BigDecimal.valueOf(GrunnbeløpForTest.GRUNNBELØP_2017 * 6).subtract(ÅRSBELØP_TILSTØTENDE_YTELSE);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, RelatertYtelseType.FORELDREPENGER, brutto1, avkortet1);
        verifiserAndel(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER, RelatertYtelseType.UDEFINERT, brutto2, avkortet2);
    }

    private void verifiserAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                AktivitetStatus aktivitetStatus,
                                Inntektskategori inntektskategori,
                                RelatertYtelseType relatertYtelse,
                                BigDecimal bruttoPrÅr,
                                BigDecimal avkortetPrÅr) {
        verifiserAndel(andel, aktivitetStatus, inntektskategori, null, relatertYtelse, bruttoPrÅr, avkortetPrÅr);
    }

    private void verifiserAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                AktivitetStatus aktivitetStatus,
                                Inntektskategori inntektskategori,
                                VirksomhetEntitet virksomhet,
                                RelatertYtelseType relatertYtelse,
                                BigDecimal bruttoPrÅr,
                                BigDecimal avkortetPrÅr) {
        assertThat(andel.getAktivitetStatus()).isEqualTo(aktivitetStatus);
        assertThat(andel.getInntektskategori()).isEqualTo(inntektskategori);
        assertThat(andel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEqualTo(Optional.ofNullable(virksomhet));
        assertThat(andel.getYtelse()).isEqualTo(relatertYtelse);
        if (bruttoPrÅr == null) {
            assertThat(andel.getBruttoPrÅr()).isNull();
        } else {
            assertThat(andel.getBruttoPrÅr()).isCloseTo(bruttoPrÅr, Offset.offset(BigDecimal.valueOf(0.01)));
        }
        if (avkortetPrÅr == null) {
            assertThat(andel.getAvkortetPrÅr()).isNull();
        } else {
            assertThat(andel.getAvkortetPrÅr()).isCloseTo(avkortetPrÅr, Offset.offset(BigDecimal.valueOf(0.01)));
        }
        if (aktivitetStatus.erFrilanser() || aktivitetStatus.erArbeidstaker()) {
            LocalDate skjæringstidspunkt = andel.getBeregningsgrunnlagPeriode().getBeregningsgrunnlag().getSkjæringstidspunkt();
            assertThat(andel.getBeregningsperiodeFom()).isEqualTo(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1));
            assertThat(andel.getBeregningsperiodeTom()).isEqualTo(skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
        } else {
            assertThat(andel.getBeregningsperiodeFom()).isNull();
            assertThat(andel.getBeregningsperiodeTom()).isNull();
        }
    }

}
