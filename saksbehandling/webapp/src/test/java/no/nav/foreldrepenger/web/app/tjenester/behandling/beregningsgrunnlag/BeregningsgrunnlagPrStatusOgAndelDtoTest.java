package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
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
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
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
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelDto;

public class BeregningsgrunnlagPrStatusOgAndelDtoTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private VirksomhetTjeneste virksomhetTjeneste = mock(VirksomhetTjeneste.class);
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, mock);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjeneste(repositoryProvider, null,null, null, mock, apOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjeneste(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, null);
    private final HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = mock(HentGrunnlagsdataTjeneste.class);
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;
    private KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilanserTjeneste  = new KontrollerFaktaBeregningFrilanserTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);
    private BeregningsgrunnlagDtoUtil beregningsgrunnlagDtoUtil;

    @Mock
    private TpsTjeneste tpsTjenesteMock;

    private Behandling behandling;
    private VirksomhetEntitet virksomhet;
    private BeregningsgrunnlagDtoTjeneste beregningsgrunnlagDtoTjeneste;

    private static final Inntektskategori INNTEKTSKATEGORI = Inntektskategori.ARBEIDSTAKER;
    private static final BigDecimal AVKORTET_PR_AAR = BigDecimal.valueOf(150000);
    private static final BigDecimal BRUTTO_PR_AAR = BigDecimal.valueOf(300000);
    private static final BigDecimal REDUSERT_PR_AAR = BigDecimal.valueOf(500000);
    private static final BigDecimal OVERSTYRT_PR_AAR = BigDecimal.valueOf(500);
    private static final LocalDate ANDEL_FOM = LocalDate.now().minusDays(100);
    private static final LocalDate ANDEL_TOM = LocalDate.now();
    private static final String ORGNR = "567";
    private static final Long ANDELSNR = 1L;
    private static final String PRIVATPERSON_NAVN = "Skrue McDuck";
    private static final String PRIVATPERSON_IDENT = "9988776655443";
    @Before
    public void setup() {
        initMocks(this);
        when(tpsTjenesteMock.hentBrukerForAktør(Mockito.any(AktørId.class))).thenReturn(Optional.of(lagPersoninfo()));
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjeneste(repositoryProvider, inntektArbeidYtelseTjeneste);
        this.kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(ORGNR)
                .medNavn("VirksomhetNavn")
                .oppdatertOpplysningerNå()
                .build();
        repositoryProvider.getVirksomhetRepository().lagre(virksomhet);

        BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        beregningsgrunnlagDtoUtil = new BeregningsgrunnlagDtoUtil(tpsTjenesteMock, beregningsgrunnlagRepository);
        EndringBeregningsgrunnlagDtoTjeneste endringBeregningsgrunnlagDtoTjeneste = new EndringBeregningsgrunnlagDtoTjeneste(kontrollerFaktaBeregningTjeneste, beregningsgrunnlagDtoUtil, beregningsgrunnlagRepository);
        TilstøtendeYtelseDtoTjeneste tilstøtendeYtelseDtoTjeneste = new TilstøtendeYtelseDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, opptjeningInntektArbeidYtelseTjeneste, beregningsgrunnlagDtoUtil);
        FaktaOmBeregningAndelDtoTjeneste faktaOmBeregningAndelDtoTjeneste = new FaktaOmBeregningAndelDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste, beregningsgrunnlagDtoUtil, beregningsgrunnlagRepository);
        FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilanserTjeneste);
        FaktaOmBeregningDtoTjenesteImpl faktaOmBeregningDtoTjeneste = new FaktaOmBeregningDtoTjenesteImpl(kontrollerFaktaBeregningTjeneste, faktaOmBeregningTilfelleTjeneste,
            endringBeregningsgrunnlagDtoTjeneste, tilstøtendeYtelseDtoTjeneste, faktaOmBeregningAndelDtoTjeneste, beregningsgrunnlagDtoUtil);
        beregningsgrunnlagDtoTjeneste = new BeregningsgrunnlagDtoTjenesteImpl(repositoryProvider, resultatRepositoryProvider, faktaOmBeregningDtoTjeneste, beregningsgrunnlagDtoUtil);
        when(virksomhetTjeneste.hentOgLagreOrganisasjon(any(String.class))).thenReturn(null);

    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier() {
        lagBehandlingMedBG(Arbeidsgiver.virksomhet(virksomhet));
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).hasValueSatisfying(beregningsgrunnlagDto -> {
            List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
            assertThat(beregningsgrunnlagPeriodeDtoList.size()).isEqualTo(1);

            BeregningsgrunnlagPeriodeDto periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
            assertThat(andelList.size()).isEqualTo(1);
            BeregningsgrunnlagPrStatusOgAndelDto andelDto = andelList.get(0);
            assertThat(andelDto.getInntektskategori()).isEqualByComparingTo(INNTEKTSKATEGORI);
            assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
            assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
            assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
            assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
            assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
            assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
            assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
            assertThat(andelDto.getArbeidsforhold()).isNotNull();
            assertThat(andelDto.getArbeidsforhold().getArbeidsgiverNavn()).isEqualTo(virksomhet.getNavn());
            assertThat(andelDto.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(virksomhet.getOrgnr());

        });
    }

    @Test
    public void skal_teste_at_beregningsgrunnlagDto_beregningsgrunnlagperiode_får_korrekte_verdier_ved_arbeidsgiver_privatperson() {
        lagBehandlingMedBG(Arbeidsgiver.person(new AktørId("123123123123")));
        Optional<BeregningsgrunnlagDto> beregningsgrunnlagDtoOpt = beregningsgrunnlagDtoTjeneste.lagBeregningsgrunnlagDto(behandling);

        // Assert
        assertThat(beregningsgrunnlagDtoOpt).hasValueSatisfying(beregningsgrunnlagDto -> {
            List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriodeDtoList = beregningsgrunnlagDto.getBeregningsgrunnlagPeriode();
            assertThat(beregningsgrunnlagPeriodeDtoList.size()).isEqualTo(1);

            BeregningsgrunnlagPeriodeDto periodeDto = beregningsgrunnlagPeriodeDtoList.get(0);
            List<BeregningsgrunnlagPrStatusOgAndelDto> andelList = periodeDto.getBeregningsgrunnlagPrStatusOgAndel();
            assertThat(andelList.size()).isEqualTo(1);
            BeregningsgrunnlagPrStatusOgAndelDto andelDto = andelList.get(0);
            assertThat(andelDto.getInntektskategori()).isEqualByComparingTo(INNTEKTSKATEGORI);
            assertThat(andelDto.getAndelsnr()).isEqualTo(ANDELSNR);
            assertThat(andelDto.getAvkortetPrAar()).isEqualTo(AVKORTET_PR_AAR);
            assertThat(andelDto.getRedusertPrAar()).isEqualTo(REDUSERT_PR_AAR);
            assertThat(andelDto.getBruttoPrAar()).isEqualTo(OVERSTYRT_PR_AAR);
            assertThat(andelDto.getBeregnetPrAar()).isEqualTo(BRUTTO_PR_AAR);
            assertThat(andelDto.getBeregningsgrunnlagFom()).isEqualTo(ANDEL_FOM);
            assertThat(andelDto.getBeregningsgrunnlagTom()).isEqualTo(ANDEL_TOM);
            assertThat(andelDto.getArbeidsforhold()).isNotNull();
            assertThat(andelDto.getArbeidsforhold().getArbeidsgiverNavn()).isEqualTo(PRIVATPERSON_NAVN);
            assertThat(andelDto.getArbeidsforhold().getArbeidsgiverId()).isEqualTo(PRIVATPERSON_IDENT);
        });
    }


    private void lagBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario, Arbeidsgiver arbeidsgiver) {
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(90000))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(90000))
            .build();

       BeregningsgrunnlagPeriode bgPeriode = buildBeregningsgrunnlagPeriode(beregningsgrunnlag);
        buildBgPrStatusOgAndel(bgPeriode, arbeidsgiver);
    }

    private void buildBgPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2))
            .medArbeidsgiver(arbeidsgiver);

        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medInntektskategori(INNTEKTSKATEGORI)
            .medAndelsnr(ANDELSNR)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medBeregningsperiode(ANDEL_FOM, ANDEL_TOM)
            .medBeregnetPrÅr(BRUTTO_PR_AAR)
            .medAvkortetPrÅr(AVKORTET_PR_AAR)
            .medRedusertPrÅr(REDUSERT_PR_AAR)
            .medOverstyrtPrÅr(OVERSTYRT_PR_AAR)
            .build(beregningsgrunnlagPeriode);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode buildBeregningsgrunnlagPeriode(Beregningsgrunnlag beregningsgrunnlag) {
        return no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(ANDEL_FOM, null)
            .build(beregningsgrunnlag);
    }

    private void lagBehandlingMedBG(Arbeidsgiver arbeidsgiver) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS,
            BehandlingStegType.FORESLÅ_BEREGNINGSGRUNNLAG);
        lagBeregningsgrunnlag(scenario, arbeidsgiver);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    private Personinfo lagPersoninfo() {
        Personinfo.Builder b = new Personinfo.Builder()
            .medNavn(PRIVATPERSON_NAVN)
            .medPersonIdent(new PersonIdent(PRIVATPERSON_IDENT))
            .medAktørId(new AktørId("123123123123"))
            .medFødselsdato(LocalDate.now())
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE);
        return b.build();
    }

}
