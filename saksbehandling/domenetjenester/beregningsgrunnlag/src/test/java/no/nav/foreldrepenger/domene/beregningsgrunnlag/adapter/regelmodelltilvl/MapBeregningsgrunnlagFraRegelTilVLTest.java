package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_DAYS_10;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_DAYS_20;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_DAYS_5;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_1;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_2;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.MINUS_YEARS_3;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildRegelBGPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildRegelBeregningsgrunnlag;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildRegelSammenligningsG;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGAktivitetStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPStatus;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPStatusForSN;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper.buildVLBGPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.RegelMapperTestDataHelper;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.AktivitetStatusModell;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class MapBeregningsgrunnlagFraRegelTilVLTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, Month.MARCH, 1);
    private static final LocalDate TWO_YEARS_AGO = SKJÆRINGSTIDSPUNKT.minusYears(2);

    private Behandling behandling;
    private VirksomhetEntitet virksomhet;
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    @Inject
    private MapBeregningsgrunnlagFraRegelTilVL mapBeregningsgrunnlagFraRegelTilVL;
    @Inject
    private BeregningIAYTestUtil iayTestUtil;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(AKTØR_ID));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr("42L")
                .medNavn("VirksomhetNavn")
                .oppdatertOpplysningerNå()
                .medRegistrert(TWO_YEARS_AGO)
                .medOppstart(TWO_YEARS_AGO)
                .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
    }

    @Test
    public void testMappingBGForSN() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = buildVLBG();

        List<RegelResultat> regelresultater = Arrays.asList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag mappedBG = new MapBeregningsgrunnlagFraRegelTilVL().mapFastsettBeregningsgrunnlag(buildRegelBGForSN(), "input", regelresultater, vlBG);

        assertThat(mappedBG).isNotSameAs(vlBG);
        assertThat(mappedBG.getSammenligningsgrunnlag().getSammenligningsperiodeFom()).isEqualTo(MINUS_YEARS_1);
        assertThat(mappedBG.getSammenligningsgrunnlag().getSammenligningsperiodeTom()).isEqualTo(MINUS_DAYS_20);
        assertThat(mappedBG.getSammenligningsgrunnlag().getRapportertPrÅr().doubleValue()).isEqualTo(42.0);
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGP = mappedBG.getBeregningsgrunnlagPerioder().get(0);
        assertThat(vlBGP.getBruttoPrÅr().doubleValue()).isEqualTo(400000.42, within(0.01));
        assertThat(vlBGP.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertVLBGPStatusSN(vlBGPStatus);
    }

    @Test
    public void testMappingBGForArbeidstaker() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = buildVLBGForAT();
        List<RegelResultat> regelresultater = Arrays.asList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));

        final SkjæringstidspunktTjenesteImpl skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        Beregningsgrunnlag resultatGrunnlag = buildRegelBGForAT();
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag mappedBG = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste).mapForeslåBeregningsgrunnlag(resultatGrunnlag, "input", regelresultater, vlBG);

        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGP = mappedBG.getBeregningsgrunnlagPerioder().get(0);

        assertThat(vlBGP.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus1 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertVLBGPStatusAT(vlBGPStatus1);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus2 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertVLBGPStatusFL(vlBGPStatus2);
    }

    @Test
    public void testMappingBGForATFLogSN() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = buildVLBGForATFLogSN();
        List<RegelResultat> regelresultater = Arrays.asList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag mappedBG = new MapBeregningsgrunnlagFraRegelTilVL().mapFastsettBeregningsgrunnlag(buildRegelBGForATFLogSN(), "input", regelresultater, vlBG);

        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGP = mappedBG.getBeregningsgrunnlagPerioder().get(0);

        assertThat(vlBGP.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(3);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertVLBGPStatusSN(vlBGPStatus);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus1 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(1);
        assertThat(vlBGPStatus1.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        assertVLBGPStatusAT(vlBGPStatus1);
        final BeregningsgrunnlagPrStatusOgAndel vlBGPStatus2 = vlBGP.getBeregningsgrunnlagPrStatusOgAndelList().get(2);
        assertThat(vlBGPStatus2.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER);
        assertVLBGPStatusFL(vlBGPStatus2);
    }

    @Test
    public void skal_mappe_beregningsgrunnlag_når_arbeidsgiver_er_privatperson() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018,1,1);
        String aktørId = "123123123123";
        List<RegelResultat> regelresultater = Arrays.asList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"), new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        AktivitetStatusModell regelmodell = lagRegelModell(skjæringstidspunkt, Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(aktørId));
        String inputSkjæringstidspunkt = toJson(regelmodell);
        iayTestUtil.byggArbeidForBehandling(behandling, skjæringstidspunkt, skjæringstidspunkt.minusYears(1), skjæringstidspunkt, null, Arbeidsgiver.person(new AktørId(aktørId)));

        // Act
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL
            .mapForSkjæringstidspunktOgStatuser(behandling, regelmodell, Dekningsgrad.DEKNINGSGRAD_100, Arrays.asList(inputSkjæringstidspunkt, inputSkjæringstidspunkt), regelresultater);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndel andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Andel asserts
        assertThat(andel.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        // Arbeidsforhold asserts
        assertThat(andel.getBgAndelArbeidsforhold()).isPresent();
        BGAndelArbeidsforhold bga = andel.getBgAndelArbeidsforhold().get();
        assertThat(bga.getArbeidsgiver()).isPresent();
        assertThat(bga.getArbeidsgiver().get().getErVirksomhet()).isFalse();
        assertThat(bga.getArbeidsgiver().get().getIdentifikator()).isEqualTo(aktørId);
    }

    @Test
    public void skal_mappe_beregningsgrunnlag_når_arbeidsgiver_er_virksomhet() {
        // Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018,1,1);
        List<RegelResultat> regelresultater = Arrays.asList(new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"), new RegelResultat(ResultatBeregningType.BEREGNET, "sporing"));
        AktivitetStatusModell regelmodell = lagRegelModell(skjæringstidspunkt, Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(virksomhet.getOrgnr()));
        String inputSkjæringstidspunkt = toJson(regelmodell);
        iayTestUtil.byggArbeidForBehandling(behandling, skjæringstidspunkt, skjæringstidspunkt.minusYears(1), skjæringstidspunkt, null, Arbeidsgiver.virksomhet(virksomhet));

        // Act
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = mapBeregningsgrunnlagFraRegelTilVL
            .mapForSkjæringstidspunktOgStatuser(behandling, regelmodell, Dekningsgrad.DEKNINGSGRAD_100, Arrays.asList(inputSkjæringstidspunkt, inputSkjæringstidspunkt), regelresultater);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);
        BeregningsgrunnlagPrStatusOgAndel andel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        // Andel asserts
        assertThat(andel.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        // Arbeidsforhold asserts
        assertThat(andel.getBgAndelArbeidsforhold()).isPresent();
        BGAndelArbeidsforhold bga = andel.getBgAndelArbeidsforhold().get();
        assertThat(bga.getArbeidsgiver()).isPresent();
        assertThat(bga.getArbeidsgiver().get().getErVirksomhet()).isTrue();
        assertThat(bga.getArbeidsgiver().get().getIdentifikator()).isEqualTo(virksomhet.getOrgnr());
    }

    private void assertVLBGPStatusSN(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(vlBGPStatus.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(vlBGPStatus.getBeregnetPrÅr().doubleValue()).isEqualTo(400000.42, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().doubleValue()).isEqualTo(400000.42, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).isEmpty();
        assertThat(vlBGPStatus.getAvkortetPrÅr().doubleValue()).isEqualTo(789.789, within(0.01));
        assertThat(vlBGPStatus.getRedusertPrÅr().doubleValue()).isEqualTo(901.901, within(0.01));
        assertThat(vlBGPStatus.getDagsatsArbeidsgiver()).isEqualTo(0L);
    }

    private void assertVLBGPStatusFL(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER);
        assertThat(vlBGPStatus.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEmpty();
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(vlBGPStatus.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(vlBGPStatus.getBeregnetPrÅr().doubleValue()).isEqualTo(456.456, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().doubleValue()).isEqualTo(456.456, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).get().doubleValue()).isEqualTo(45.45, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(vlBGPStatus.getAvkortetPrÅr().doubleValue()).isEqualTo(34.34, within(0.01));
        assertThat(vlBGPStatus.getRedusertPrÅr().doubleValue()).isEqualTo(65.65, within(0.01));
        assertThat(vlBGPStatus.getDagsatsArbeidsgiver()).isEqualTo(5L);
    }

    private void assertVLBGPStatusAT(BeregningsgrunnlagPrStatusOgAndel vlBGPStatus) {
        assertThat(vlBGPStatus.getAktivitetStatus()).isEqualTo(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER);
        assertThat(vlBGPStatus.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virksomhet ->
            assertThat(virksomhet.getOrgnr()).isEqualTo("42L"));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(vlBGPStatus.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(vlBGPStatus.getBeregnetPrÅr().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(vlBGPStatus.getBruttoPrÅr().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(vlBGPStatus.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(123.123, within(0.01));
        assertThat(vlBGPStatus.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).get().doubleValue()).isEqualTo(87.87, within(0.01));
        assertThat(vlBGPStatus.getAvkortetPrÅr().doubleValue()).isEqualTo(57.57, within(0.01));
        assertThat(vlBGPStatus.getRedusertPrÅr().doubleValue()).isEqualTo(89.89, within(0.01));
        assertThat(vlBGPStatus.getDagsatsArbeidsgiver()).isEqualTo(10L);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag buildVLBG() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = RegelMapperTestDataHelper.buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG);
        buildVLBGPStatusForSN(buildVLBGPeriode(vlBG));
        return vlBG;
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag buildVLBGForAT() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = RegelMapperTestDataHelper.buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG);
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode = buildVLBGPeriode(vlBG);
        buildVLBGPStatus(vlBGPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2,
            MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhet), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(vlBGPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, MINUS_YEARS_3, MINUS_YEARS_2);
        return vlBG;
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag buildVLBGForATFLogSN() {
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag vlBG = RegelMapperTestDataHelper.buildVLBeregningsgrunnlag();
        buildVLBGAktivitetStatus(vlBG);
        final no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode vlBGPeriode = buildVLBGPeriode(vlBG);
        buildVLBGPStatusForSN(vlBGPeriode);
        buildVLBGPStatus(vlBGPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER, MINUS_YEARS_2,
            MINUS_YEARS_1, Arbeidsgiver.virksomhet(virksomhet), OpptjeningAktivitetType.ARBEID);
        buildVLBGPStatus(vlBGPeriode, no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER, Inntektskategori.FRILANSER, MINUS_YEARS_3, MINUS_YEARS_2);
        return vlBG;
    }

    private Beregningsgrunnlag buildRegelBGForSN() {
        final Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.SN,
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE,
            BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG);
        Beregningsgrunnlag.builder(regelBG).medSammenligningsgrunnlag(buildRegelSammenligningsG()).build();

        final BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPeriodeSN(regelBGP);
        return regelBG;
    }

    private Beregningsgrunnlag buildRegelBGForAT() {
        final Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL,
            null,
            BeregningsgrunnlagHjemmel.HJEMMEL_BARE_ARBEIDSTAKER);
        Beregningsgrunnlag.builder(regelBG).medSammenligningsgrunnlag(buildRegelSammenligningsG()).build();

        final BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPStatusATFL(regelBGP, 1);
        return regelBG;
    }

    private Beregningsgrunnlag buildRegelBGForATFLogSN() {
        final Beregningsgrunnlag regelBG = buildRegelBeregningsgrunnlag(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL_SN,
            null,
            BeregningsgrunnlagHjemmel.HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG);
        Beregningsgrunnlag.builder(regelBG).medSammenligningsgrunnlag(buildRegelSammenligningsG()).build();

        final BeregningsgrunnlagPeriode regelBGP = regelBG.getBeregningsgrunnlagPerioder().get(0);

        buildRegelBGPeriodeSN(regelBGP);
        buildRegelBGPStatusATFL(regelBGP, 2);
        return regelBG;
    }

    private void buildRegelBGPeriodeSN(BeregningsgrunnlagPeriode regelBGP) {
        buildRegelBGPeriode(regelBGP, AktivitetStatus.SN, new Periode(MINUS_DAYS_10, MINUS_DAYS_5));
    }

    private void buildRegelBGPStatusATFL(BeregningsgrunnlagPeriode regelBGP, long andelNr) {
        final BeregningsgrunnlagPrStatus regelBGPStatus = buildRegelBGPeriode(regelBGP, AktivitetStatus.ATFL, new Periode(MINUS_YEARS_2, MINUS_YEARS_1));
        final BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold42 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("42L"))
                .medInntektskategori(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER)
                .medAndelNr(andelNr++)
                .medBeregnetPrÅr(BigDecimal.valueOf(123.123))
                .medMaksimalRefusjonPrÅr(BigDecimal.valueOf(123.123))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(87.87))
                .medAvkortetPrÅr(BigDecimal.valueOf(57.57))
                .medRedusertPrÅr(BigDecimal.valueOf(89.89))
                .medRedusertRefusjonPrÅr(BigDecimal.valueOf(2600.0))
                .build();
        regelBGPStatus.getArbeidsforhold().add(regelArbeidsforhold42);

        final BeregningsgrunnlagPrArbeidsforhold regelArbeidsforhold66 = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
                .medInntektskategori(no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.FRILANSER)
                .medAndelNr(andelNr++)
                .medBeregnetPrÅr(BigDecimal.valueOf(456.456))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(45.45))
                .medAvkortetPrÅr(BigDecimal.valueOf(34.34))
                .medRedusertPrÅr(BigDecimal.valueOf(65.65))
                .medRedusertRefusjonPrÅr(BigDecimal.valueOf(1300.0))
                .build();
        regelBGPStatus.getArbeidsforhold().add(regelArbeidsforhold66);
    }

    private String toJson(AktivitetStatusModell grunnlag) {
        JacksonJsonConfig jacksonJsonConfig = new JacksonJsonConfig();
        return jacksonJsonConfig.toJson(grunnlag, null);
    }

    private AktivitetStatusModell lagRegelModell(LocalDate skjæringstidspunkt, Arbeidsforhold arbeidsforhold) {
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunkt);
        regelmodell.setSkjæringstidspunktForOpptjening(skjæringstidspunkt);
        regelmodell.leggTilAktivitetStatus(AktivitetStatus.ATFL);
        no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.BeregningsgrunnlagPrStatus bgPrStatus = new no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt.BeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        bgPrStatus.getArbeidsforholdList().add(arbeidsforhold);
        regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
        return regelmodell;
    }


}
