package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.EndringBeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.SammenligningsgrunnlagDto;

public class SammenligningsgrunnlagDtoTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, mock);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, mock, apOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, null);
    private final HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = mock(HentGrunnlagsdataTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste  = new KontrollerFaktaBeregningFrilanserTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);

    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private Behandling behandling;

    private static final BigDecimal RAPPORTERT_PR_AAR = BigDecimal.valueOf(300000);
    private static final long AVVIK = 500L;
    private static final LocalDate SAMMENLIGNING_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate SAMMENLIGNING_TOM = LocalDate.now();
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now().minusDays(5);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);

    @Before
    public void setup() {
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        this.kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        Beregningsgrunnlag bg = scenario.medBeregningsgrunnlag().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP).build();

        Sammenligningsgrunnlag.builder()
            .medSammenligningsperiode(SAMMENLIGNING_FOM, SAMMENLIGNING_TOM)
            .medRapportertPrÅr(RAPPORTERT_PR_AAR)
            .medAvvikPromille(AVVIK).build(bg);

        behandling = scenario.lagre(repositoryProvider);
        EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste = new EndringBeregningsgrunnlagDtoTjeneste(kontrollerFaktaBeregningTjeneste, repositoryProvider, null);
        TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = new TilstøtendeYtelseDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, opptjeningInntektArbeidYtelseTjeneste, null);
        FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste = new FaktaOmBeregningAndelDtoTjenesteImpl(repositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste,null );
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(repositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste);
        FaktaOmBeregningDtoTjenesteImpl faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste,
            faktaOmBeregningTilfelleTjeneste,
            endringBeregningsgrunnlagDtoTjeneste, tilstøtendeYtelseDtoTjeneste, faktaOmBeregningAndelDtoTjeneste, null);
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjenesteImpl(repositoryProvider, faktaOmBeregningDtoTjeneste, null);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_sammenligningsgrunnlag_får_korrekte_verdier() {
        // Act

        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).hasValueSatisfying(beregningsgrunnlagDto -> {
            SammenligningsgrunnlagDto sammenligningsgrunnlag = beregningsgrunnlagDto.getSammenligningsgrunnlag();
            assertThat(sammenligningsgrunnlag).isNotNull();
            assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(AVVIK);
            assertThat(sammenligningsgrunnlag.getRapportertPrAar()).isEqualTo(RAPPORTERT_PR_AAR);
            assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagFom()).isEqualTo(SAMMENLIGNING_FOM);
            assertThat(sammenligningsgrunnlag.getSammenligningsgrunnlagTom()).isEqualTo(SAMMENLIGNING_TOM);
        });
    }

}
