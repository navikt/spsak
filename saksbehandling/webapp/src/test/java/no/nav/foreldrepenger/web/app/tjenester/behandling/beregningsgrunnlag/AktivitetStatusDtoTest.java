package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
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

public class AktivitetStatusDtoTest {
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final AktivitetStatus STATUS = AktivitetStatus.ARBEIDSTAKER;
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjeneste(repositoryProvider, null, null, null, mock, apOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjeneste(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, null);
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = mock(HentGrunnlagsdataTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste = new KontrollerFaktaBeregningFrilanserTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);
    private Behandling behandling;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private BeregningsgrunnlagDtoUtil beregningsgrunnlagDtoUtil;

    @Before
    public void setup() {
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjeneste(repositoryProvider, inntektArbeidYtelseTjeneste);
        this.kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        lagBeregningsgrunnlagMedStatus(scenario);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        beregningsgrunnlagDtoUtil = new BeregningsgrunnlagDtoUtil(null, beregningsgrunnlagRepository);
        EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste = new EndringBeregningsgrunnlagDtoTjeneste(kontrollerFaktaBeregningTjeneste, beregningsgrunnlagDtoUtil,
            beregningsgrunnlagRepository);
        TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = new TilstøtendeYtelseDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, opptjeningInntektArbeidYtelseTjeneste, null);
        FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste = new FaktaOmBeregningAndelDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste, beregningsgrunnlagDtoUtil,
            beregningsgrunnlagRepository);
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste);
        FaktaOmBeregningDtoTjenesteImpl faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, faktaOmBeregningTilfelleTjeneste,
            endringBeregningsgrunnlagDtoTjeneste, tilstøtendeYtelseDtoTjeneste, faktaOmBeregningAndelDtoTjeneste, beregningsgrunnlagDtoUtil);
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjenesteImpl(repositoryProvider, resultatRepositoryProvider, faktaOmBeregningDtoTjeneste, beregningsgrunnlagDtoUtil);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_aktivitetStatus_får_korrekte_verdier() {
        //Act
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).hasValueSatisfying(grunnlagDto -> {
            List<AktivitetStatus> aktivitetStatus = grunnlagDto.getAktivitetStatus();
            assertThat(aktivitetStatus).isNotNull();
            assertThat(aktivitetStatus.get(0)).isEqualTo(STATUS);
        });
    }

    private void lagBeregningsgrunnlagMedStatus(ScenarioMorSøkerForeldrepenger scenario) {
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .build();
        buildBeregningsgrunnlagAktivitetStatus(beregningsgrunnlag, STATUS);
    }


    private void buildBeregningsgrunnlagAktivitetStatus(Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus status) {
        BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(status)
            .build(beregningsgrunnlag);
    }

}
