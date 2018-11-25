package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsatteVerdierDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettEndretBeregningsgrunnlagPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class FastsettEndretBeregningsgrunnlagOppdatererTest {

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);


    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    @Inject
    private HistorikkTjenesteAdapter historikkAdapter;

    @Inject
    private FastsettEndretBeregningsgrunnlagOppdaterer fastsettEndretBeregningsgrunnlagOppdaterer;
    @Inject
    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    public ScenarioMorSøkerForeldrepenger scenario;
    public Behandling behandling;

    @Before
    public void setup() {
        this.scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        this.behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void skal_finne_korrekt_andel_for_lagt_til_av_saksbehandler_i_tidligere_faktaavklaring() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel andelIForrigeBG = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);
        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);

        // Assert
        assertThat(korrektAndel).isEqualTo(andelIForrigeBG);
    }


    @Test
    public void skal_finne_korrekt_andel_for_lagt_til_av_saksbehandler_i_tidligere_faktaavklaring_med_ein_eksisterende_andel() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel andelIForrigeBG = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);
        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);

        // Assert
        assertThat(korrektAndel).isEqualTo(andelIForrigeBG);
    }


    @Test
    public void skal_finne_korrekt_andel_for_lagt_til_av_saksbehandler_i_forrige_faktaavklaring() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Long andelsnrLagtTil = 2L;
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(bg);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        Beregningsgrunnlag forrigeBg = bg.dypKopi();
        BeregningsgrunnlagPrStatusOgAndel andelLagtTil = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnrLagtTil)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(forrigeBg.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, forrigeBg, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnrLagtTil, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);

        // Assert
        assertThat(korrektAndel).isEqualTo(andelLagtTil);
    }


    @Test(expected = TekniskException.class)
    public void skal_kaste_exception_om_det_finnes_meir_enn_1_overlappende_periode_i_forrige_grunnlag() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIForrigeBG1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(10).minusDays(1))
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG1);


        BeregningsgrunnlagPeriode periodeIForrigeBG2 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT.plusWeeks(10), null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG2);
        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);

        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);
    }


    @Test
    public void skal_matche_på_arbeidsforholdref_om_andelsnr_gir_tomt_resultat() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG);
        BeregningsgrunnlagPrStatusOgAndel andelLagtTil = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG);
        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, null, lagtTilAvSaksbehandler);
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);

        // Assert
        assertThat(korrektAndel).isEqualTo(andelLagtTil);
    }


    @Test
    public void skal_matche_på_arbeidsforholdref_om_andelsnr_gir_tomt_resultat_for_andel_lagt_til_i_tidligere_grunnlag() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel andelITidligereGrunnlag = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIForrigeBG);
        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(10, 10, Inntektskategori.ARBEIDSTAKER);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, null, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettEndretBeregningsgrunnlagOppdaterer.getKorrektAndel(behandling, periode, endretAndel);

        // Assert
        assertThat(korrektAndel).isEqualTo(andelITidligereGrunnlag);
    }


    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_1_periode_og_1_andel_refusjon_lik_null() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = null;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);
        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Collections.singletonList(endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(andel.getInntektskategori()).isEqualTo(inntektskategori);

    }


    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_1_periode_og_2_andeler() {
        // Arrange
        String arbId = "123124";
        String arbId2 = "3242342";
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel1 = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId2))
            .medAndelsnr(andelsnr2)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);
        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Collections.singletonList(endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(andel1.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(andel1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(andel1.getInntektskategori()).isEqualTo(inntektskategori);
    }


    @Test
    public void skal_sette_verdier_på_ny_andel_med_1_periode_og_1_andel() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        boolean nyAndel2 = true;
        boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FastsatteVerdierDto fastsatteVerdier2 = new FastsatteVerdierDto(refusjon2, fastsatt2, inntektskategori2);
        RedigerbarAndelDto andelDto2 = new RedigerbarAndelDto("Andelen", nyAndel2, arbId, andelsnr, lagtTilAvSaksbehandler2);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel2 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto2, fastsatteVerdier2);


        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel2, endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert

        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        assertThat(eksisterendeAndel.size()).isEqualTo(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon2 * 12));
        assertThat(andelLagtTil.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getInntektskategori()).isEqualTo(inntektskategori2);
    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_KOFAKBER_med_1_periode_og_1_andel() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Long andelsnrForAndelLagtTilAvSaksbehandler = 2L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        Beregningsgrunnlag forrigeGrunnlag = beregningsgrunnlag.dypKopi();
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnrForAndelLagtTilAvSaksbehandler)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(forrigeGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, forrigeGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        boolean nyAndel2 = false;
        boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FastsatteVerdierDto fastsatteVerdier2 = new FastsatteVerdierDto(refusjon2, fastsatt2, inntektskategori2);
        RedigerbarAndelDto andelDto2 = new RedigerbarAndelDto("Andelen", nyAndel2, arbId, andelsnrForAndelLagtTilAvSaksbehandler, lagtTilAvSaksbehandler2);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel2 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto2, fastsatteVerdier2);


        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel2, endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        assertThat(eksisterendeAndel.size()).isEqualTo(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon2 * 12));
        assertThat(andelLagtTil.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getInntektskategori()).isEqualTo(inntektskategori2);
    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Long andelsnr2 = 1L;
        BigDecimal forrigeFastsatt = BigDecimal.valueOf(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr2)
            .medBeregnetPrÅr(forrigeFastsatt)
            .medLagtTilAvSaksbehandler(true)
            .medFastsattAvSaksbehandler(true)
            .medInntektskategori(forrigeInntektskategori)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        boolean nyAndel2 = false;
        boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FastsatteVerdierDto fastsatteVerdier2 = new FastsatteVerdierDto(refusjon2, fastsatt2, inntektskategori2);
        RedigerbarAndelDto andelDto2 = new RedigerbarAndelDto("Andelen", nyAndel2, arbId, andelsnr2, lagtTilAvSaksbehandler2);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel2 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto2, fastsatteVerdier2);


        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel, endretAndel2), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        assertThat(eksisterendeAndel.size()).isEqualTo(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(1);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon2 * 12));
        assertThat(andelLagtTil.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 * 12));
        assertThat(andelLagtTil.get(0).getInntektskategori()).isEqualTo(inntektskategori2);
    }


    @Test
    public void skal_ikkje_legge_til_slettet_andel_ved_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Long andelsnr2 = 1L;
        BigDecimal forrigeFastsatt = BigDecimal.valueOf(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr2)
            .medBeregnetPrÅr(forrigeFastsatt)
            .medLagtTilAvSaksbehandler(true)
            .medFastsattAvSaksbehandler(true)
            .medInntektskategori(forrigeInntektskategori)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(1);
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        assertThat(eksisterendeAndel.size()).isEqualTo(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(0);
    }


    @Test
    public void skal_sette_verdier_på_andeler_for_tilbakehopp_til_steg_før_KOFAKBER_med_nye_andeler_og_eksisterende_andeler_i_ulike_arbeidsforhold() {
        // Arrange
        String arbId = "123124";
        String arbId2 = "1324423423124";
        Long andelsnr = 1L;
        Long andelsnr2 = 2L;
        Long andelsnr3 = 3L;

        BigDecimal forrigeFastsatt = BigDecimal.valueOf(200000);
        Inntektskategori forrigeInntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeForrigeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr2)
            .medBeregnetPrÅr(forrigeFastsatt)
            .medLagtTilAvSaksbehandler(true)
            .medFastsattAvSaksbehandler(true)
            .medInntektskategori(forrigeInntektskategori)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeForrigeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        BeregningsgrunnlagPeriode periode2 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT.plusMonths(2), null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode2);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId2))
            .medAndelsnr(andelsnr3)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode2);


        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjon = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjon, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);

        boolean nyAndel2 = false;
        boolean lagtTilAvSaksbehandler2 = true;
        Integer refusjon2 = 3000;
        Integer fastsatt2 = 20000;
        Inntektskategori inntektskategori2 = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
        FastsatteVerdierDto fastsatteVerdier2 = new FastsatteVerdierDto(refusjon2, fastsatt2, inntektskategori2);
        RedigerbarAndelDto andelDto2 = new RedigerbarAndelDto("Andelen", nyAndel2, arbId, andelsnr2, lagtTilAvSaksbehandler2);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel2 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto2, fastsatteVerdier2);


        boolean nyAndel3 = true;
        boolean lagtTilAvSaksbehandler3 = true;
        Integer refusjon3 = 2000;
        Integer fastsatt3 = 30000;
        Inntektskategori inntektskategori3 = Inntektskategori.JORDBRUKER;
        FastsatteVerdierDto fastsatteVerdier3 = new FastsatteVerdierDto(refusjon3, fastsatt3, inntektskategori3);
        RedigerbarAndelDto andelDto3 = new RedigerbarAndelDto("Andelen", nyAndel3, arbId, andelsnr, lagtTilAvSaksbehandler3);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel3 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto3, fastsatteVerdier3);

        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode1 = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel3, endretAndel, endretAndel2), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1));

        boolean nyAndel4 = false;
        boolean lagtTilAvSaksbehandler4 = false;
        Integer refusjon4 = 10000;
        Integer fastsatt4 = 40000;
        Inntektskategori inntektskategori4 = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier4 = new FastsatteVerdierDto(refusjon4, fastsatt4, inntektskategori4);
        RedigerbarAndelDto andelDto4 = new RedigerbarAndelDto("Andelen", nyAndel4, arbId2, andelsnr3, lagtTilAvSaksbehandler4);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel4 = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto4, fastsatteVerdier4);

        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode2 = new FastsettEndretBeregningsgrunnlagPeriodeDto(Arrays.asList(endretAndel3, endretAndel, endretAndel4), SKJÆRINGSTIDSPUNKT.plusMonths(2), null);


        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Arrays.asList(endretPeriode2, endretPeriode1));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(periode1.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel = periode1
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());

        assertThat(eksisterendeAndel.size()).isEqualTo(1);
        assertThat(eksisterendeAndel.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel.get(0).getInntektskategori()).isEqualTo(inntektskategori);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = periode1
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(2);
        Optional<BeregningsgrunnlagPrStatusOgAndel> fraForrige = andelLagtTil.stream().filter(lagtTil -> lagtTil.getInntektskategori().equals(inntektskategori2)).findFirst();
        assertThat(fraForrige.isPresent()).isTrue();
        assertThat(fraForrige.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(fraForrige.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon2 * 12));
        assertThat(fraForrige.get().getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 * 12));
        assertThat(fraForrige.get().getInntektskategori()).isEqualTo(inntektskategori2);

        Optional<BeregningsgrunnlagPrStatusOgAndel> ny = andelLagtTil.stream().filter(lagtTil -> lagtTil.getInntektskategori().equals(inntektskategori3)).findFirst();
        assertThat(ny.isPresent()).isTrue();
        assertThat(ny.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(ny.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon3 * 12));
        assertThat(ny.get().getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt3 * 12));
        assertThat(ny.get().getInntektskategori()).isEqualTo(inntektskategori3);

        assertThat(periode2.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel2 = periode2
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr)).collect(Collectors.toList());
        assertThat(eksisterendeAndel2.size()).isEqualTo(1);
        assertThat(eksisterendeAndel2.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon * 12));
        assertThat(eksisterendeAndel2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt * 12));
        assertThat(eksisterendeAndel2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> eksisterendeAndel3 = periode2
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(andelsnr3)).collect(Collectors.toList());
        assertThat(eksisterendeAndel3.size()).isEqualTo(1);
        assertThat(eksisterendeAndel3.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon4 * 12));
        assertThat(eksisterendeAndel3.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt4 * 12));
        assertThat(eksisterendeAndel3.get(0).getInntektskategori()).isEqualTo(inntektskategori4);


        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil2 = periode2
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());

        assertThat(andelLagtTil2.size()).isEqualTo(1);
        Optional<BeregningsgrunnlagPrStatusOgAndel> fraForrige2 = andelLagtTil2.stream().filter(lagtTil -> lagtTil.getInntektskategori().equals(inntektskategori2)).findFirst();
        assertThat(fraForrige2.isPresent()).isFalse();
        assertThat(fraForrige.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(fraForrige.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon2 * 12));
        assertThat(fraForrige.get().getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt2 * 12));
        assertThat(fraForrige.get().getInntektskategori()).isEqualTo(inntektskategori2);

        Optional<BeregningsgrunnlagPrStatusOgAndel> ny2 = andelLagtTil2.stream().filter(lagtTil -> lagtTil.getInntektskategori().equals(inntektskategori3)).findFirst();
        assertThat(ny2.isPresent()).isTrue();
        assertThat(ny2.get().getAndelsnr()).isNotEqualTo(eksisterendeAndel.get(0).getAndelsnr());
        assertThat(ny2.get().getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null))
            .isEqualByComparingTo(BigDecimal.valueOf(refusjon3 * 12));
        assertThat(ny2.get().getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt3 * 12));
        assertThat(ny2.get().getInntektskategori()).isEqualTo(inntektskategori3);
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_1_periode_og_1_andel_med_refusjon_og_sjekk_historikkinnslag() {

        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        BigDecimal refusjonskravPrÅr = BigDecimal.valueOf(120_000);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId).medRefusjonskravPrÅr(refusjonskravPrÅr))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer refusjonPrMåned = 5000;
        Integer fastsatt = 10000;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsatteVerdierDto fastsatteVerdier = new FastsatteVerdierDto(refusjonPrMåned, fastsatt, inntektskategori);
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, arbId, andelsnr, lagtTilAvSaksbehandler);
        FastsettEndretBeregningsgrunnlagAndelDto endretAndel = new FastsettEndretBeregningsgrunnlagAndelDto(andelDto, fastsatteVerdier);
        FastsettEndretBeregningsgrunnlagPeriodeDto endretPeriode = new FastsettEndretBeregningsgrunnlagPeriodeDto(Collections.singletonList(endretAndel), SKJÆRINGSTIDSPUNKT, null);
        FastsettEndretBeregningsgrunnlagDto endretDto = new FastsettEndretBeregningsgrunnlagDto(Collections.singletonList(endretPeriode));

        // Act
        fastsettEndretBeregningsgrunnlagOppdaterer.oppdater(endretDto, behandling, beregningsgrunnlag);

        // Assert
        List<HistorikkinnslagDel> deler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        List<HistorikkinnslagFelt> historikkinnslagFelt = deler.get(0).getHistorikkinnslagFelt();

        assertThat(deler.size()).isEqualTo(1);
        assertThat(historikkinnslagFelt.size()).isEqualTo(5);
        HistorikkinnslagDel del = deler.get(0);

        Optional<HistorikkinnslagFelt> gjeldendeFraFelt = del.getGjeldendeFraFelt();
        assertThat(gjeldendeFraFelt).isPresent();
        assertEndretFelt(gjeldendeFraFelt.get(), HistorikkEndretFeltType.NY_FORDELING, null, dtf.format(SKJÆRINGSTIDSPUNKT), arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(andel));

        assertEndretFelt(del, HistorikkEndretFeltType.NYTT_REFUSJONSKRAV,
            String.valueOf(refusjonskravPrÅr.divide(BigDecimal.valueOf(12), 0, BigDecimal.ROUND_HALF_UP)),
            String.valueOf(refusjonPrMåned));

        assertEndretFelt(del, HistorikkEndretFeltType.INNTEKT, null, String.valueOf(fastsatt));

        assertEndretFelt(del, HistorikkEndretFeltType.INNTEKTSKATEGORI, null, Inntektskategori.SJØMANN.getKode());

        assertThat(del.getSkjermlenke()).hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).isEqualTo(SkjermlenkeType.FAKTA_OM_BEREGNING.getKode()));
    }

    private void assertEndretFelt(HistorikkinnslagDel del,
                                  HistorikkEndretFeltType endretFeltType,
                                  String expectedFraVerdi,
                                  String expectedTilVerdi) {
        Optional<HistorikkinnslagFelt> endretFeltOpt = del.getEndretFelt(endretFeltType);
        assertThat(endretFeltOpt).as("endretFelt").hasValueSatisfying(endretFelt ->
            assertEndretFelt(endretFelt, endretFeltType, expectedFraVerdi, expectedTilVerdi, null));
    }

    private void assertEndretFelt(HistorikkinnslagFelt endretFelt, HistorikkEndretFeltType endretFeltType, String expectedFraVerdi, String expectedTilVerdi, String expectedNavnVerdi) {
        assertThat(endretFelt.getNavn()).as("navn").isEqualTo(endretFeltType.getKode());
        assertThat(endretFelt.getTilVerdi()).as("tilVerdi").isEqualTo(expectedTilVerdi);
        assertThat(endretFelt.getFraVerdi()).as("fraVerdi").isEqualTo(expectedFraVerdi);
        assertThat(endretFelt.getNavnVerdi()).isEqualTo(expectedNavnVerdi);
    }

}
