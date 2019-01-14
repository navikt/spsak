package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

public class KopierBeregningsgrunnlagTest {

    private static final BigDecimal BESTEBEREGNING_PR_ÅR = BigDecimal.valueOf(0xBE57E);
    private static final BigDecimal OVERSTYRT_PR_ÅR = BigDecimal.valueOf(7331);
    private static final BigDecimal BEREGNET_PR_ÅR = BigDecimal.valueOf(1337);

    @Test
    public void kopierOverstyrteVerdier() {
        // Arrange
        Beregningsgrunnlag overstyrtBeregningsgrunnlag = lagBeregningsgrunnlag(true);
        Beregningsgrunnlag nyttBeregningsgrunnlag = lagBeregningsgrunnlag(false);

        // Act
        KopierBeregningsgrunnlag.kopierOverstyrteVerdier(overstyrtBeregningsgrunnlag, nyttBeregningsgrunnlag);

        // Assert
        BeregningsgrunnlagPrStatusOgAndel nyAndel = hentFørsteAndelIFørstePeriode(nyttBeregningsgrunnlag);
        verifiserAndel(nyAndel);
    }

    @Test
    public void kopierOverstyrteVerdierVedOvergangFraEnTilToPerioder() {
        ÅpenDatoIntervallEntitet periode1 = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), null);
        Beregningsgrunnlag overstyrtBeregningsgrunnlag = lagBeregningsgrunnlag(true, periode1);

        ÅpenDatoIntervallEntitet periode2a = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusDays(20));
        ÅpenDatoIntervallEntitet periode2b = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().plusDays(21), null);
        Beregningsgrunnlag nyttBeregningsgrunnlag = lagBeregningsgrunnlag(false, periode2a, periode2b);

        // Act
        KopierBeregningsgrunnlag.kopierOverstyrteVerdier(overstyrtBeregningsgrunnlag, nyttBeregningsgrunnlag);

        // Assert
        assertThat(nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(2);
        nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .forEach(this::verifiserAndel);
    }

    private void verifiserAndel(BeregningsgrunnlagPrStatusOgAndel nyAndel) {
        assertThat(nyAndel.getBeregnetPrÅr()).isEqualByComparingTo(BEREGNET_PR_ÅR);
        assertThat(nyAndel.getOverstyrtPrÅr()).isEqualByComparingTo(OVERSTYRT_PR_ÅR);
        assertThat(nyAndel.getBgAndelArbeidsforhold()).isPresent();
        assertThat(nyAndel.getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isTrue();
        assertThat(nyAndel.getNyIArbeidslivet()).isTrue();
        assertThat(nyAndel.getFastsattAvSaksbehandler()).isTrue();
        assertThat(nyAndel.getLagtTilAvSaksbehandler()).isTrue();
        assertThat(nyAndel.getBgAndelArbeidsforhold().get().erLønnsendringIBeregningsperioden()).isTrue();
        assertThat(nyAndel.getBesteberegningPrÅr()).isEqualByComparingTo(BESTEBEREGNING_PR_ÅR);
    }

    @Test
    public void kopierRelevanteFeltOgIkkeAndelsNrOmNyAndelHarBlittLagtTil() { //Faktisk bug fikset
        // Arrange
        VirksomhetEntitet virksomhetA = new VirksomhetEntitet.Builder().medOrgnr("456").medNavn("VirksomhetA").build();
        VirksomhetEntitet virksomhetB = new VirksomhetEntitet.Builder().medOrgnr("123").medNavn("VirksomhetB").build();

        Beregningsgrunnlag gammeltBeregningsgrunnlag = lagBeregningsgrunnlag(false);
        Beregningsgrunnlag nyttBeregningsgrunnlag = lagBeregningsgrunnlag(true);

        BeregningsgrunnlagPeriode gammelBgPeriode = gammeltBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel gammelBgAndel = gammelBgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPeriode nyBgPeriode = nyttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel nyBgAndel = nyBgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0);

        lagAndelerSomBytterAndelsNr(virksomhetA, virksomhetB, gammelBgAndel, nyBgPeriode, nyBgAndel);

        // Act
        KopierBeregningsgrunnlag.kopierOverstyrteVerdier(gammeltBeregningsgrunnlag, nyttBeregningsgrunnlag);

        // Assert
        assertThat(nyBgPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(2);
        BeregningsgrunnlagPrStatusOgAndel andelA = nyBgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).get().equals(virksomhetA)).findFirst().get();//NOSONAR
        BeregningsgrunnlagPrStatusOgAndel andelB = nyBgPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet).get().equals(virksomhetB)).findFirst().get();//NOSONAR

        assertThat(andelA.getAndelsnr()).isEqualTo(2L);
        assertThat(andelA.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(andelA.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(andelB.getAndelsnr()).isEqualTo(1L);
        assertThat(andelB.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        assertThat(andelB.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    private BeregningsgrunnlagPrStatusOgAndel hentFørsteAndelIFørstePeriode(Beregningsgrunnlag beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().iterator().next().getBeregningsgrunnlagPrStatusOgAndelList().iterator().next();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(boolean overstyrteVerdier, ÅpenDatoIntervallEntitet... perioder) {
        if (perioder.length == 0) {
            perioder = new ÅpenDatoIntervallEntitet[]{ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), null)};
        }

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(LocalDate.now())
            .build();
        Arrays.stream(perioder).forEach(periode -> lagBeregningsgrunnlagPeriode(overstyrteVerdier, beregningsgrunnlag));

        return beregningsgrunnlag;
    }

    private void lagBeregningsgrunnlagPeriode(boolean overstyrteVerdier, Beregningsgrunnlag beregningsgrunnlag) {
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now(), null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.Builder andelBuilder = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.FISKER);
        if (overstyrteVerdier) {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
                .builder()
                .medTidsbegrensetArbeidsforhold(true)
                .medLønnsendringIBeregningsperioden(true);

            andelBuilder.medBeregnetPrÅr(BEREGNET_PR_ÅR)
                .medBGAndelArbeidsforhold(bga)
                .medOverstyrtPrÅr(OVERSTYRT_PR_ÅR)
                .medNyIArbeidslivet(true)
                .medFastsattAvSaksbehandler(true)
                .medLagtTilAvSaksbehandler(true)
                .medAndelsnr(3L)
                .medBesteberegningPrÅr(BESTEBEREGNING_PR_ÅR);
        }
        andelBuilder.build(beregningsgrunnlagPeriode);
    }

    private void lagAndelerSomBytterAndelsNr(VirksomhetEntitet virksomhetA, VirksomhetEntitet virksomhetB, BeregningsgrunnlagPrStatusOgAndel gammelBgAndel, BeregningsgrunnlagPeriode nyBgPeriode, BeregningsgrunnlagPrStatusOgAndel nyBgAndel) {
        BeregningsgrunnlagPrStatusOgAndel.builder(gammelBgAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetA)).medArbforholdRef("a1"))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr(1L)
            .medBeregnetPrÅr(BigDecimal.valueOf(100));
        BeregningsgrunnlagPrStatusOgAndel.builder(nyBgAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetA)).medArbforholdRef("a1"))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelsnr(2L);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhetB)).medArbforholdRef("a1"))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAndelsnr(1L)
            .medBeregnetPrÅr(BigDecimal.TEN)
            .build(nyBgPeriode);
    }
}
