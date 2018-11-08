package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class UttakStillingsprosentTjenesteImplTest {

    @Test
    public void medOrgNrOgArbIdEnYrkesaktivitet() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();

        BigDecimal stillingsprosent = BigDecimal.valueOf(77);

        String orgnr = "123";
        String arbId = "arbId";
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        YrkesaktivitetBuilder yrkesAktivitet = arbeidAktivitet(orgnr, arbId, fom, tom, stillingsprosent);

        List<YrkesaktivitetBuilder> yrkesaktivitetBuilder = Collections.singletonList(yrkesAktivitet);
        final InntektArbeidYtelseGrunnlag grunnlag = opprettGrunnlag(yrkesaktivitetBuilder, behandling.getAktørId()).build();
        List<Yrkesaktivitet> yrkesaktiviteter = Collections.singletonList(yrkesAktivitet.build());
        UttakStillingsprosentTjenesteImpl tjeneste = tjeneste(behandling, grunnlag, yrkesaktiviteter);

        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), fom)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), fom).get()).isEqualTo(stillingsprosent);
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), tom)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), tom).get()).isEqualTo(stillingsprosent);
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), tom.plusDays(1))).isNotPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId), fom.minusDays(1))).isNotPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, null, arbId, fom)).isNotPresent();
    }

    @Test
    public void medToArbeidsforholdSammeArbeidsgiverSammePeriode() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();

        BigDecimal stillingsprosent1 = BigDecimal.valueOf(77);
        BigDecimal stillingsprosent2 = BigDecimal.valueOf(50);

        String orgnr = "123";
        String arbId1 = "arbId1";
        String arbId2 = "arbId2";
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        YrkesaktivitetBuilder yrkesAktivitet1 = arbeidAktivitet(orgnr, arbId1, fom, tom, stillingsprosent1);
        YrkesaktivitetBuilder yrkesAktivitet2 = arbeidAktivitet(orgnr, arbId2, fom, tom, stillingsprosent2);
        List<YrkesaktivitetBuilder> yrkesAktivitetBuilder = Arrays.asList(yrkesAktivitet1, yrkesAktivitet2);
        List<Yrkesaktivitet> yrkesaktiviteter = Arrays.asList(yrkesAktivitet1.build(), yrkesAktivitet2.build());

        final InntektArbeidYtelseGrunnlag grunnlag = opprettGrunnlag(yrkesAktivitetBuilder, behandling.getAktørId()).build();
        UttakStillingsprosentTjenesteImpl tjeneste = tjeneste(behandling, grunnlag, yrkesaktiviteter);

        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId1), fom)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId1), fom).get()).isEqualTo(stillingsprosent1);
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId2), fom)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId2), fom).get()).isEqualTo(stillingsprosent2);
    }

    @Test
    public void medToArbeidsforholdSammeArbeidsgiverUlikPeriode() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();

        BigDecimal stillingsprosent1 = BigDecimal.valueOf(77);
        BigDecimal stillingsprosent2 = BigDecimal.valueOf(50);

        String orgnr = "123";
        String arbId1 = "arbId1";
        String arbId2 = "arbId2";
        LocalDate fom1 = LocalDate.now();
        LocalDate tom1 = fom1.plusWeeks(2);
        LocalDate fom2 = tom1.plusDays(1);
        LocalDate tom2 = fom2.plusWeeks(2);
        YrkesaktivitetBuilder yrkesAktivitet1 = arbeidAktivitet(orgnr, arbId1, fom1, tom1, stillingsprosent1);
        YrkesaktivitetBuilder yrkesAktivitet2 = arbeidAktivitet(orgnr, arbId2, fom2, tom2, stillingsprosent2);
        List<YrkesaktivitetBuilder> yrkesaktivitetBuilder = Arrays.asList(yrkesAktivitet1, yrkesAktivitet2);
        List<Yrkesaktivitet> yrkesaktiviteter = Arrays.asList(yrkesAktivitet1.build(), yrkesAktivitet2.build());

        final InntektArbeidYtelseGrunnlag grunnlag = opprettGrunnlag(yrkesaktivitetBuilder, behandling.getAktørId()).build();
        UttakStillingsprosentTjenesteImpl tjeneste = tjeneste(behandling, grunnlag, yrkesaktiviteter);

        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId1), fom1)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId1), fom1).get()).isEqualTo(stillingsprosent1);
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId2), fom2)).isPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId2), fom2).get()).isEqualTo(stillingsprosent2);
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId1), fom2)).isNotPresent();
        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, finnArbeidsforholdIdFor(grunnlag, orgnr, arbId2), fom1)).isNotPresent();
    }

    private String finnArbeidsforholdIdFor(InntektArbeidYtelseGrunnlag grunnlag, String orgnr, String ref) {
        try {
            Field m = InntektArbeidYtelseGrunnlagEntitet.class.getDeclaredField("informasjon");
            m.setAccessible(true);
            final ArbeidsforholdInformasjon invoke = (ArbeidsforholdInformasjon) m.get(grunnlag);
            return invoke.finnForEkstern(Arbeidsgiver.virksomhet(virksomhet(orgnr)), ArbeidsforholdRef.ref(ref)).getReferanse();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void skalMatcheSelvOmArbeidsgiverErNullMenYtelseHarArbeidsgiverRef() {
        Behandling behandling = ScenarioMorSøkerForeldrepenger.forFødsel().lagMocked();

        BigDecimal stillingsprosent = BigDecimal.valueOf(77);

        String orgnr = "123";
        String arbId = "arbId";
        LocalDate fom = LocalDate.now();
        LocalDate tom = fom.plusWeeks(2);
        YrkesaktivitetBuilder yrkesAktivitet = arbeidAktivitet(orgnr, arbId, fom, tom, stillingsprosent);

        List<YrkesaktivitetBuilder> yrkesAktivitetBuilder = Collections.singletonList(yrkesAktivitet);
        final InntektArbeidYtelseGrunnlag grunnlag = opprettGrunnlag(yrkesAktivitetBuilder, behandling.getAktørId()).build();
        List<Yrkesaktivitet> yrkesaktiviteter = Collections.singletonList(yrkesAktivitet.build());
        UttakStillingsprosentTjenesteImpl tjeneste = tjeneste(behandling, grunnlag, yrkesaktiviteter);

        assertThat(tjeneste.finnStillingsprosentOrdinærtArbeid(behandling, orgnr, null, fom)).isPresent();
    }

    private YrkesaktivitetBuilder arbeidAktivitet(String orgnr, String arbId, LocalDate fom, LocalDate tom, BigDecimal stillingsprosent) {

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetAvtale = YrkesaktivitetEntitet.AktivitetsAvtaleBuilder.ny()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
            .medProsentsats(stillingsprosent);

        return YrkesaktivitetBuilder.oppdatere(Optional.empty())
            .leggTilAktivitetsAvtale(aktivitetAvtale)
            .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet(orgnr)))
            .medArbeidsforholdId(ArbeidsforholdRef.ref(arbId));
    }

    private Virksomhet virksomhet(String orgnr) {
        return new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .build();
    }

    private UttakStillingsprosentTjenesteImpl tjeneste(Behandling behandling, InntektArbeidYtelseGrunnlag build, List<Yrkesaktivitet> yrkesAktiviteter) {
        UttakArbeidTjeneste uttakArbeidTjeneste = new UttakArbeidTjenesteImpl(inntektArbeidYtelseRepository(behandling, build),
            uttakBeregningsandelTjeneste(behandling, yrkesAktiviteter));
        return new UttakStillingsprosentTjenesteImpl(uttakArbeidTjeneste);
    }

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseRepository(Behandling behandling, InntektArbeidYtelseGrunnlag build) {

        InntektArbeidYtelseTjeneste mock = mock(InntektArbeidYtelseTjeneste.class);
        when(mock.hentAggregat(behandling)).thenReturn(build);
        return mock;
    }

    private UttakBeregningsandelTjeneste uttakBeregningsandelTjeneste(Behandling behandling, List<Yrkesaktivitet> yrkesAktiviteter) {
        UttakBeregningsandelTjeneste mock = mock(UttakBeregningsandelTjeneste.class);
        when(mock.hentAndeler(behandling)).thenReturn(lagAndelerForAlleInntekter(yrkesAktiviteter));
        return mock;
    }

    private List<BeregningsgrunnlagPrStatusOgAndel> lagAndelerForAlleInntekter(List<Yrkesaktivitet> yrkesAktiviteter) {
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = new ArrayList<>();
        for (Yrkesaktivitet yrkesaktivitet : yrkesAktiviteter) {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
                .builder()
                .medArbeidsgiver(yrkesaktivitet.getArbeidsgiver())
                .medArbforholdRef(yrkesaktivitet.getArbeidsforholdRef().isPresent() ? yrkesaktivitet.getArbeidsforholdRef().get().getReferanse() : null);

            andeler.add(new BeregningsgrunnlagPrStatusOgAndel.Builder()
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBGAndelArbeidsforhold(bga)
                .build(new BeregningsgrunnlagPeriode()));
        }
        return andeler;
    }

    private InntektArbeidYtelseGrunnlagBuilder opprettGrunnlag(List<YrkesaktivitetBuilder> yrkesaktivitetList, AktørId aktørId) {
        InntektArbeidYtelseAggregatBuilder aggregat = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER
        );
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = aggregat.getAktørArbeidBuilder(aktørId);
        for (YrkesaktivitetBuilder yrkesaktivitet : yrkesaktivitetList) {
            aktørArbeidBuilder.leggTilYrkesaktivitet(yrkesaktivitet);
        }

        aggregat.leggTilAktørArbeid(aktørArbeidBuilder);

        InntektArbeidYtelseGrunnlagBuilder inntektArbeidYtelseGrunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(Optional.empty());
        inntektArbeidYtelseGrunnlagBuilder.medData(aggregat);
        return inntektArbeidYtelseGrunnlagBuilder;
    }

}
