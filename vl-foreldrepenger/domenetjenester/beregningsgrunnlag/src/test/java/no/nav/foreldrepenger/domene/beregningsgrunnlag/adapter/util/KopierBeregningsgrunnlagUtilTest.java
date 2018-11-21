package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.KopierBeregningsgrunnlagUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;

public class KopierBeregningsgrunnlagUtilTest {

    private static final Periode periode = Periode.of(LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(1));

    @Test
    public void skalKopiereBeregninggrunnlagPeriode() {
        // Arrange
        BeregningsgrunnlagPeriode orginal = lagPeriode();
        BeregningsgrunnlagPeriode kopi = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(periode.getTom().plusDays(1), null))
            .leggTilPeriodeÅrsak(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET)
            .build();
        // Act
        KopierBeregningsgrunnlagUtil.kopierBeregningsgrunnlagPeriode(orginal, kopi);
        //Assert
        assertBeregningsgrunnlagPerioderErLike(kopi);
    }


    private void assertBeregningsgrunnlagPerioderErLike(BeregningsgrunnlagPeriode kopi) {
        assertThat(kopi.getBeregningsgrunnlagPrStatus()).hasSize(2);
        assertThat(kopi.getBeregningsgrunnlagPeriode()).isEqualTo(Periode.of(periode.getTom().plusDays(1), null));
        assertThat(kopi.getPeriodeÅrsaker().get(0)).isEqualTo(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        assertATFLAndel(kopi.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL));
        assertSNAndel(kopi.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN));
    }

    private void assertSNAndel(BeregningsgrunnlagPrStatus kopi) {
        assertThat(kopi.getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
        assertThat(kopi.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(kopi.getNyIArbeidslivet()).isFalse();
        assertThat(kopi.getAndelNr()).isEqualTo(2L);
        assertThat(kopi.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(kopi.getArbeidsforhold()).isEmpty();
    }

    private void assertATFLAndel(BeregningsgrunnlagPrStatus kopi) {
        assertThat(kopi.getArbeidsforhold()).hasSize(1);
        assertThat(kopi.getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold af = kopi.getArbeidsforhold().get(0);

        assertThat(af.getArbeidsforhold()).isEqualTo(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345"));
        assertThat(af.getAndelNr()).isEqualTo(1L);
        assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(af.getNaturalytelseBortfaltPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(af.getNaturalytelseTilkommetPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(af.getMaksimalRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(af.getAvkortetRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(af.getRedusertRefusjonPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(4));
        assertThat(af.getAvkortetBrukersAndelPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(af.getRedusertBrukersAndelPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(6));
        assertThat(af.getTidsbegrensetArbeidsforhold()).isTrue();
        assertThat(af.getFastsattAvSaksbehandler()).isTrue();
    }

    private BeregningsgrunnlagPeriode lagPeriode() {
        BeregningsgrunnlagPeriode bgPeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(periode)
            .build();

        lagATFLStatus(bgPeriode);
        lagSNStatus(bgPeriode);

        return bgPeriode;
    }

    private void lagATFLStatus(BeregningsgrunnlagPeriode bgPeriode) {
        BeregningsgrunnlagPrArbeidsforhold bgArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345"))
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAndelNr(1L)
            .medBeregnetPrÅr(BigDecimal.valueOf(1))
            .medNaturalytelseBortfaltPrÅr(BigDecimal.ZERO)
            .medNaturalytelseTilkommetPrÅr(BigDecimal.TEN)
            .medMaksimalRefusjonPrÅr(BigDecimal.valueOf(2))
            .medAvkortetRefusjonPrÅr(BigDecimal.valueOf(3))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(4))
            .medAvkortetBrukersAndelPrÅr(BigDecimal.valueOf(5))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(6))
            .medErTidsbegrensetArbeidsforhold(true)
            .medFastsattAvSaksbehandler(true)
            .build();

        BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medArbeidsforhold(bgArbeidsforhold)
            .build();
    }

    private void lagSNStatus(BeregningsgrunnlagPeriode bgPeriode) {
        BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medBeregningsgrunnlagPeriode(bgPeriode)
            .medBeregnetPrÅr(BigDecimal.valueOf(100000))
            .medErNyIArbeidslivet(false)
            .medAndelNr(2L)
            .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build();
    }


}
