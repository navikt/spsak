package no.nav.foreldrepenger.behandlingsprosess.komponenttest.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

class KomponenttestBeregningAssertUtil {


    static void assertBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag,
                                         LocalDate skjæringstidspunktForBeregning,
                                         List<AktivitetStatus> aktivitetStatuser) {
        assertThat(beregningsgrunnlag.getSkjæringstidspunkt()).isEqualTo(skjæringstidspunktForBeregning);
        assertThat(beregningsgrunnlag.getGrunnbeløp() != null).isTrue();
        assertThat(beregningsgrunnlag.getAktivitetStatuser()).hasSize(aktivitetStatuser.size());
        for (int i = 0; i < aktivitetStatuser.size(); i++) {
            assertThat(beregningsgrunnlag.getAktivitetStatuser().get(i).getAktivitetStatus()).isEqualTo(aktivitetStatuser.get(i));
        }
    }

    static void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                                ÅpenDatoIntervallEntitet periode,
                                                BigDecimal beregnetPrÅr, Long dagsats, BigDecimal overstyrtPrÅr, BigDecimal refusjonskravPrÅr) {
        assertThat(beregningsgrunnlagPeriode.getDagsats()).isEqualTo(dagsats);
        if (overstyrtPrÅr != null) {
            assertThat(beregningsgrunnlagPeriode.getBruttoPrÅr().compareTo(overstyrtPrÅr)).isEqualTo(0);
        }
        if (refusjonskravPrÅr == null){
            assertThat(beregningsgrunnlagPeriode.getTotaltRefusjonkravIPeriode().erNulltall()).isTrue();

        } else {
            assertThat(beregningsgrunnlagPeriode.getTotaltRefusjonkravIPeriode().getVerdi().compareTo(refusjonskravPrÅr)).isEqualTo(0);
        }
        assertThat(beregningsgrunnlagPeriode.getBeregnetPrÅr().compareTo(beregnetPrÅr)).isEqualTo(0);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(periode.getFomDato());
        if (periode.getTomDato() == null) {
            assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).isNull();
        } else {
            assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(periode.getTomDato());
        }
    }

    static void assertBeregningsgrunnlagAndel(BeregningsgrunnlagPrStatusOgAndel andel,
                                              BigDecimal beregnetPrÅr, AktivitetStatus aktivitetStatus,
                                              Inntektskategori inntektskategori,
                                              LocalDate beregningsperiodFom,
                                              LocalDate beregningsperiodeTom, BigDecimal refusjonskravPrÅr, BigDecimal overstyrtPrÅr) {

        assertThat(andel.getOverstyrtPrÅr()).isEqualTo(overstyrtPrÅr);
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualTo(refusjonskravPrÅr);
        if (beregnetPrÅr == null) {
            assertThat(andel.getBeregnetPrÅr()).isNull();
        } else {
            assertThat(andel.getBeregnetPrÅr().compareTo(beregnetPrÅr)).isEqualTo(0);
        }
        assertThat(andel.getBeregningsperiodeFom()).isEqualTo(beregningsperiodFom);
        assertThat(andel.getBeregningsperiodeTom()).isEqualTo(beregningsperiodeTom);
        assertThat(andel.getAktivitetStatus()).isEqualTo(aktivitetStatus);
        assertThat(andel.getInntektskategori()).isEqualTo(inntektskategori);

    }

    static void assertSammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag,
                                             BigDecimal rapportertInntekt,
                                             Long avvik){

        assertThat(sammenligningsgrunnlag.getRapportertPrÅr()).isEqualTo(rapportertInntekt);
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(avvik);

    }
}
