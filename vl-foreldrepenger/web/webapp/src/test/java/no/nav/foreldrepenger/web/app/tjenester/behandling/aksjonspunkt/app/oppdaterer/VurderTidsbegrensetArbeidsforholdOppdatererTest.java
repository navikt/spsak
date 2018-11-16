package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.Assertions.assertThat;

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
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsgiverHistorikkinnslagTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderFaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderTidsbegrensetArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderteArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class VurderTidsbegrensetArbeidsforholdOppdatererTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();

    private final BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();

    private VurderTidsbegrensetArbeidsforholdOppdaterer vurderTidsbegrensetArbeidsforholdOppdaterer;
    private VurderFaktaOmBeregningOppdaterer vurderFaktaOmBeregningOppdaterer;
    private Behandling behandling;
    private List<VurderteArbeidsforholdDto> tidsbestemteArbeidsforhold;
    private final long FØRSTE_ANDELSNR = 1L;
    private final long ANDRE_ANDELSNR = 2L;
    private final long TREDJE_ANDELSNR = 3L;
    private final LocalDate FOM = LocalDate.now().minusDays(100);
    private final LocalDate TOM = LocalDate.now();
    private final List<VirksomhetEntitet> virksomheter = new ArrayList<>();

    @Before
    public void setup() {
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("123")
                .medNavn("VirksomhetNavn1")
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("456")
                .medNavn("VirksomhetNavn2")
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.add(new VirksomhetEntitet.Builder()
                .medOrgnr("789")
                .medNavn("VirksomhetNavn3")
                .oppdatertOpplysningerNå()
                .build());
        virksomheter.forEach(v -> repositoryProvider.getVirksomhetRepository().lagre(v));
        ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(null);
        vurderTidsbegrensetArbeidsforholdOppdaterer = new VurderTidsbegrensetArbeidsforholdOppdaterer(repositoryProvider, lagMockHistory(), arbeidsgiverHistorikkinnslagTjeneste);
        vurderFaktaOmBeregningOppdaterer = new VurderFaktaOmBeregningOppdaterer(lagMockHistory(), repositoryProvider, arbeidsgiverHistorikkinnslagTjeneste);
        tidsbestemteArbeidsforhold = lagFastsatteAndelerListe();
    }

    private List<VurderteArbeidsforholdDto> lagFastsatteAndelerListe() {

        VurderteArbeidsforholdDto førsteForhold = new VurderteArbeidsforholdDto(
            FØRSTE_ANDELSNR,
            true,
            null
        );

        VurderteArbeidsforholdDto andreForhold = new VurderteArbeidsforholdDto(
            ANDRE_ANDELSNR,
            false,
            null
        );

        VurderteArbeidsforholdDto tredjeForhold = new VurderteArbeidsforholdDto(
            TREDJE_ANDELSNR,
            true,
            null
        );

        return new ArrayList<>(Arrays.asList(førsteForhold, andreForhold, tredjeForhold));
    }


    @Test
    public void skal_markere_korrekte_andeler_som_tidsbegrenset() {
        //Arrange
        lagBehandlingMedBeregningsgrunnlag();

        //Dto
        VurderTidsbegrensetArbeidsforholdDto vurderTidsbegrensetArbeidsforholdDto = new VurderTidsbegrensetArbeidsforholdDto( tidsbestemteArbeidsforhold);
        VurderFaktaOmBeregningDto vurderFaktaOmBeregningDto = new VurderFaktaOmBeregningDto("begrunnelse",
            Collections.singletonList(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD), vurderTidsbegrensetArbeidsforholdDto);

        // Act
        vurderFaktaOmBeregningOppdaterer.oppdater(vurderFaktaOmBeregningDto, behandling);

        //Assert
        Optional<Beregningsgrunnlag> beregningsgrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);

        assertThat(beregningsgrunnlagOpt).hasValueSatisfying(bg -> {
            List<BeregningsgrunnlagPrStatusOgAndel> andeler = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList();
            assertThat(andeler.get(0).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isTrue();
            assertThat(andeler.get(1).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isFalse();
            assertThat(andeler.get(2).getBgAndelArbeidsforhold().get().getErTidsbegrensetArbeidsforhold()).isTrue();
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, VirksomhetEntitet virksomhet) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));
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
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();


        BeregningsgrunnlagPeriode periode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag,
            FOM, TOM);
        buildBgPrStatusOgAndel(periode, virksomheter.get(0));
        buildBgPrStatusOgAndel(periode, virksomheter.get(1));
        buildBgPrStatusOgAndel(periode, virksomheter.get(2));

        behandling = scenario.lagre(repositoryProvider);
    }
}
