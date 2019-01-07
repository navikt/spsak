package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsgiverHistorikkinnslagTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsatteAndelerTidsbegrensetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattePerioderTidsbegrensetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBGTidsbegrensetArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class FastsettBGTidsbegrensetArbeidsforholdOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(repositoryRule.getEntityManager());

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();

    private FastsettBGTidsbegrensetArbeidsforholdOppdaterer fastsettBGTidsbegrensetArbeidsforholdOppdaterer;
    private Behandling behandling;
    private List<FastsattePerioderTidsbegrensetDto> fastsatteInnteker;
    private final LocalDate FØRSTE_PERIODE_FOM = LocalDate.now().minusDays(100);
    private final LocalDate FØRSTE_PERIODE_TOM = LocalDate.now().minusDays(50);
    private final LocalDate ANDRE_PERIODE_FOM = LocalDate.now().minusDays(49);
    private final LocalDate ANDRE_PERIODE_TOM = LocalDate.now();
    private final Long FØRSTE_ANDELSNR = 1L;
    private final Long ANDRE_ANDELSNR = 2L;
    private final Integer FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT = 100000;
    private final Integer FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT = 200000;
    private final Integer ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT = 300000;
    private final Integer ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT = 400000;
    private VirksomhetEntitet virksomhet1;
    private VirksomhetEntitet virksomhet2;


    @Before
    public void setup() {
        ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(null);
        fastsettBGTidsbegrensetArbeidsforholdOppdaterer = new FastsettBGTidsbegrensetArbeidsforholdOppdaterer(resultatRepositoryProvider, lagMockHistory(), aksjonspunktRepository, arbeidsgiverHistorikkinnslagTjeneste);
        fastsatteInnteker = lagFastsatteAndelerListe();
        virksomhet1 = new VirksomhetEntitet.Builder()
                .medOrgnr("123")
                .medNavn("VirksomhetNavn1")
                .oppdatertOpplysningerNå()
                .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet1);
        virksomhet2 = new VirksomhetEntitet.Builder()
                .medOrgnr("456")
                .medNavn("VirksomhetNavn2")
                .oppdatertOpplysningerNå()
                .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet2);
        repositoryRule.getEntityManager().flush();
    }

    private List<FastsattePerioderTidsbegrensetDto> lagFastsatteAndelerListe() {
        FastsatteAndelerTidsbegrensetDto andelEnPeriodeEn = new FastsatteAndelerTidsbegrensetDto(FØRSTE_ANDELSNR, FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT);
        FastsatteAndelerTidsbegrensetDto andelToPeriodeEn = new FastsatteAndelerTidsbegrensetDto(ANDRE_ANDELSNR, FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT);

        FastsattePerioderTidsbegrensetDto førstePeriode = new FastsattePerioderTidsbegrensetDto(
            FØRSTE_PERIODE_FOM,
            FØRSTE_PERIODE_TOM,
            Arrays.asList(andelEnPeriodeEn, andelToPeriodeEn)
        );

        FastsatteAndelerTidsbegrensetDto andelEnPeriodeTo = new FastsatteAndelerTidsbegrensetDto(FØRSTE_ANDELSNR, ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT);
        FastsatteAndelerTidsbegrensetDto andelToPeriodeTo = new FastsatteAndelerTidsbegrensetDto(ANDRE_ANDELSNR, ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT);

        FastsattePerioderTidsbegrensetDto andrePeriode = new FastsattePerioderTidsbegrensetDto(
            ANDRE_PERIODE_FOM,
            ANDRE_PERIODE_TOM,
            Arrays.asList(andelEnPeriodeTo, andelToPeriodeTo)
        );

        return Arrays.asList(førstePeriode, andrePeriode);
    }


    @Test
    public void skal_sette_korrekt_overstyrtSum_på_korrekt_periode_og_korrekt_andel() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();

        //Dto
        FastsettBGTidsbegrensetArbeidsforholdDto fastsettBGTidsbegrensetArbeidsforholdDto = new FastsettBGTidsbegrensetArbeidsforholdDto("begrunnelse", fastsatteInnteker);

        // Act
        fastsettBGTidsbegrensetArbeidsforholdOppdaterer.oppdater(fastsettBGTidsbegrensetArbeidsforholdDto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(beregningsgrunnlag.isPresent()).isTrue();
        BeregningsgrunnlagPeriode førstePeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPeriode andrePeriode = beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().get(1);
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(FØRSTE_PERIODE_FØRSTE_ANDEL_INNTEKT));
        assertThat(førstePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(FØRSTE_PERIODE_ANDRE_ANDEL_INNTEKT));
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(0).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(ANDRE_PERIODE_FØRSTE_ANDEL_INNTEKT));
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatusOgAndelList().get(1).getOverstyrtPrÅr()).isEqualTo(BigDecimal.valueOf(ANDRE_PERIODE_ANDRE_ANDEL_INNTEKT));
    }

    @Test
    public void skal_håndtere_overflødig_fastsett_bg_ATFL_aksjonspunkt() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();
        repositoryProvider.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS);

        //Dto
        FastsettBGTidsbegrensetArbeidsforholdDto fastsettBGTidsbegrensetArbeidsforholdDto = new FastsettBGTidsbegrensetArbeidsforholdDto("begrunnelse", fastsatteInnteker);
        // Act
        fastsettBGTidsbegrensetArbeidsforholdOppdaterer.oppdater(fastsettBGTidsbegrensetArbeidsforholdDto, behandling);

        //Assert
        Optional<Aksjonspunkt> overflødigAp = behandling.getAksjonspunkter().stream().filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS)).findFirst();
        assertThat(overflødigAp.get().erAvbrutt()).isTrue();

    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, VirksomhetEntitet virksomhet) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(Beregningsgrunnlag beregningsgrunnlag, LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();


        BeregningsgrunnlagPeriode førstePeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            FØRSTE_PERIODE_FOM, FØRSTE_PERIODE_TOM);
        buildBgPrStatusOgAndel(førstePeriode, virksomhet1);
        buildBgPrStatusOgAndel(førstePeriode, virksomhet2);

        BeregningsgrunnlagPeriode andrePeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            ANDRE_PERIODE_FOM, ANDRE_PERIODE_TOM);
        buildBgPrStatusOgAndel(andrePeriode, virksomhet1);
        buildBgPrStatusOgAndel(andrePeriode, virksomhet2);

        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }
}
