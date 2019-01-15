package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.TilstøtendeYtelseAndelDto;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TilstøtendeYtelseDtoTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);
    private static final BigDecimal INNTEKT_PR_MND = BigDecimal.valueOf(15000L);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private TilstøtendeYtelseDtoTjenesteImpl tilstøtendeYtelseDtoTjeneste;
    @Inject
    private BeregningsgrunnlagTestUtil testUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;
    private Behandling behandling;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(BeregningIAYTestUtil.AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void skal_lage_tilstøtende_ytelse_andel_med_virksomhet() {
        // Arrange
        String orgnr = "123234238";
        String arbId = "32423355";
        Long andelsnr = 2L;
        boolean lagtTilAvSaksbehandler = false;
        LocalDate arbeidsperiodeFom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(3);
        LocalDate arbeidsperiodeTom = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1);
        BigDecimal årsbeløpFraTilstøtendeYtelse = BigDecimal.valueOf(100000);
        Inntektskategori inntektskategori = Inntektskategori.ARBEIDSTAKER;
        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        BigDecimal beregnet = BigDecimal.valueOf(100000);
        BigDecimal overstyrt = BigDecimal.valueOf(80000);
        BigDecimal refusjonskrav = BigDecimal.valueOf(10000);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING)
            .medGrunnbeløp(testUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING))
            .medRedusertGrunnbeløp(testUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING))
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, null)
            .build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbId)
            .medArbeidsgiver(beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr))
            .medArbeidsperiodeFom(arbeidsperiodeFom)
            .medArbeidsperiodeTom(arbeidsperiodeTom)
            .medRefusjonskravPrÅr(refusjonskrav);

        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(lagtTilAvSaksbehandler)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .medBeregnetPrÅr(beregnet)
            .medOverstyrtPrÅr(overstyrt)
            .medÅrsbeløpFraTilstøtendeYtelse(årsbeløpFraTilstøtendeYtelse)
            .medBGAndelArbeidsforhold(bga)
            .build(periode1);

        TilstøtendeYtelseAndelDto andelDto = tilstøtendeYtelseDtoTjeneste.lagTilstøtendeYtelseAndel(behandling, andel);
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverNavn()).isEqualTo("Beregningvirksomhet");
        assertThat(andelDto.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(orgnr);
        assertThat(andelDto.getArbeidsforhold().getArbeidsforholdId()).isEqualTo(arbId);
        assertThat(andelDto.getArbeidsforhold().getOpphoersdato()).isEqualTo(arbeidsperiodeTom);
        assertThat(andelDto.getArbeidsforhold().getStartdato()).isEqualTo(arbeidsperiodeFom);
        assertThat(andelDto.getFordelingForrigeYtelse()).isEqualByComparingTo(årsbeløpFraTilstøtendeYtelse);
        assertThat(andelDto.getRefusjonskrav()).isEqualByComparingTo(refusjonskrav);
        assertThat(andelDto.getInntektskategori()).isEqualByComparingTo(inntektskategori);
        assertThat(andelDto.getFastsattPrAar()).isEqualByComparingTo(beregnet);
        assertThat(andelDto.getAktivitetStatus()).isEqualByComparingTo(aktivitetStatus);
        assertThat(andelDto.getLagtTilAvSaksbehandler()).isEqualTo(lagtTilAvSaksbehandler);
        assertThat(andelDto.getAndelIArbeid()).containsExactly(BigDecimal.ZERO);
    }

    @Test
    public void skal_legge_til_andel_som_er_lagt_til_manuelt_ved_forrige_faktaavklaring() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        Periode arbeidsperiode = new Periode(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(10), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1));
        Map<String, Periode> opptjeningPeriodeMap = new HashMap<>();
        opptjeningPeriodeMap.put(orgnr, arbeidsperiode);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, opptjeningPeriodeMap);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, arbeidsperiode, arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr), INNTEKT_PR_MND);

        List<List<PeriodeÅrsak>> periodeÅrsakerIGjeldendeGrunnlag = Collections.singletonList(Collections.emptyList());
        List<LocalDateInterval> perioderIGjeldendeGrunnlag = Collections.singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));
        Map<String, Integer> gjeldendeBruttoBrÅr = new HashMap<>();
        gjeldendeBruttoBrÅr.put(orgnr, 0);
        Map<String, Integer> avkortetPrÅr = new HashMap<>();
        Map<String, List<Boolean>> lagtTilAvSaksbehandler = new HashMap<>();
        lagtTilAvSaksbehandler.put(orgnr, Arrays.asList(false, true));
        Map<String, List<Inntektskategori>> inntektskategori = new HashMap<>();
        inntektskategori.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.SJØMANN));
        testUtil.lagForrigeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, lagtTilAvSaksbehandler, perioderIGjeldendeGrunnlag, periodeÅrsakerIGjeldendeGrunnlag, inntektskategori);


        List<List<PeriodeÅrsak>> periodeÅrsakerINyttBG = Arrays.asList(Collections.emptyList(), Collections.singletonList(PeriodeÅrsak.GRADERING));
        List<LocalDateInterval> perioderINyttBG = Arrays.asList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1)),
            new LocalDateInterval(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(1).plusDays(1), null));
        Map<String, List<Boolean>> lagtTilAvSaksbehandlerEndring = new HashMap<>();
        lagtTilAvSaksbehandlerEndring.put(orgnr, Arrays.asList(false));
        Map<String, List<Inntektskategori>> inntektskategoriEndring = new HashMap<>();
        inntektskategoriEndring.put(orgnr, Arrays.asList(Inntektskategori.ARBEIDSTAKER));
        Beregningsgrunnlag bg = testUtil.lagBeregningsgrunnlagForEndring(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, avkortetPrÅr, gjeldendeBruttoBrÅr, periodeÅrsakerINyttBG, perioderINyttBG, lagtTilAvSaksbehandlerEndring, inntektskategoriEndring, Collections.emptyMap());
        List<TilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler = new ArrayList<>();


        assertThat(bg.getBeregningsgrunnlagPerioder()).hasSize(2);

        // Act
        tilstøtendeYtelseDtoTjeneste.leggTilAndelerSomErLagtTilManueltAvSaksbehandlerIForrigeFaktaavklaring(behandling, bg.getBeregningsgrunnlagPerioder().get(0), tilstøtendeYtelseAndeler);

        // Assert
        assertThat(tilstøtendeYtelseAndeler).hasSize(1);
        assertThat(tilstøtendeYtelseAndeler.get(0).getInntektskategori()).isEqualByComparingTo(Inntektskategori.SJØMANN);

    }


}
