package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaKortvarigArbeidsforholdTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018,9,30);
    
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    @Inject
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;

    @Inject
    private BeregningIAYTestUtil iayTestUtil;

    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private Behandling behandling;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(AKTØR_ID));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6Mnd() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018,8,5),
            LocalDate.of(2019,2,4), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato1() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018,8,29),
            LocalDate.of(2019,2,28), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato2() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018,8,31),
            LocalDate.of(2019,2,28), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato3() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, LocalDate.of(2018,9,1),
            LocalDate.of(2019,2,28), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdPå6MndIMånederMedUlikDato4() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, LocalDate.of(2018,9,30), LocalDate.of(2018,8,30),
            LocalDate.of(2019,2,28), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdSomStarterPåSkjæringstidspunktet() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }

    @Test
    public void skalIkkjeGiKortvarigForArbeidsforholdSomStarterEtterSkjæringstidspunktet() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(1), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.isEmpty()).isTrue();
    }


    @Test
    public void skalGiKortvarigForArbeidsforholdPå6MndMinusEinDag() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        List<Yrkesaktivitet> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter.size()).isEqualTo(1);
    }

    @Test
    public void skalGiKortvarigForArbeidsforholdSomStarterDagenFørSkjæringstidspunktet() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);
        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        List<Yrkesaktivitet> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter.size()).isEqualTo(1);
    }

    @Test
    public void skalGiKortvarigVedKombinasjonMedDagpenger() {
        // Arrange
        String arbId = "123";
        String orgnr = "123456780";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, AktivitetStatus.DAGPENGER);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        assertThat(kortvarige.values().size()).isEqualTo(1);
    }


    @Test
    public void skalGiToKortvarigeArbeidsforhold() {
        // Arrange
        String arbId1 = "123";
        String arbId2 = "231";
        String orgnr1 = "123456780";
        String orgnr2 = "123456644";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(5).minusDays(10), arbId1, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr1));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(2), arbId2, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Act
        Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> kortvarige = kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(behandling);

        // Assert
        List<Yrkesaktivitet> kortvarigeYrkesaktiviteter = new ArrayList<>(kortvarige.values());
        assertThat(kortvarigeYrkesaktiviteter.size()).isEqualTo(2);
    }

}
