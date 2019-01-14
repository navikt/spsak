package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.BeregningsgrunnlagDtoUtil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.EndringBeregningsgrunnlagDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningAndelDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.FaktaOmBeregningDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.app.TilstøtendeYtelseDtoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPeriodeDto;

public class BeregningsgrunnlagPeriodeDtoTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, mock, apOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, null);
    private final HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = mock(HentGrunnlagsdataTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private BeregningsgrunnlagDtoUtil beregningsgrunnlagDtoUtil;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste  = new KontrollerFaktaBeregningFrilanserTjenesteImpl(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);

    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;
    private Behandling behandling;
    private VirksomhetEntitet virksomhet;

    private static final BigDecimal AVKORTET_PR_AAR = BigDecimal.valueOf(150000);
    private static final BigDecimal BRUTTO_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal REDUSERT_PR_AAR = BigDecimal.valueOf(500000);
    private static final LocalDate PERIODE_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate PERIODE_TOM = LocalDate.now();
    private static final String ORGNR = "234";

    @Before
    public void setup() {
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        this.kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);

        virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORGNR)
            .medNavn("VirksomhetNavn")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);
        lagBeregningsgrunnlag(scenario);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        beregningsgrunnlagDtoUtil = new BeregningsgrunnlagDtoUtil(null, beregningsgrunnlagRepository);
        EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste = new EndringBeregningsgrunnlagDtoTjeneste(kontrollerFaktaBeregningTjeneste, beregningsgrunnlagDtoUtil, beregningsgrunnlagRepository);
        TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = new TilstøtendeYtelseDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, opptjeningInntektArbeidYtelseTjeneste, beregningsgrunnlagDtoUtil);
        FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste = new FaktaOmBeregningAndelDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste, beregningsgrunnlagDtoUtil, beregningsgrunnlagRepository);
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste);
        FaktaOmBeregningDtoTjenesteImpl faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, faktaOmBeregningTilfelleTjeneste,
            endringBeregningsgrunnlagDtoTjeneste, tilstøtendeYtelseDtoTjeneste, faktaOmBeregningAndelDtoTjeneste, beregningsgrunnlagDtoUtil);
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjenesteImpl(repositoryProvider, resultatRepositoryProvider, faktaOmBeregningDtoTjeneste, beregningsgrunnlagDtoUtil);
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier() {
        // Act
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);
        BeregningsgrunnlagDto grunnlagDto = beregningsgrunnlagDtoOpt.get();

        // Assert
        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriodeDtoList = grunnlagDto.getBeregningsgrunnlagPeriode();
        assertThat(beregningsgrunnlagPeriodeDtoList.size()).isEqualTo(1);

        BeregningsgrunnlagPeriodeDto dto = beregningsgrunnlagPeriodeDtoList.get(0);
        assertThat(dto.getBeregningsgrunnlagPrStatusOgAndel().size()).isEqualTo(1);
        assertThat(dto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
        assertThat(dto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
        assertThat(dto.getBruttoPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(dto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
        assertThat(dto.getBeregningsgrunnlagPeriodeFom()).isEqualTo(PERIODE_FOM);
        assertThat(dto.getBeregningsgrunnlagPeriodeTom()).isEqualTo(PERIODE_TOM);

    }

    private void lagBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario) {
        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

        no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode bgPeriode = buildBeregningsgrunnlagPeriode(
            beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode, false);
    }

    private void buildBgPrStatusOgAndel(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode beregningsgrunnlagPeriode,
                                        boolean erArbeidstaker) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        if (erArbeidstaker) {
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .build(beregningsgrunnlagPeriode);
        } else {
            BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder();
            builder
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medBeregnetPrÅr(BRUTTO_PR_AAR)
                .build(beregningsgrunnlagPeriode);
        }
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlag) {
        return no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(PERIODE_FOM, PERIODE_TOM)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medBruttoPrÅr(BRUTTO_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .build(beregningsgrunnlag);
    }

}
