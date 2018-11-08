package no.nav.foreldrepenger.domene.uttak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.UttakBeregningsandelTjenesteImpl;

public class UttakArbeidTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    @Test
    public void skalBareHenteInntektsmeldingerSomHarArbeidsforholdIAndeler() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Virksomhet virksomhet = virksomhet("orgnr");
        ArbeidsforholdRef arbeidsforholdRef = ArbeidsforholdRef.ref("id");
        setupBeregning(scenario, virksomhet, arbeidsforholdRef);

        Behandling behandling = scenario.lagre(repositoryProvider);
        UttakBeregningsandelTjeneste beregningsandelTjeneste = beregningTjeneste();
        Inntektsmelding im1 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet)
            .medArbeidsforholdId(arbeidsforholdRef.getReferanse())
            .build();
        Inntektsmelding im2 = getInntektsmeldingBuilder()
            .medVirksomhet(virksomhet("orgnr2"))
            .build();
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = iayTjenesteMedInntektsmeldinger(behandling, Arrays.asList(im1, im2));

        UttakArbeidTjenesteImpl tjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);

        List<Inntektsmelding> inntektsmeldinger = tjeneste.hentInntektsmeldinger(behandling);

        assertThat(inntektsmeldinger).hasSize(1);
        assertThat(inntektsmeldinger.get(0).getVirksomhet()).isEqualTo(virksomhet);
        assertThat(inntektsmeldinger.get(0).getArbeidsforholdRef()).isEqualTo(arbeidsforholdRef);
    }

    private InntektsmeldingBuilder getInntektsmeldingBuilder() {
        return InntektsmeldingBuilder.builder().medInnsendingstidspunkt(LocalDateTime.now());
    }

    @Test
    public void testeAktivitetStatus() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Virksomhet virksomhet = virksomhet("orgnr");
        ArbeidsforholdRef arbeidsforholdRef = ArbeidsforholdRef.ref("id");
        setupBeregning(scenario, virksomhet, arbeidsforholdRef);

        Behandling behandling = scenario.lagre(repositoryProvider);
        UttakBeregningsandelTjeneste beregningsandelTjeneste = beregningTjeneste();

        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = iayTjenesteMedInntektsmeldinger(behandling, Collections.emptyList());

        UttakArbeidTjenesteImpl tjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseTjeneste, beregningsandelTjeneste);

        boolean erArbeidstaker = tjeneste.erArbeidstaker(behandling);

        assertThat(erArbeidstaker).isTrue();
    }


    private Virksomhet virksomhet(String orgnr) {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        return virksomhet;
    }

    private UttakBeregningsandelTjenesteImpl beregningTjeneste() {
        return new UttakBeregningsandelTjenesteImpl(repositoryProvider.getBeregningsgrunnlagRepository());
    }

    private InntektArbeidYtelseTjeneste iayTjenesteMedInntektsmeldinger(Behandling behandling, List<Inntektsmelding> inntektsmeldinger) {
        InntektArbeidYtelseTjeneste mock = mock(InntektArbeidYtelseTjeneste.class);
        when(mock.hentAlleInntektsmeldinger(behandling)).thenReturn(inntektsmeldinger);
        return mock;
    }

    private void setupBeregning(ScenarioMorSøkerForeldrepenger scenario, Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        Beregningsgrunnlag.Builder builder = scenario.medBeregningsgrunnlag()
            .medDekningsgrad(100L)
            .medGrunnbeløp(BigDecimal.TEN)
            .medRedusertGrunnbeløp(BigDecimal.ZERO)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now())
            .medSkjæringstidspunkt(LocalDate.now());
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbforholdRef(arbeidsforholdRef.getReferanse());
        BeregningsgrunnlagPrStatusOgAndel.Builder andel = new BeregningsgrunnlagPrStatusOgAndel.Builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPeriode.Builder periode = new BeregningsgrunnlagPeriode.Builder()
            .leggTilBeregningsgrunnlagPrStatusOgAndel(andel)
            .medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now());
        builder.leggTilBeregningsgrunnlagPeriode(periode);
    }
}
