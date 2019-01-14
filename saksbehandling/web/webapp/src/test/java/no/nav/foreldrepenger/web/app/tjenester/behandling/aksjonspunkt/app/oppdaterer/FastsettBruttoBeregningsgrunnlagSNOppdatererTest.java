package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBruttoBeregningsgrunnlagSNDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto;

public class FastsettBruttoBeregningsgrunnlagSNOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();

    private static final int BRUTTO_BG = 200000;
    private VurderVarigEndringEllerNyoppstartetSNOppdaterer vurderVarigEndringEllerNyoppstartetSNOppdaterer;
    private FastsettBruttoBeregningsgrunnlagSNOppdaterer fastsettBruttoBeregningsgrunnlagSNOppdaterer;
    private Behandling behandling;

    @Before
    public void setup() {
        fastsettBruttoBeregningsgrunnlagSNOppdaterer = new FastsettBruttoBeregningsgrunnlagSNOppdaterer(repositoryProvider, resultatRepositoryProvider, lagMockHistory());
        vurderVarigEndringEllerNyoppstartetSNOppdaterer = new VurderVarigEndringEllerNyoppstartetSNOppdaterer(repositoryProvider, resultatRepositoryProvider, lagMockHistory());
    }

    @Test
    public void skal_generere_historikkinnslag_ved_fastsettelse_av_brutto_beregningsgrunnlag_SN() {
        // Arrange
        boolean varigEndring = true;
        lagBehandlingMedBeregningsgrunnlag(1);

        //Dto
        VurderVarigEndringEllerNyoppstartetSNDto vurderVarigEndringEllerNyoppstartetSNDto = new VurderVarigEndringEllerNyoppstartetSNDto("begrunnelse1", varigEndring);
        FastsettBruttoBeregningsgrunnlagSNDto fastsettBGDto = new FastsettBruttoBeregningsgrunnlagSNDto("begrunnelse2", BRUTTO_BG);

        // Act
        vurderVarigEndringEllerNyoppstartetSNOppdaterer.oppdater(vurderVarigEndringEllerNyoppstartetSNDto, behandling);
        fastsettBruttoBeregningsgrunnlagSNOppdaterer.oppdater(fastsettBGDto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.medHendelse(HistorikkinnslagType.FAKTA_ENDRET).build(historikkinnslag);

        // Assert
        assertThat(historikkInnslag).hasSize(2);

        HistorikkinnslagDel del1 = historikkInnslag.get(0);
        assertThat(del1.getEndredeFelt()).hasSize(1);
        assertHistorikkinnslagFelt(del1, HistorikkEndretFeltType.ENDRING_NAERING, null, HistorikkEndretFeltVerdiType.VARIG_ENDRET_NAERING.getKode());
        assertThat(del1.getBegrunnelse()).as("begrunnelse").hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("begrunnelse1"));
        assertThat(del1.getSkjermlenke()).as("skjermlenkeOpt").hasValueSatisfying(skjermlenke ->
            assertThat(skjermlenke).as("skjermlenke1").isEqualTo(SkjermlenkeType.BEREGNING_FORELDREPENGER.getKode()));

        HistorikkinnslagDel del2 = historikkInnslag.get(1);
        assertThat(del2.getEndredeFelt()).hasSize(1);
        assertHistorikkinnslagFelt(del2, HistorikkEndretFeltType.BRUTTO_NAERINGSINNTEKT, null, "200000");
        assertThat(del2.getBegrunnelse()).as("begrunnelse").hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("begrunnelse2"));
        assertThat(del2.getSkjermlenke()).as("skjermlenke2").isNotPresent();
    }

    private void assertHistorikkinnslagFelt(HistorikkinnslagDel del, HistorikkEndretFeltType historikkEndretFeltType, String fraVerdi, String tilVerdi) {
        Optional<HistorikkinnslagFelt> feltOpt = del.getEndretFelt(historikkEndretFeltType);
        String feltNavn = historikkEndretFeltType.getKode();
        assertThat(feltOpt).hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as(feltNavn + ".navn").isEqualTo(feltNavn);
            assertThat(felt.getFraVerdi()).as(feltNavn + ".fraVerdi").isEqualTo(fraVerdi);
            assertThat(felt.getTilVerdi()).as(feltNavn + ".tilVerdi").isEqualTo(tilVerdi);
        });
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi() {
        //Arrange
        int antallPerioder = 1;
        boolean varigEndring = true;
        lagBehandlingMedBeregningsgrunnlag(antallPerioder);

        //Dto
        VurderVarigEndringEllerNyoppstartetSNDto vurderVarigEndringEllerNyoppstartetSNDto = new VurderVarigEndringEllerNyoppstartetSNDto("begrunnelse", varigEndring);
        FastsettBruttoBeregningsgrunnlagSNDto fastsettBGDto = new FastsettBruttoBeregningsgrunnlagSNDto("begrunnelse", BRUTTO_BG);

        // Act
        vurderVarigEndringEllerNyoppstartetSNOppdaterer.oppdater(vurderVarigEndringEllerNyoppstartetSNDto, behandling);
        fastsettBruttoBeregningsgrunnlagSNOppdaterer.oppdater(fastsettBGDto, behandling);

        //Assert
        assertBeregningsgrunnlag(antallPerioder);
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_med_overstyrt_verdi_for_fleire_perioder() {
        //Arrange
        int antallPerioder = 3;
        boolean varigEndring = true;
        lagBehandlingMedBeregningsgrunnlag(antallPerioder);

        //Dto
        VurderVarigEndringEllerNyoppstartetSNDto vurderVarigEndringEllerNyoppstartetSNDto = new VurderVarigEndringEllerNyoppstartetSNDto("begrunnelse", varigEndring);
        FastsettBruttoBeregningsgrunnlagSNDto fastsettBGDto = new FastsettBruttoBeregningsgrunnlagSNDto("begrunnelse", BRUTTO_BG);

        // Act
        vurderVarigEndringEllerNyoppstartetSNOppdaterer.oppdater(vurderVarigEndringEllerNyoppstartetSNDto, behandling);
        fastsettBruttoBeregningsgrunnlagSNOppdaterer.oppdater(fastsettBGDto, behandling);

        //Assert
        assertBeregningsgrunnlag(antallPerioder);
    }

    private void assertBeregningsgrunnlag(int antallPerioder) {
        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        assertThat(beregningsgrunnlag).as("beregningsgrunnlag").hasValueSatisfying(bg -> {
            List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder = bg.getBeregningsgrunnlagPerioder();
            assertThat(beregningsgrunnlagPerioder).hasSize(antallPerioder);
            beregningsgrunnlagPerioder.forEach(beregningsgrunnlagPeriode -> {
                List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
                assertThat(beregningsgrunnlagPrStatusOgAndelList).hasSize(1);
                assertThat(beregningsgrunnlagPrStatusOgAndelList.get(0).getBruttoPrÅr().doubleValue()).isEqualTo(BRUTTO_BG);
            });
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private void buildBgPrStatusOgAndel(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build(beregningsgrunnlagPeriode);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag,
                                                                                                                                                   LocalDate fom,
                                                                                                                                                   LocalDate tom) {
        return no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBeregningsgrunnlag(int antallPerioder) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();

        for (int i = 0; i < antallPerioder; i++) {
            LocalDate fom = LocalDate.now().minusDays(20).plusDays(i*5).plusDays(i==0 ? 0 : 1);
            no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
                fom, fom.plusDays(5));
            buildBgPrStatusOgAndel(bgPeriode);
        }
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }
}
