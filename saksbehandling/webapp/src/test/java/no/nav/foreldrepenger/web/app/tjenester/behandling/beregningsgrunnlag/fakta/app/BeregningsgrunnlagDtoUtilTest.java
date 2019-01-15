package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
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
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagArbeidsforholdDto;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

public class BeregningsgrunnlagDtoUtilTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final BigDecimal GRUNNBELØP = BigDecimal.TEN;
    private static final String PRIVATPERSON_NAVN = "Donald Duck";
    private static final String PRIVATPERSON_IDENT = "27357455123";
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private BeregningsgrunnlagDtoUtil beregningsgrunnlagDtoUtil;
    private BeregningArbeidsgiverTestUtil virksomhetTestUtil;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    @Mock
    private TpsTjeneste tpsTjenesteMock;

    @Before
    public void setUp() {
        initMocks(this);
        when(tpsTjenesteMock.hentBrukerForAktør(Mockito.any(AktørId.class))).thenReturn(Optional.of(lagPersoninfo()));
        virksomhetTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        beregningsgrunnlagDtoUtil = new BeregningsgrunnlagDtoUtil(tpsTjenesteMock, resultatRepositoryProvider.getBeregningsgrunnlagRepository());
    }

    @Test
    public void arbeidsprosenter_for_uavsluttet_periode() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));

        // Act
        List<BigDecimal> arbeidsandeler = beregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1);
    }

    @Test
    public void arbeidsprosenter_for_uavsluttet_periode_og_uavsluttet_gradering() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, TIDENES_ENDE, arbeidsprosent1));

        // Act
        List<BigDecimal> arbeidsandeler = beregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, null));

        // Assert
        assertThat(arbeidsandeler).containsExactly(arbeidsprosent1);
    }

    @Test
    public void arbeidsprosenter_for_samanhengande_gradering_med_hull_på_slutten() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));

        // Act
        List<BigDecimal> arbeidsandeler = beregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(1)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4);
    }

    @Test
    public void arbeidsprosenter_for_ikkje_samanhengande_gradering() {
        // Arrange
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(20);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(30);
        BigDecimal arbeidsprosent3 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent4 = BigDecimal.valueOf(50);
        BigDecimal arbeidsprosent5 = BigDecimal.valueOf(60);
        List<Gradering> graderinger = new ArrayList<>();
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1), arbeidsprosent1));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(1).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2), arbeidsprosent2));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(2).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3), arbeidsprosent3));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(3).plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4), arbeidsprosent4));
        graderinger.add(new GraderingEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(4).plusDays(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5), arbeidsprosent5));

        // Act
        List<BigDecimal> arbeidsandeler = beregningsgrunnlagDtoUtil.finnArbeidsprosenterIPeriode(graderinger, ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(5)));

        // Assert
        assertThat(arbeidsandeler).containsExactlyInAnyOrder(BigDecimal.ZERO ,arbeidsprosent1, arbeidsprosent2, arbeidsprosent3, arbeidsprosent4, arbeidsprosent5);
    }

    @Test
    public void skal_returnere_empty_om_ingen_arbeidsgiver_eller_opptjeningaktivitet_på_andel() {
        long andelsnr = 1;
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medArbforholdType(null)
            .build(periode);
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = beregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel);
        assertThat(arbeidsforhold.isPresent()).isFalse();
    }

    @Test
    public void skal_returnere_arbeidsforholdDto_om_virksomhet_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
        String orgnr = "21367124714";
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomhetTestUtil.forArbeidsgiverVirksomhet(orgnr)))
            .build(periode);
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = beregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel);
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverId()).isEqualTo(orgnr);
    }

    @Test
    public void skal_returnere_arbeidsforholdDto_om_privatperson_som_arbeidsgiver_på_andel() {
        long andelsnr = 1;
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);

        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.person(new AktørId("3423215214"))))
            .build(periode);
        Optional<BeregningsgrunnlagArbeidsforholdDto> arbeidsforhold = beregningsgrunnlagDtoUtil.lagArbeidsforholdDto(andel);
        assertThat(arbeidsforhold.isPresent()).isTrue();
        assertThat(arbeidsforhold.get().getArbeidsgiverId()).isEqualTo(PRIVATPERSON_IDENT);
        assertThat(arbeidsforhold.get().getArbeidsgiverNavn()).isEqualTo(PRIVATPERSON_NAVN);
    }

    private Personinfo lagPersoninfo() {
        Personinfo.Builder b = new Personinfo.Builder()
            .medNavn(PRIVATPERSON_NAVN)
            .medPersonIdent(new PersonIdent(PRIVATPERSON_IDENT))
            .medAktørId(new AktørId("123123123123"))
            .medFødselsdato(LocalDate.now())
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE);
        return b.build();
    }

}
