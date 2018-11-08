package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.FaktaOmBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.TilstøtendeYtelseDto;

public class FaktaOmBeregningDtoTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private FaktaOmBeregningDtoTjeneste faktaOmBeregningDtoTjeneste;
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = mock(FaktaOmBeregningTilfelleTjeneste.class);
    private TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = mock(TilstøtendeYtelseDtoTjeneste.class);

    private Behandling behandling;
    private AksjonspunktRepository aksjonspunktRepository;


    @Before
    public void setUp() {
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(BeregningIAYTestUtil.AKTØR_ID)
            .medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING));
        Beregningsgrunnlag bg = lagBeregningsgrunnlag(scenario);
        behandling = scenario.lagre(repositoryProvider);
        List<FaktaOmBeregningTilfelle> tilfeller = Collections.singletonList(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE);
        when(faktaOmBeregningTilfelleTjeneste.finnTilfellerForFellesAksjonspunkt(behandling)).thenReturn(tilfeller);
        TilstøtendeYtelseDto tyDto = new TilstøtendeYtelseDto();
        tyDto.setArbeidskategori(Arbeidskategori.ARBEIDSTAKER);
        when(tilstøtendeYtelseDtoTjeneste.lagTilstøtendeYtelseDto(behandling,bg)).thenReturn(Optional.of(tyDto));
        faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(null, faktaOmBeregningTilfelleTjeneste, null, tilstøtendeYtelseDtoTjeneste, null, null);
    }


    @Test
    public void skal_ikkje_lage_fakta_om_beregning_dto_når_man_ikkje_har_aksjonspunkt_i_fakta_om_beregning() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD);
        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, repositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling));
        assertThat(dto.isPresent()).isFalse();
    }

    @Test
    public void skal_lage_fakta_om_beregning_dto_når_man_har_aksjonspunkt_i_fakta_om_beregning() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
        Optional<FaktaOmBeregningDto> dto = faktaOmBeregningDtoTjeneste.lagFaktaOmBeregningDto(behandling, repositoryProvider.getBeregningsgrunnlagRepository().hentAggregat(behandling));
        assertThat(dto.isPresent()).isTrue();
    }


    private Beregningsgrunnlag lagBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario) {
        return scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(90000))
            .build();
    }
}
