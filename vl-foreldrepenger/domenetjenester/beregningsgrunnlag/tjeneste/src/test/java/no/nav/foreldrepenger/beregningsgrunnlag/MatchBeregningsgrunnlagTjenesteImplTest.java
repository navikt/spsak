package no.nav.foreldrepenger.beregningsgrunnlag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.exception.TekniskException;

public class MatchBeregningsgrunnlagTjenesteImplTest {


    private static final AktørId AKTØR_ID = new AktørId("210195");
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);
    private Arbeidsgiver arbeidsgiverEn;
    private Arbeidsgiver arbeidsgiverTo;

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private MatchBeregningsgrunnlagTjeneste matchBeregningsgrunnlagTjeneste = new MatchBeregningsgrunnlagTjenesteImpl(repositoryProvider);
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    public ScenarioMorSøkerForeldrepenger scenario;
    public Behandling behandling;
    private BeregningArbeidsgiverTestUtil virksomhetTestUtil;


    @Before
    public void setUp() {

        this.scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        this.behandling = scenario.lagre(repositoryProvider);
        virksomhetTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        arbeidsgiverEn = virksomhetTestUtil.forArbeidsgiverVirksomhet("412412421");
        arbeidsgiverTo = virksomhetTestUtil.forArbeidsgiverVirksomhet("987686557");
    }


    @Test
    public void skal_finne_korrekt_andel_i_forrige_grunnlag() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;
        Beregningsgrunnlag forrigeBG = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periodeIGjeldendeBG = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null)
            .build(forrigeBG);
        BeregningsgrunnlagPrStatusOgAndel andelIForrigeBg = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periodeIGjeldendeBG);

        beregningsgrunnlagRepository.lagre(behandling, forrigeBG, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, andelsnr, arbId);

        // Assert
        assertThat(korrektAndel.get()).isEqualTo(andelIForrigeBg);
    }


    @Test
    public void skal_returnere_empty_når_det_ikkje_finnes_forrige_grunnlag() {
        // Arrange
        String arbId = "123124";
        Long andelsnr = 1L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, andelsnr, arbId);

        // Assert
        assertThat(korrektAndel.isPresent()).isFalse();
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

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medGjeldendeBeregningsgrunnlag(forrigeBG).medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        matchBeregningsgrunnlagTjeneste.matchMedAndelIForrigeBeregningsgrunnlag(behandling, periode, andelsnr, arbId);
    }


    @Test
    public void skal_matche_på_andelsnr() {
        // Arrange
        String arbId = null;
        Long andelsnr = 1L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = matchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(behandling, periode, andelsnr, arbId);

        // Assert
        assertThat(korrektAndel).isEqualTo(andel);
    }


    @Test
    public void skal_matche_på_arbid_om_andelsnr_input_er_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = matchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(behandling, periode, null, arbId);

        // Assert
        assertThat(korrektAndel).isEqualTo(andel);
    }


    @Test(expected = TekniskException.class)
    public void skal_kaste_exception_om_andel_ikkje_eksisterer_i_grunnlag() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        matchBeregningsgrunnlagTjeneste.matchMedAndelFraPeriode(behandling, periode, 2L, "523325252");
    }


    @Test
    public void skal_matche_på_arbid_om_den_er_ulik_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;

        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(arbId))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode);

        // Act
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåArbeidsforholdIdHvisTilgjengelig(behandling, periode, arbId, null);

        // Assert
        assertThat(korrektAndel).isEqualTo(andel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void skal_kaste_exception_når_aktivitetstatus_er_lik_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;
        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbId);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        // Act
        matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, null, inntektskategori, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void skal_kaste_exception_når_inntektskategori_er_lik_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;
        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbId);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverTo)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        // Act
        matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, null, null, null);
    }


    @Test
    public void skal_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_når_orgnr_er_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;
        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbId);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medBGAndelArbeidsforhold(arb)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, null, null);

        // Assert
        assertThat(korrektAndel.isPresent()).isTrue();
        assertThat(korrektAndel.get()).isEqualTo(andel);
    }

    @Test
    public void skal_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_når_orgnr_er_ulik_null() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef(arbId);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverTo)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), null);

        // Assert
        assertThat(korrektAndel.isPresent()).isTrue();
        assertThat(korrektAndel.get()).isEqualTo(andel);
    }


    @Test
    public void skal_ikkje_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_når_orgnr_er_ulik_null_og_inntektskategori_ikkje_matcher() {
        // Arrange
        String arbId = "1235235";
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);

        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef(arbId);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverTo)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.DAGMAMMA)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), null);

        // Assert
        assertThat(korrektAndel.isPresent()).isFalse();
    }


    @Test
    public void skal_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_og_arbeidsforholdId_når_orgnr_er_ulik_null_og_arbeidsforholdId_er_lik_null_og_periode_har_andel_uten_arbeidsforholdRef() {
        // Arrange
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnr)
            .medBGAndelArbeidsforhold(arb)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), null);

        // Assert
        assertThat(korrektAndel.isPresent()).isTrue();
        assertThat(korrektAndel.get()).isEqualTo(andel);
    }


    @Test
    public void skal_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_og_arbeidsforholdId_når_orgnr_er_ulik_null_og_arbeidsforholdId_er_lik_null_og_periode_har_andel_med_referanse_lik_null() {
        // Arrange
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef(null);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);

        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), null);

        // Assert
        assertThat(korrektAndel.isPresent()).isTrue();
        assertThat(korrektAndel.get()).isEqualTo(andel);
    }


    @Test
    public void skal_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_og_arbeidsforholdId_når_orgnr_er_ulik_null_og_arbeidsforholdId_er_ulik_null() {
        // Arrange
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        String arbforholdRef = "241412";
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef(arbforholdRef);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("2235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), arbforholdRef);

        // Assert
        assertThat(korrektAndel.isPresent()).isTrue();
        assertThat(korrektAndel.get()).isEqualTo(andel);
    }

    @Test
    public void skal_ikkje_matche_på_aktivitetstatus_og_inntektskategori_og_orgnr_og_arbeidsforholdId_når_orgnr_er_ulik_null_og_arbeidsforholdId_er_ulik_null() {
        // Arrange
        Long andelsnr = 1L;

        AktivitetStatus aktivitetStatus = AktivitetStatus.ARBEIDSTAKER;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, null).build(beregningsgrunnlag);
        String arbforholdRef = "241412";
        BGAndelArbeidsforhold.Builder arb = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef(arbforholdRef);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arb)
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);
        BGAndelArbeidsforhold.Builder arbTo = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(arbeidsgiverEn)
            .medArbforholdRef("235253");
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(arbTo)
            .medAndelsnr(2L)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(inntektskategori)
            .medAktivitetStatus(aktivitetStatus)
            .build(periode);


        // Act
        Optional<BeregningsgrunnlagPrStatusOgAndel> korrektAndel = matchBeregningsgrunnlagTjeneste.matchPåTilgjengeligAndelsinformasjon(periode, aktivitetStatus, inntektskategori, arbeidsgiverEn.getIdentifikator(), "534543");

        // Assert
        assertThat(korrektAndel.isPresent()).isFalse();
    }


}
