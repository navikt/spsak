package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_1;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_2;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.NOW;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGAktivitetStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBeregningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.util.Tuple;

public class MapBeregningsgrunnlagFraVLTilRegelOgTilbakeTest {

    private static final AktørId AKTØR_ID = new AktørId(1234L);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider realRepositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider realResultatProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    @Mock
    private PersonopplysningRepository personopplysningRepository;

    @Mock
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;

    @Mock
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private Behandling behandling;

    private GrunnlagRepositoryProvider repositoryProvider;

    private VirksomhetEntitet virksomhet;

    private VirksomhetRepository virksomhetRepository = realRepositoryProvider.getVirksomhetRepository();

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        behandling = scenario.lagre(realRepositoryProvider, realResultatProvider);
        Tuple<GrunnlagRepositoryProvider, ResultatRepositoryProvider> providerTuple = scenario.mockBehandlingRepositoryProvider();
        repositoryProvider = providerTuple.getElement1();
        when(repositoryProvider.getPersonopplysningRepository()).thenReturn(personopplysningRepository);
        when(repositoryProvider.getSatsRepository()).thenReturn(realRepositoryProvider.getSatsRepository());
        when(repositoryProvider.getVirksomhetRepository()).thenReturn(virksomhetRepository);
        when(hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling)).thenReturn(true);

        virksomhet = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("42L").oppdatertOpplysningerNå().build();
        virksomhetRepository.lagre(virksomhet);
        BigDecimal inntektInntektsmelding = BigDecimal.valueOf(20000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(19000);
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(realRepositoryProvider, behandling, virksomhet, inntektInntektsmelding, refusjonskrav);

        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, providerTuple.getElement2(), skjæringstidspunktTjeneste);
        inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    }

    @Test
    public void skal_sjekke_mapping_er_konsistent_til_og_fra_regelmodell_for_ATFL() {
        //Arrange
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2,
            MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhet), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(bgPeriode, AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, MINUS_YEARS_1, NOW, null, OpptjeningAktivitetType.FRILANS);
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, realResultatProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        List<RegelResultat> regelresultater = Collections.singletonList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag resultatBG2 = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatBG, "input", regelresultater, beregningsgrunnlag);
        //Assert
        assertVLBGPRegelStatus(resultatBG2.getAktivitetStatuser().get(0));
        assertVLBGPStatusAT(resultatBG2.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0), OpptjeningAktivitetType.ARBEID,Arbeidsgiver.virksomhet(virksomhet));
        assertVLBGPStatusFL(resultatBG2.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(1));
    }

    @Test
    public void skal_sjekke_mapping_er_konsistent_til_og_fra_regelmodell_for_AT_arbeidsgiver_privatperson() {
        //Arrange
        String aktørId = "123123123123";
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_1,
            NOW, Arbeidsgiver.person(new AktørId(aktørId)), OpptjeningAktivitetType.ARBEID);
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, realResultatProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        List<RegelResultat> regelresultater = Collections.singletonList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag resultatBG2 = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatBG, "input", regelresultater, beregningsgrunnlag);
        //Assert
        assertVLBGPRegelStatus(resultatBG2.getAktivitetStatuser().get(0));
        assertVLBGPStatusAT(resultatBG2.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0), OpptjeningAktivitetType.ARBEID, Arbeidsgiver.person(new AktørId(aktørId)));
    }


    @Test
    public void skal_sjekke_mapping_er_konsistent_til_og_fra_regelmodell_for_arbeidstaker_uten_arbeidsforhold() {
        //Arrange
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
        buildVLBGPStatus(bgPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, Inntektskategori.FRILANSER, MINUS_YEARS_2,
            MINUS_YEARS_1, null, OpptjeningAktivitetType.VARTPENGER);
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, realResultatProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        List<RegelResultat> regelresultater = Collections.singletonList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag resultatBG2 = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatBG, "input", regelresultater, beregningsgrunnlag);
        //Assert
        assertVLBGPRegelStatus(resultatBG2.getAktivitetStatuser().get(0));
        assertVLBGPStatusAT(resultatBG2.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            OpptjeningAktivitetType.VARTPENGER, Arbeidsgiver.virksomhet(virksomhet));
    }

    private void assertVLBGPRegelStatus(BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus) {
        assertThat(beregningsgrunnlagAktivitetStatus.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(beregningsgrunnlagAktivitetStatus.getHjemmel()).isEqualTo(Hjemmel.F_14_7_8_30);
    }

    @Test
    public void skal_sjekke_mapping_for_periodeårsaker() {
        //Arrange
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        final List<PeriodeÅrsak> årsaker = Arrays.asList(PeriodeÅrsak.UDEFINERT,PeriodeÅrsak.NATURALYTELSE_BORTFALT, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        for (int i = 0; i < årsaker.size(); i++) {
            BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
            BeregningsgrunnlagPeriode.builder(bgPeriode)
                .leggTilPeriodeÅrsak(årsaker.get(i))
                .medBeregningsgrunnlagPeriode(LocalDate.now().plusMonths(i), LocalDate.now().plusMonths(i + 1).minusDays(1)).build(beregningsgrunnlag);
        }
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, realResultatProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        List<RegelResultat> regelresultater = årsaker.stream().map(a -> new RegelResultat(ResultatBeregningType.BEREGNET, "sporing")).collect(Collectors.toList());
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag resultatBG2 = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatBG, "input", regelresultater, beregningsgrunnlag);
        //Assert
        List<BeregningsgrunnlagPeriode> perioder = resultatBG2.getBeregningsgrunnlagPerioder();
        assertThat(perioder.size()).isEqualTo(årsaker.size());
        assertThat(perioder.get(0).getPeriodeÅrsaker().isEmpty()).isTrue();
        for (int i = 1; i < årsaker.size(); i++) {
            assertThat(perioder.get(i).getPeriodeÅrsaker().get(0)).isEqualTo(årsaker.get(i));
        }
    }

    @Test
    public void skal_sjekke_mapping_for_flere_perioder_med_ulike_antall_andeler() {
        //Arrange
        VirksomhetEntitet virksomhetA = new VirksomhetEntitet.Builder().medNavn("OrgA").medOrgnr("42").oppdatertOpplysningerNå().build();
        VirksomhetEntitet virksomhetB = new VirksomhetEntitet.Builder().medNavn("OrgB").medOrgnr("47").oppdatertOpplysningerNå().build();
        VirksomhetEntitet virksomhetC = new VirksomhetEntitet.Builder().medNavn("OrgC").medOrgnr("123").oppdatertOpplysningerNå().build();
        List<VirksomhetEntitet> virksomheter = Arrays.asList(virksomhetA, virksomhetB, virksomhetC);

        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(beregningsgrunnlag);
        for (int i = 0; i < 3; i++) {
            BeregningsgrunnlagPeriode bgPeriode = buildVLBGPeriode(beregningsgrunnlag);
            for (int j = 0; j <i+1; j++) {
                buildVLBGPStatus(bgPeriode, AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, LocalDate.now().minusMonths(3), LocalDate.now(), Arbeidsgiver.virksomhet(virksomheter.get(j)), OpptjeningAktivitetType.ARBEID);
            }
            BeregningsgrunnlagPeriode.builder(bgPeriode)
                .medBeregningsgrunnlagPeriode(LocalDate.now().plusMonths(i), LocalDate.now().plusMonths(i + 1).minusDays(1))
                .build(beregningsgrunnlag);
        }
        //Act
        final no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag resultatBG = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, realResultatProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5).map(behandling, beregningsgrunnlag);
        List<RegelResultat> regelresultater = Stream.of(1,2,3).map(a -> new RegelResultat(ResultatBeregningType.BEREGNET, "sporing")).collect(Collectors.toList());
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag resultatBG2 = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatBG, "input", regelresultater, beregningsgrunnlag);
        //Assert
        List<BeregningsgrunnlagPeriode> perioder = resultatBG2.getBeregningsgrunnlagPerioder();
        assertThat(perioder.size()).isEqualTo(3);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        assertThat(perioder.get(2).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);

    }

    private void assertVLBGPStatusFL(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER);
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEmpty();
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(vlBGPStatus.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(vlBGPStatus.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
        assertThat(vlBGPStatus.getOverstyrtPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
    }

    private void assertVLBGPStatusAT(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus, OpptjeningAktivitetType arbforholdType, Arbeidsgiver arbeidsgiver) {
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        if (OpptjeningAktivitetType.ARBEID.equals(arbforholdType)) {
            assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver)).hasValueSatisfying(arb ->
                assertThat(arb).isEqualTo(arbeidsgiver));
        } else {
            assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver)).isEmpty();
        }
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(vlBGPStatus.getArbeidsforholdType()).isEqualTo(arbforholdType);
        assertThat(vlBGPStatus.getBeregnetPrÅr().doubleValue()).isEqualTo(1000.01, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).get().doubleValue()).isEqualTo(3232.32, within(0.01));
        assertThat(vlBGPStatus.getOverstyrtPrÅr().doubleValue()).isEqualTo(4444432.32, within(0.01));
    }
}
