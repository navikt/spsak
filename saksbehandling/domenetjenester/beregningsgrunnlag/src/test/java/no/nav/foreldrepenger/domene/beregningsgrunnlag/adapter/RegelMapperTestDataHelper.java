package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GRUNNBELØP_2018;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2013;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2014;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2015;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2016;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest.GSNITT_2018;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.Grunnbeløp;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;

public class RegelMapperTestDataHelper {
    public static final LocalDate NOW = LocalDate.now();
    public static final LocalDate MINUS_DAYS_5 = LocalDate.now().minusDays(5);
    public static final LocalDate MINUS_DAYS_10 = LocalDate.now().minusDays(10);
    public static final LocalDate MINUS_DAYS_20 = LocalDate.now().minusDays(20);
    public static final LocalDate MINUS_YEARS_1 = LocalDate.now().minusYears(1);
    public static final LocalDate MINUS_YEARS_2 = LocalDate.now().minusYears(2);
    public static final LocalDate MINUS_YEARS_3 = LocalDate.now().minusYears(3);

    public static final List<Grunnbeløp> GRUNNBELØPLISTE = Collections.unmodifiableList(Arrays.asList(
        new Grunnbeløp(LocalDate.of(2013, 05, 01), LocalDate.of(2014, 04, 30), 85245L, GSNITT_2013),
        new Grunnbeløp(LocalDate.of(2014, 05, 01), LocalDate.of(2015, 04, 30), 88370L, GSNITT_2014),
        new Grunnbeløp(LocalDate.of(2015, 05, 01), LocalDate.of(2016, 04, 30), 90068L, GSNITT_2015),
        new Grunnbeløp(LocalDate.of(2016, 05, 01), LocalDate.of(2017, 04, 30), 92576L, GSNITT_2016),
        new Grunnbeløp(LocalDate.of(2017, 05, 01), LocalDate.of(2018, 04, 30), GRUNNBELØP_2017, GSNITT_2017),
        new Grunnbeløp(LocalDate.of(2018, 05, 01), LocalDate.MAX, GRUNNBELØP_2018, GSNITT_2018)));

    public static Beregningsgrunnlag buildVLBeregningsgrunnlag() {
        return Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(MINUS_DAYS_5)
            .medOpprinneligSkjæringstidspunkt(MINUS_DAYS_5)
            .medGrunnbeløp(BigDecimal.ZERO)
            .medRedusertGrunnbeløp(BigDecimal.ZERO)
            .medDekningsgrad(100L)
            .build();
    }

    public static void buildVLBGAktivitetStatus(Beregningsgrunnlag beregningsgrunnlag) {
        buildVLBGAktivitetStatus(beregningsgrunnlag, AktivitetStatus.ARBEIDSTAKER);
    }

    public static void buildVLBGAktivitetStatus(Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus aktivitetStatus) {
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medHjemmel(Hjemmel.F_14_7_8_30)
            .build(beregningsgrunnlag);
    }

