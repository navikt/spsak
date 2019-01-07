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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderVarigEndringEllerNyoppstartetSNDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class VurderVarigEndringEllerNyoppstartetSNOppdatererTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = new BeregningsgrunnlagRepositoryImpl(repositoryRule.getEntityManager(), repositoryProvider.getBehandlingLåsRepository());
    private Behandling behandling;
    private ScenarioMorSøkerForeldrepenger scenario;

    @Before
    public void setup() {
        scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_varig_endring_SN() {
        // Arrange
        int antallPerioder = 1;
        boolean varigEndring = false;
        lagBehandlingMedBeregningsgrunnlag(scenario, antallPerioder);

        //Dto
        VurderVarigEndringEllerNyoppstartetSNDto dto = new VurderVarigEndringEllerNyoppstartetSNDto("begrunnelse", varigEndring);

        // Act
        new VurderVarigEndringEllerNyoppstartetSNOppdaterer(repositoryProvider, resultatRepositoryProvider, lagMockHistory())
            .oppdater(dto, behandling);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslag = tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkInnslag).hasSize(1);
        HistorikkinnslagDel del = historikkInnslag.get(0);
        assertThat(del.getBegrunnelse()).hasValueSatisfying(begrunnelse -> assertThat(begrunnelse).isEqualTo("begrunnelse"));
        List<HistorikkinnslagFelt> feltList = del.getEndredeFelt();
        assertThat(feltList).hasSize(1);
        assertFelt(del, HistorikkEndretFeltType.ENDRING_NAERING, null, HistorikkEndretFeltVerdiType.INGEN_VARIG_ENDRING_NAERING.getKode());
    }

    @Test
    public void skal_oppdatere_beregningsgrunnlag_ved_varig_endring_SN_for_fleire_perioder() {
        // Arrange
        int antallPerioder = 3;
        boolean varigEndring = false;
        lagBehandlingMedBeregningsgrunnlag(scenario, antallPerioder);

        //Dto
        VurderVarigEndringEllerNyoppstartetSNDto dto = new VurderVarigEndringEllerNyoppstartetSNDto("begrunnelse", varigEndring);

        // Act
        new VurderVarigEndringEllerNyoppstartetSNOppdaterer(repositoryProvider, resultatRepositoryProvider, lagMockHistory())
            .oppdater(dto, behandling);

        // Assert
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
                assertThat(beregningsgrunnlagPrStatusOgAndelList.get(0).getBruttoPrÅr()).isNull();
            });
        });
    }

    private void assertFelt(HistorikkinnslagDel del, HistorikkEndretFeltType historikkEndretFeltType, String fraVerdi, String tilVerdi) {
        Optional<HistorikkinnslagFelt> feltOpt = del.getEndretFelt(historikkEndretFeltType);
        String feltNavn = historikkEndretFeltType.getKode();
        assertThat(feltOpt).hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as(feltNavn + ".navn").isEqualTo(feltNavn);
            assertThat(felt.getFraVerdi()).as(feltNavn + ".fraVerdi").isEqualTo(fraVerdi);
            assertThat(felt.getTilVerdi()).as(feltNavn + ".tilVerdi").isEqualTo(tilVerdi);
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private void lagBehandlingMedBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario, int antallPerioder) {
        Beregningsgrunnlag.Builder grunnlagBuilder = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);

        for (int i = 0; i < antallPerioder; i++) {
            LocalDate fom = LocalDate.now().minusDays(20).plusDays(i*5).plusDays(i==0 ? 0 : 1);
            BeregningsgrunnlagPeriode.Builder bgPeriodeBuilder = lagBeregningsgrunnlagPeriodeBuilder(fom, fom.plusDays(5));
            grunnlagBuilder.leggTilBeregningsgrunnlagPeriode(bgPeriodeBuilder);
        }
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    private BeregningsgrunnlagPeriode.Builder lagBeregningsgrunnlagPeriodeBuilder(LocalDate fom, LocalDate tom) {
        BeregningsgrunnlagPrStatusOgAndel.Builder prStatusOgAndelBuilder = lagPrStatusOgAndelBuilder();
        BeregningsgrunnlagPeriode.Builder bgPeriodeBuilder = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fom, tom)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(prStatusOgAndelBuilder);
        return bgPeriodeBuilder;
    }

    private BeregningsgrunnlagPrStatusOgAndel.Builder lagPrStatusOgAndelBuilder() {
        return BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold
                .builder()
                .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                .medArbeidsperiodeTom(LocalDate.now().plusYears(2)))
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

}
