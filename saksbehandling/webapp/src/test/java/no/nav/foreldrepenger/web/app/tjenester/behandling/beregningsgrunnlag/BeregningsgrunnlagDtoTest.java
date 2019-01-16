package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.SatsType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoUtil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.EndringBeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BeregningsgrunnlagDtoTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjeneste(repositoryProvider,null, null, null, mock, apOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjeneste(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, null);
    private final HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = mock(HentGrunnlagsdataTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste  = new KontrollerFaktaBeregningFrilanserTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;
    @Inject
    BeregningsgrunnlagDtoUtil dtoUtil;
    private BeregningOpptjeningTestUtil opptjeningTestUtil;

    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private BigDecimal grunnbeløp;
    private Behandling behandling;

    private static final LocalDate SKJAERINGSTIDSPUNKT = LocalDate.now().minusDays(100);
    private static final String ORGNR = "2132313";

    @Before
    public void setup() {
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjeneste(repositoryProvider, inntektArbeidYtelseTjeneste);
        this.kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        SatsRepository beregningRepository = repositoryProvider.getSatsRepository();
        grunnbeløp = BigDecimal.valueOf(beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, SKJAERINGSTIDSPUNKT).getVerdi());
        scenario.medBeregningsgrunnlag().medSkjæringstidspunkt(SKJAERINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJAERINGSTIDSPUNKT)
            .medGrunnbeløp(grunnbeløp)
            .medRedusertGrunnbeløp(grunnbeløp);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        leggTilOpptjening();
        EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste = new EndringBeregningsgrunnlagDtoTjeneste(kontrollerFaktaBeregningTjeneste, dtoUtil, resultatRepositoryProvider.getBeregningsgrunnlagRepository());
        TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = new TilstøtendeYtelseDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, opptjeningInntektArbeidYtelseTjeneste, dtoUtil);
        FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste = new FaktaOmBeregningAndelDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste, dtoUtil, resultatRepositoryProvider.getBeregningsgrunnlagRepository());
        FaktaOmBeregningDtoTjenesteImpl faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste,
            faktaOmBeregningTilfelleTjeneste,
            endringBeregningsgrunnlagDtoTjeneste, tilstøtendeYtelseDtoTjeneste, faktaOmBeregningAndelDtoTjeneste, null);
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjenesteImpl(repositoryProvider, resultatRepositoryProvider, faktaOmBeregningDtoTjeneste, null);
    }

    private void leggTilOpptjening() {
        Map<String, Periode> periodeMap = new HashMap<>();
        periodeMap.put(ORGNR, new Periode(SKJAERINGSTIDSPUNKT.minusYears(1), SKJAERINGSTIDSPUNKT.minusDays(1)));
        opptjeningTestUtil = new BeregningOpptjeningTestUtil(resultatRepositoryProvider, new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository()));
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJAERINGSTIDSPUNKT, periodeMap);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_lages() {

        // Act
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).isPresent();
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_får_korrekte_verdier() {
        // Act
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).hasValueSatisfying(grunnlagDto -> {
            assertThat(grunnlagDto).isNotNull();
            assertThat(grunnlagDto.getSkjaeringstidspunktBeregning()).isEqualTo(SKJAERINGSTIDSPUNKT);
            assertThat(grunnlagDto.getLedetekstAvkortet()).isNotNull();
            assertThat(grunnlagDto.getLedetekstBrutto()).isNotNull();
            assertThat(grunnlagDto.getLedetekstRedusert()).isNull();
            assertThat(grunnlagDto.getHalvG().intValue()).isEqualTo(grunnbeløp.divide(BigDecimal.valueOf(2),  RoundingMode.HALF_UP).intValue());
        });
    }
}