    public static void buildVLSammenligningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        Sammenligningsgrunnlag.builder()
            .medRapportertPrÅr(BigDecimal.valueOf(1098318.12))
            .medSammenligningsperiode(MINUS_YEARS_1, NOW)
            .medAvvikPromille(220L)
            .build(beregningsgrunnlag);
    }

    public static BeregningsgrunnlagPeriode buildVLBGPeriode(Beregningsgrunnlag beregningsgrunnlag) {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(beregningsgrunnlag.getSkjæringstidspunkt(), beregningsgrunnlag.getSkjæringstidspunkt().plusYears(3))
            .medBruttoPrÅr(BigDecimal.valueOf(534343.55))
            .medAvkortetPrÅr(BigDecimal.valueOf(223421.334))
            .medRedusertPrÅr(BigDecimal.valueOf(23412.32))
            .build(beregningsgrunnlag);
    }

    public static void buildVLBGPStatusForSN(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        buildVLBGPStatus(beregningsgrunnlagPeriode, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, MINUS_DAYS_10, MINUS_DAYS_5);
    }

    public static void buildVLBGPStatus(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                        AktivitetStatus aktivitetStatus,
                                        Inntektskategori inntektskategori, LocalDate fom, LocalDate tom) {
        buildVLBGPStatus(beregningsgrunnlagPeriode, aktivitetStatus, inntektskategori, fom, tom,
            null, AktivitetStatus.FRILANSER.equals(aktivitetStatus) ? OpptjeningAktivitetType.FRILANS : null);
    }

    public static void buildVLBGPStatus(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                        AktivitetStatus aktivitetStatus,
                                        Inntektskategori inntektskategori, LocalDate fom, LocalDate tom,
                                        Arbeidsgiver arbeidsgiver,
                                        OpptjeningAktivitetType arbforholdType) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus) ? arbeidsgiver : null)
            .medRefusjonskravPrÅr(AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus) ? BigDecimal.valueOf(42.00) : null)
            .medNaturalytelseBortfaltPrÅr(AktivitetStatus.ARBEIDSTAKER.equals(aktivitetStatus) ? BigDecimal.valueOf(3232.32) : null)
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbforholdRef(null);// TODO (TOPAS) legg til i test
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medBeregningsperiode(fom, tom)
            .medArbforholdType(arbforholdType)
            .medYtelse(RelatertYtelseType.FORELDREPENGER)
            .medBeregnetPrÅr(BigDecimal.valueOf(1000.01))
            .medOverstyrtPrÅr(BigDecimal.valueOf(4444432.32))
            .medAvkortetPrÅr(BigDecimal.valueOf(12.12))
            .medRedusertPrÅr(BigDecimal.valueOf(34.34))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(52000.0))
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(26000.0))
            .build(beregningsgrunnlagPeriode);
    }

    public static SammenligningsGrunnlag buildRegelSammenligningsG() {
        return SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(new Periode(MINUS_YEARS_1, MINUS_DAYS_20))
            .medRapportertPrÅr(BigDecimal.valueOf(42))
            .medAvvikProsent(BigDecimal.ZERO)
            .build();
    }

    public static BeregningsgrunnlagPrStatus buildRegelBGPeriode(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode regelBGP, no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus status, Periode periode) {
        if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.erArbeidstakerEllerFrilanser(status)) {
            final BeregningsgrunnlagPrStatus regelBGPStatus = BeregningsgrunnlagPrStatus.builder(regelBGP.getBeregningsgrunnlagPrStatus(status))
                .medBeregningsperiode(periode)
                .build();
            return regelBGPStatus;
        } else {
            final BeregningsgrunnlagPrStatus regelBGPStatus = BeregningsgrunnlagPrStatus.builder(regelBGP.getBeregningsgrunnlagPrStatus(status))
                .medBeregnetPrÅr(BigDecimal.valueOf(400000.42))
                .medBruttoPrÅr(BigDecimal.valueOf(111.11))
                .medAvkortetPrÅr(BigDecimal.valueOf(789.789))
                .medRedusertPrÅr(BigDecimal.valueOf(901.901))
                .medBeregningsperiode(periode)
                .build();
            return regelBGPStatus;
        }
    }

    public static no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag buildRegelBeregningsgrunnlag(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus,
                                                                                                                                no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori inntektskategori,
                                                                                                                                BeregningsgrunnlagHjemmel hjemmel) {
        no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode periode = buildRegelBGPeriode(aktivitetStatus, inntektskategori);
        return no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag.builder()
            .medInntektsgrunnlag(new Inntektsgrunnlag())
            .medSkjæringstidspunkt(NOW)
            .medAktivitetStatuser(singletonList(new AktivitetStatusMedHjemmel(aktivitetStatus, hjemmel)))
            .medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløpSatser(GRUNNBELØPLISTE)
            .build();
    }

    private static no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode buildRegelBGPeriode(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori inntektskategori) {
        no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode.Builder periodeBuilder =
            no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(NOW, null));
        long andelNr = 1;
        if (no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.erKombinasjonMedSelvstendig(aktivitetStatus)) {
            BeregningsgrunnlagPrStatus prStatusATFL = buildPrStatus(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.ATFL, inntektskategori, null);
            BeregningsgrunnlagPrStatus prStatusSN = buildPrStatus(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.SN, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, andelNr);
            return periodeBuilder
                .medBeregningsgrunnlagPrStatus(prStatusATFL)
                .medBeregningsgrunnlagPrStatus(prStatusSN)
                .build();
        }
        return periodeBuilder
            .medBeregningsgrunnlagPrStatus(buildPrStatus(aktivitetStatus, inntektskategori, andelNr))
            .build();
    }

    private static BeregningsgrunnlagPrStatus buildPrStatus(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus aktivitetStatus, no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori inntektskategori, Long andelNr) {
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medAndelNr(no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus.erArbeidstaker(aktivitetStatus) ? null : andelNr)
            .build();
    }
}
