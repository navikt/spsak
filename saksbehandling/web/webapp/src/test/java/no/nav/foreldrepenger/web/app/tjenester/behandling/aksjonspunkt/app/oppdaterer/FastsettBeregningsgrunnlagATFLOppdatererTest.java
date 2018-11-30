package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsgiverHistorikkinnslagTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBeregningsgrunnlagATFLDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.InntektPrAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class FastsettBeregningsgrunnlagATFLOppdatererTest {
    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository = new BeregningsgrunnlagRepositoryImpl(repositoryRule.getEntityManager(), repositoryProvider.getBehandlingLåsRepository());

    private Behandling behandling;
    private FastsettBeregningsgrunnlagATFLOppdaterer oppdaterer;

    private final List<VirksomhetEntitet> virksomheter = new ArrayList<>();

    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final String ORGNR1 = "1234";
    private static final String ORGNR2 = "2345";
    private static final String ARBEIDSFORHOLD_1 = "AF0";
    private static final String ARBEIDSFORHOLD_2 = "AF1";
    private static final String ARBEIDSFORHOLD_ID = "kjashngaiughabcd";
    private static final int BRUTTO_PR_AR = 150000;
    private static final int OVERSTYRT_PR_AR = 200000;
    private static final int FRILANSER_INNTEKT = 4000;

    @Before
    public void setup() {
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr(ORGNR1)
                .medNavn(ARBEIDSFORHOLD_1)
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr(ORGNR2)
                .medNavn(ARBEIDSFORHOLD_2)
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.forEach(v -> repositoryProvider.getVirksomhetRepository().lagre(v));
        ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(null);
        oppdaterer = new FastsettBeregningsgrunnlagATFLOppdaterer(repositoryProvider, lagMockHistory(), arbeidsgiverHistorikkinnslagTjeneste);
    }

    @Test
    public void skal_generere_historikkinnslag_ved_fastsettelse_av_brutto_beregningsgrunnlag_AT() {
        // Arrange
        buildOgLagreBeregningsgrunnlag(true, 1, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkInnslag).hasSize(1);

        HistorikkinnslagDel del = historikkInnslag.get(0);
        List<HistorikkinnslagFelt> feltList = del.getEndredeFelt();
        assertThat(feltList).hasSize(1);
        assertThat(feltList.get(0)).satisfies(felt -> {
            assertThat(felt.getNavn()).as("navn").isEqualTo(HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD.getKode());
            assertThat(felt.getNavnVerdi()).as("navnVerdi").isEqualTo("AF0 (1234) ...abcd");
            assertThat(felt.getFraVerdi()).as("fraVerdi").isNull();
            assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo("200000");
        });
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("begrunnelse"));
    }

    @Test
    public void skal_generere_historikkinnslag_ved_fastsettelse_av_brutto_beregningsgrunnlag_FL() {
        // Arrange
        buildOgLagreBeregningsgrunnlag(false, 1, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkInnslag).hasSize(1);

        HistorikkinnslagDel del = historikkInnslag.get(0);
        List<HistorikkinnslagFelt> feltList = del.getEndredeFelt();
        assertThat(feltList).hasSize(1);
        assertThat(feltList.get(0)).satisfies(felt -> {
            assertThat(felt.getNavn()).as("navn").isEqualTo(HistorikkEndretFeltType.FRILANS_INNTEKT.getKode());
            assertThat(felt.getFraVerdi()).as("fraVerdi").isNull();
            assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo("4000");
        });
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("begrunnelse"));
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_AT() {
        //Arrange
        buildOgLagreBeregningsgrunnlag(true, 1, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, 1, OVERSTYRT_PR_AR));
    }

    @Test
    public void skal_oppdatere_bruttoPrÅr_i_beregningsgrunnlagperiode_når_andel_overstyres_AT() {
        //Arrange
        int overstyrt1 = 1000;
        int overstyrt2 = 2000;
        int antallAndeler = 2;
        buildOgLagreBeregningsgrunnlag(true, 1, antallAndeler);

        List<InntektPrAndelDto> overstyrteVerdier = new ArrayList<>();
        overstyrteVerdier.add(new InntektPrAndelDto(overstyrt1, 1L));
        overstyrteVerdier.add(new InntektPrAndelDto(overstyrt2, 2L));

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", overstyrteVerdier, FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> {
            BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
            List<BeregningsgrunnlagPrStatusOgAndel> andeler = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
            assertThat(andeler.size()).isEqualTo(antallAndeler);
            BigDecimal nyBruttoBG1 = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0)
                .getBruttoPrÅr();
            assertThat(nyBruttoBG1.intValue()).as("nyBruttoBG").isEqualTo(overstyrt1);
            BigDecimal nyBruttoBG2 = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1)
                .getBruttoPrÅr();
            assertThat(nyBruttoBG2.intValue()).as("nyBruttoBG").isEqualTo(overstyrt2);
            assertThat(beregningsgrunnlagPeriode.getBruttoPrÅr().intValue()).as("nyBruttoBGPeriode").isEqualTo(overstyrt1 + overstyrt2);
        });
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_med_andeler_med_ulike_inntektskategorier_AT() {
        //Arrange
        List<List<Boolean>> arbeidstakerPrPeriode = Arrays.asList(Arrays.asList(false, true), Arrays.asList(false, true, true, true));
        List<Inntektskategori> inntektskategoriPeriode1 = Arrays.asList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER);
        List<Inntektskategori> inntektskategoriPeriode2 = Arrays.asList(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.ARBEIDSTAKER,
            Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.SJØMANN);
        List<List<Inntektskategori>> inntektskategoriPrPeriode = Arrays.asList(inntektskategoriPeriode1, inntektskategoriPeriode2);
        buildOgLagreBeregningsgrunnlag(arbeidstakerPrPeriode, inntektskategoriPrPeriode);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 2L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlagOpt.get().getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerPeriode1 = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerPeriode1).hasSize(2);
        assertThat(andelerPeriode1.get(1).getBruttoPrÅr().intValue()).isEqualByComparingTo(OVERSTYRT_PR_AR);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerPeriode2 = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerPeriode2).hasSize(4);
        assertThat(andelerPeriode2.get(1).getBruttoPrÅr().intValue()).isEqualByComparingTo(OVERSTYRT_PR_AR);
        assertThat(andelerPeriode2.get(2).getBruttoPrÅr().intValue()).isEqualTo(BRUTTO_PR_AR);
        assertThat(andelerPeriode2.get(3).getBruttoPrÅr().intValue()).isEqualTo(BRUTTO_PR_AR);
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_AT() {
        //Arrange
        int antallPerioder = 3;
        buildOgLagreBeregningsgrunnlag(true, antallPerioder, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, antallPerioder, OVERSTYRT_PR_AR));
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_FL() {
        //Arrange
        buildOgLagreBeregningsgrunnlag(false, 1, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, 1, FRILANSER_INNTEKT));
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder_FL() {
        //Arrange
        int antallPerioder = 3;
        buildOgLagreBeregningsgrunnlag(false, antallPerioder, 1);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(beregningsgrunnlag -> assertBeregningsgrunnlag(beregningsgrunnlag, antallPerioder, FRILANSER_INNTEKT));
    }

    @Test
    public void skal_håndtere_overflødig_fastsett_tidsbegrenset_arbeidsforhold_aksjonspunkt() {
        //Arrange
        buildOgLagreBeregningsgrunnlag(true, 1, 1);
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD);

        //Dto
        FastsettBeregningsgrunnlagATFLDto dto = new FastsettBeregningsgrunnlagATFLDto("begrunnelse", Collections.singletonList(new InntektPrAndelDto(OVERSTYRT_PR_AR, 1L)), FRILANSER_INNTEKT);

        // Act
        oppdaterer.oppdater(dto, behandling);

        //Assert
        Optional<Aksjonspunkt> overflødigAp = behandling.getAksjonspunkter().stream().filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD)).findFirst();
        assertThat(overflødigAp.get().erAvbrutt()).isTrue();
    }

    private void assertBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag, int antallPerioder, int frilanserInntekt) {
        List<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode> beregningsgrunnlagperioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(beregningsgrunnlagperioder.size()).isEqualTo(antallPerioder);
        beregningsgrunnlagperioder.forEach(periode -> {
                BigDecimal nyBruttoBG = periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0)
                    .getBruttoPrÅr();
                assertThat(nyBruttoBG.intValue()).as("nyBruttoBG").isEqualTo(frilanserInntekt);
            }
        );
    }


    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private void buildOgLagreBeregningsgrunnlag(List<List<Boolean>> erArbeidstakerPrPeriode, List<List<Inntektskategori>> inntektskategoriPrPeriode) {
        ScenarioMorSøkerForeldrepenger scenario = lagScenario();

        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5));

        assertThat(erArbeidstakerPrPeriode).hasSize(inntektskategoriPrPeriode.size());
        for (int i = 0; i < erArbeidstakerPrPeriode.size(); i++) {
            LocalDate fom = LocalDate.now().minusDays(20).plusDays(i * 5).plusDays(i == 0 ? 0 : 1);
            LocalDate tom = fom.plusDays(5);
            leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagBuilder, fom, tom, erArbeidstakerPrPeriode.get(i), inntektskategoriPrPeriode.get(i));
        }
        behandling = scenario.lagre(repositoryProvider);
    }

    private void buildOgLagreBeregningsgrunnlag(boolean erArbeidstaker, int antallPerioder, int antallAndeler) {
        ScenarioMorSøkerForeldrepenger scenario = lagScenario();

        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5));

        for (int i = 0; i < antallPerioder; i++) {
            LocalDate fom = LocalDate.now().minusDays(20).plusDays(i * 5).plusDays(i == 0 ? 0 : 1);
            LocalDate tom = fom.plusDays(5);
            leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagBuilder, fom, tom, erArbeidstaker, antallAndeler);
        }
        behandling = scenario.lagre(repositoryProvider);
    }

    private ScenarioMorSøkerForeldrepenger lagScenario() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);
        return scenario;
    }

    private void leggTilBeregningsgrunnlagPeriode(Beregningsgrunnlag.Builder beregningsgrunnlagBuilder, LocalDate fomDato, LocalDate tomDato, List<Boolean> erArbeidstakerList, List<Inntektskategori> inntektskategoriList) {
        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fomDato, tomDato);
        assertThat(erArbeidstakerList).hasSize(inntektskategoriList.size());
        for (int i = 0; i < erArbeidstakerList.size(); i++) {
            if (erArbeidstakerList.get(i)) {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.ARBEIDSTAKER, virksomheter.get(0), "ARBEIDSFORHOLD", (long) (i+1), inntektskategoriList.get(i));
            } else {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.FRILANSER, null, null, ((long) (i+1)), inntektskategoriList.get(i));
            }
        }
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
    }


    private void leggTilBeregningsgrunnlagPeriode(Beregningsgrunnlag.Builder beregningsgrunnlagBuilder, LocalDate fomDato, LocalDate tomDato, boolean erArbeidstaker, int antallAndeler) {
        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fomDato, tomDato);
        for (int i = 0; i < antallAndeler; i++) {
            if (erArbeidstaker) {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.ARBEIDSTAKER, virksomheter.get(i), ARBEIDSFORHOLD_ID, (long) (i+1), Inntektskategori.ARBEIDSTAKER);
            } else {
                leggTilBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriodeBuilder, AktivitetStatus.FRILANSER, null, null, ((long) (i+1)), Inntektskategori.FRILANSER);
            }
        }
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
    }

    private void leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder, AktivitetStatus aktivitetStatus,
                                                          VirksomhetEntitet virksomheten, String arbforholdId, Long andelsnr, Inntektskategori inntektskategori) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbforholdId)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomheten))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        beregningsgrunnlagPeriodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(bga)
                .medAndelsnr(andelsnr)
                .medInntektskategori(inntektskategori)
                .medAktivitetStatus(aktivitetStatus)
                .medBeregnetPrÅr(BigDecimal.valueOf(BRUTTO_PR_AR)));
    }
}
