package no.nav.foreldrepenger.beregningsgrunnlag.ytelse;


import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.TilstøtendeYtelseAndel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;

public class OpprettAndelerBasertPåTilstøtendeYtelseTest {

    private OpprettAndelerBasertPåTilstøtendeYtelse opprettAndelerBasertPåTilstøtendeYtelse = new OpprettAndelerBasertPåTilstøtendeYtelse();

    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_atfl_med_definert_virksomhet() {
        // Arrange
        String orgnr = "23984293";
        String arbId = "42348293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr, arbId));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medOrgNr(orgnr)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
        assertThat(andel.get().getOrgnr()).isEqualTo(orgnr);
        assertThat(andel.get().getArbeidsforholdId()).isEqualTo(arbId);
    }

    @Test
    public void skal_ikkje_finne_arbeidsforhold_fra_beregningsgrunnlag_for_atfl_med_definert_virksomhet() {
        // Arrange
        String orgnr = "23984293";
        String arbId = "42348293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr, arbId));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medOrgNr(null)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isFalse();
    }

    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_SN_med_definert_virksomhet() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medInntektskategori(Inntektskategori.FISKER)
            .medOrgNr(orgnr)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
        assertThat(andel.get().getOrgnr()).isEqualTo(orgnr);
        assertThat(andel.get().getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_SN_med_udefinert_virksomhet_lik_inntektskategori() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medInntektskategori(Inntektskategori.FISKER)
            .medOrgNr(null)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
        assertThat(andel.get().getOrgnr()).isEqualTo(orgnr);
        assertThat(andel.get().getInntektskategori()).isEqualTo(Inntektskategori.FISKER);
    }

    @Test
    public void skal_ikke_finne_arbeidsforhold_fra_beregningsgrunnlag_for_SN_med_definert_virksomhet_ulik_inntektskategori() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.JORDBRUKER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.SN)
            .medInntektskategori(Inntektskategori.FISKER)
            .medOrgNr(orgnr)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isFalse();
    }

    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_ATFL_med_definert_virksomhet_ulik_inntektskategori() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.UDEFINERT)
            .medOrgNr(orgnr)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
    }

    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_FL_med_udefinert_virksomhet() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medOrgNr(null)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
        assertThat(andel.get().getOrgnr()).isEqualTo(orgnr);
        assertThat(andel.get().getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
    }


    @Test
    public void skal_finne_arbeidsforhold_fra_beregningsgrunnlag_for_FL_med_definert_virksomhet() {
        // Arrange
        String orgnr = "23984293";
        String orgnr2 = "23324984293";
        String arbId2 = "42348523523293";
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, orgnr, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, orgnr2, arbId2));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        TilstøtendeYtelse.Builder tyBuilder = TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER);
        TilstøtendeYtelseAndel tyAndel = TilstøtendeYtelseAndel.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.FRILANSER)
            .medOrgNr(orgnr)
            .build();
        tyBuilder.leggTilArbeidsforhold(tyAndel);
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        Optional<BeregningsgrunnlagAndelTilstøtendeYtelse> andel = opprettAndelerBasertPåTilstøtendeYtelse.finnAndelHvisEksisterer(tyGrunnlagBuilder.build(), tyAndel);

        // Assert
        assertThat(andel.isPresent()).isTrue();
    }

    private BeregningsgrunnlagAndelTilstøtendeYtelse lagAndel(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori, String orgnr, String arbeidsforholdId) {
        BeregningsgrunnlagAndelTilstøtendeYtelse.Builder builder = BeregningsgrunnlagAndelTilstøtendeYtelse.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medOrgnr(orgnr)
            .medArbeidsforholdId(arbeidsforholdId);

        return builder.build();
    }

}
