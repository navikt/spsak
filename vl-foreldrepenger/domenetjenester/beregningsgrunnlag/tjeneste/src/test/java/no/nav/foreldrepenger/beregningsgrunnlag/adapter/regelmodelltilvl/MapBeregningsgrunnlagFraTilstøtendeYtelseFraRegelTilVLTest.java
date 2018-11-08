package no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettInntektskategoriFraSøknadTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.MatchBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVLTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Inject
    private MapBeregningsgrunnlagFraTilstøtendeYtelseFraRegelTilVL mapper;

    @Test
    public void skalFjerneSNAndelDagmammaOgLeggeTilFiskerAndelFraTilstøtendeYtelse() {
        // Arrange
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now().plusMonths(10))
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.DAGMAMMA)
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FRILANSER)
            .build(periode);
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.DAGMAMMA, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        mapper.mapAndeler(tyGrunnlagBuilder.build(), periode);

        // Assert
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.DAGMAMMA.equals(andel.getInntektskategori())).count()).isEqualTo(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER.equals(andel.getInntektskategori())).count()).isEqualTo(1);
    }

    @Test
    public void skalOppdatereFiskerandelMedInfoFraTilstøtendeYtelse() {
        // Arrange
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now().plusMonths(10))
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER)
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FRILANSER)
            .build(periode);
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, BigDecimal.valueOf(100)));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        mapper.mapAndeler(tyGrunnlagBuilder.build(), periode);

        // Assert
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER.equals(andel.getInntektskategori())).count()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel fiskerAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER.equals(andel.getInntektskategori())).findFirst().get();
        assertThat(fiskerAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi())
            .isEqualByComparingTo(BigDecimal.valueOf(100));

    }

    @Test
    public void skalIkkjeBrukeInfoOmFordelingForNokonAndelerOmEinSNAndelFraTYBlirFiltrertUt() {
        // Arrange
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now().plusMonths(10))
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER)
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus.FRILANSER)
            .medInntektskategori(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FRILANSER)
            .build(periode);
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, null));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.DAGMAMMA, BigDecimal.TEN, true));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.valueOf(2321312), true));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        mapper.mapAndeler(tyGrunnlagBuilder.build(), periode);

        // Assert
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.DAGMAMMA.equals(andel.getInntektskategori())).count()).isEqualTo(0);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER.equals(andel.getInntektskategori())).count()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel fiskerAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER.equals(andel.getInntektskategori())).findFirst().get();
        assertThat(fiskerAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER.equals(andel.getInntektskategori())).count()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel atflAndel = periode.getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.ARBEIDSTAKER.equals(andel.getInntektskategori())).findFirst().get();
        assertThat(atflAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skal_bruke_informasjon_om_fordeling_om_det_ikkje_finnes_SN_andeler() {
        // Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, BigDecimal.TEN));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        boolean skalBrukeInformasjonOmFordelingVedTY = mapper.skalBrukeInformasjonOmFordelingVedTY(tyGrunnlagBuilder.build(), Optional.empty());

        // Assert
        assertThat(skalBrukeInformasjonOmFordelingVedTY).isTrue();
    }

    @Test
    public void skal_bruke_informasjon_om_fordeling_om_SN_andel_fra_tilstøtende_ytelse_er_prioritert() {
        // Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, BigDecimal.TEN, true));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        boolean skalBrukeInformasjonOmFordelingVedTY = mapper.skalBrukeInformasjonOmFordelingVedTY(tyGrunnlagBuilder.build(), Optional.of(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER));

        // Assert
        assertThat(skalBrukeInformasjonOmFordelingVedTY).isTrue();
    }

    @Test
    public void skal_ikkje_bruke_informasjon_om_fordeling_om_SN_andel_fra_tilstøtende_ytelse_ikkje_er_prioritert() {
        // Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, BigDecimal.TEN, false));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.DAGMAMMA, BigDecimal.TEN, true));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        boolean skalBrukeInformasjonOmFordelingVedTY = mapper.skalBrukeInformasjonOmFordelingVedTY(tyGrunnlagBuilder.build(), Optional.of(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER));

        // Assert
        assertThat(skalBrukeInformasjonOmFordelingVedTY).isFalse();
    }

    @Test
    public void skal_bruke_informasjon_om_fordeling_om_ingen_SN_andeler_er_fra_TY() {
        // Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse.Builder tyGrunnlagBuilder = BeregningsgrunnlagFraTilstøtendeYtelse.builder();
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.ARBEIDSTAKER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.ATFL, Inntektskategori.FRILANSER, BigDecimal.TEN));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.FISKER, BigDecimal.TEN, false));
        tyGrunnlagBuilder.leggTilBeregningsgrunnlagAndel(lagAndel(AktivitetStatus.SN, Inntektskategori.DAGMAMMA, BigDecimal.TEN, false));
        tyGrunnlagBuilder.medGrunnbeløpSatser(Collections.singletonList(new Grunnbeløp(LocalDate.now(), LocalDate.now().plusWeeks(1), 1L, 2L)));
        tyGrunnlagBuilder.medYtelse(TilstøtendeYtelse.builder().medRelatertYtelseType(RelatertYtelseType.SYKEPENGER).build());

        // Act
        boolean skalBrukeInformasjonOmFordelingVedTY = mapper.skalBrukeInformasjonOmFordelingVedTY(tyGrunnlagBuilder.build(), Optional.of(no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori.FISKER));

        // Assert
        assertThat(skalBrukeInformasjonOmFordelingVedTY).isTrue();
    }


    private BeregningsgrunnlagAndelTilstøtendeYtelse lagAndel(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori, BigDecimal årsbeløpFraTY) {
        return lagAndel(aktivitetStatus, inntektskategori, årsbeløpFraTY, false);
    }

    private BeregningsgrunnlagAndelTilstøtendeYtelse lagAndel(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori, BigDecimal årsbeløpFraTY, boolean fraTY) {
        BeregningsgrunnlagAndelTilstøtendeYtelse.Builder builder = BeregningsgrunnlagAndelTilstøtendeYtelse.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medInntektskategori(inntektskategori)
            .medBeregnetPrÅr(årsbeløpFraTY);
        if (fraTY) {
            builder.medFraTilstøtendeYtelse();
        }
        return builder.build();
    }

}
