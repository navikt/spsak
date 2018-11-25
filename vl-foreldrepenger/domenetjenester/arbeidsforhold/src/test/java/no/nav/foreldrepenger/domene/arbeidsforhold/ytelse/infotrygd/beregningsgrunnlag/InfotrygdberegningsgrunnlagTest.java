package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Engangsstoenad;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Foreldrepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.PaaroerendeSykdom;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.informasjon.Sykepenger;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagConsumer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InfotrygdberegningsgrunnlagTest {

    private static final String ORGNR = "1234567890";
    private static final BigDecimal INNTEKT = new BigDecimal("100000");
    private static final String INNTEKTPERIODETYPE = "D";
    private static final InntektPeriodeType INNTEKTPERIODETYPEKODE = InntektPeriodeType.DAGLIG;

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Inject
    private KodeverkRepository kodeverkRepository;

    @Mock
    private InfotrygdBeregningsgrunnlagConsumer infotrygdBeregningsgrunnlagConsumer;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private Behandling behandling;
    private String FNR = "12345678901";
    private LocalDate foedselsdatoPleietrengende = LocalDate.now();
    private LocalDate FOM = LocalDate.now();
    private LocalDate TOM = LocalDate.now().plusDays(10);
    private int INNTEKTSGRUNNLAGPROSENT = 100;
    private BigDecimal INNTEKTSGRUNNLAGPROSENT_BIGDECIMAL = new BigDecimal(INNTEKTSGRUNNLAGPROSENT);
    private LocalDate OPPRINNIDENTDATO = LocalDate.now();
    private int DEKNINSGRAD = 80;
    private BigDecimal DEKNINSGRAD_BIGDECIMAL = new BigDecimal(DEKNINSGRAD);
    private int GRADERING = 50;
    private BigDecimal GRADERING_BIGDECIMAL = new BigDecimal(GRADERING);
    private LocalDate FODSELSDATOBARN = LocalDate.now().minusDays(50);
    private LocalDate IDENTDATO = LocalDate.now().minusDays(5);
    private LocalDate VEDTAK_FOM = LocalDate.now().minusDays(20);
    private LocalDate VEDTAK_TOM = LocalDate.now().minusDays(19);
    private int UTBETALINGSGRAD = 100;

    @Before
    public void setup() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagre(repositoryProvider);
    }




    @Test
    public void testForPaaroerendeSykdom() throws DatatypeConfigurationException, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        PaaroerendeSykdom paaroerendeSykdom = new InfotrygdBeregningsgrunnlagBuilder.PaaroerendeSykdomBuilder().medArbeidsforhold(
            new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().medOrgnr(ORGNR).medInntekt(INNTEKT).medInntektperiodeType(INNTEKTPERIODETYPE).build())
            .medFoedselsdatoPleietrengende(foedselsdatoPleietrengende).medPeriode(FOM, TOM).medIdentdato(LocalDate.now())
            .build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder().medPaaroerendeSykdom(paaroerendeSykdom).build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).first().hasFieldOrPropertyWithValue("foedselsdatoPleietrengende",foedselsdatoPleietrengende);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).first().hasFieldOrPropertyWithValue("fom", FOM);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).first().hasFieldOrPropertyWithValue("tom", TOM);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer().get(0).getArbeidsforhold()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer().get(0).getArbeidsforhold()).first().hasFieldOrPropertyWithValue("orgnr", ORGNR);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer().get(0).getArbeidsforhold()).first().hasFieldOrPropertyWithValue("inntektPeriodeType", INNTEKTPERIODETYPEKODE);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer().get(0).getArbeidsforhold()).first().hasFieldOrPropertyWithValue("inntektForPerioden", INNTEKT);

    }

    @Test
    public void testForSykepenger() throws DatatypeConfigurationException, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        Sykepenger sykepenger = new InfotrygdBeregningsgrunnlagBuilder.SykepengerBuilder().medArbeidsforhold(
            new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().medOrgnr(ORGNR).medInntekt(INNTEKT).medInntektperiodeType(INNTEKTPERIODETYPE).build())
            .medInntektsgrunnlagProsent(INNTEKTSGRUNNLAGPROSENT).medPeriode(FOM, TOM).medIdentdato(LocalDate.now())
            .build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder().medSykepenger(sykepenger).build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).first().hasFieldOrPropertyWithValue("inntektsgrunnlagProsent",INNTEKTSGRUNNLAGPROSENT_BIGDECIMAL);
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).first().hasFieldOrPropertyWithValue("fom", FOM);
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).first().hasFieldOrPropertyWithValue("tom", TOM);
    }

    @Test
    public void testForForeldrePenger() throws DatatypeConfigurationException, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        Foreldrepenger foreldrepenger = new InfotrygdBeregningsgrunnlagBuilder.ForeldrePengerBuilder().medArbeidsforhold(
            new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().medOrgnr(ORGNR).medInntekt(INNTEKT).medInntektperiodeType(INNTEKTPERIODETYPE).build())
            .medOpprinneligIdentDato(OPPRINNIDENTDATO).medPeriode(FOM, TOM).medIdentdato(LocalDate.now())
            .medDekningsgrad(DEKNINSGRAD).medGradering(GRADERING).medFoedselsdatoBarn(FODSELSDATOBARN)
            .build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder().medForeldrepenger(foreldrepenger).build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).first().hasFieldOrPropertyWithValue("opprinneligIdentdato",OPPRINNIDENTDATO);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).first().hasFieldOrPropertyWithValue("dekningsgrad", DEKNINSGRAD_BIGDECIMAL);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).first().hasFieldOrPropertyWithValue("gradering", GRADERING_BIGDECIMAL);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).first().hasFieldOrPropertyWithValue("foedselsdatoBarn", FODSELSDATOBARN);
    }

    @Test
    public void testForEngangsstoenad() throws DatatypeConfigurationException, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        Engangsstoenad engangsstoenad = new InfotrygdBeregningsgrunnlagBuilder.EngangsstoenadBuilder()
            .medIdentdato(IDENTDATO)
            .medPeriode(FOM, TOM)
            .medVedtak(new InfotrygdBeregningsgrunnlagBuilder.VedtakBuilder().medUtbetalingsgrad(UTBETALINGSGRAD).medPeriode(VEDTAK_FOM, VEDTAK_TOM).build())
            .build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder().medEngangstoenad(engangsstoenad).build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).first().hasFieldOrPropertyWithValue("identdato", IDENTDATO);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).first().hasFieldOrPropertyWithValue("fom", FOM);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).first().hasFieldOrPropertyWithValue("tom", TOM);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads().get(0).getVedtak()).hasSize(1);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads().get(0).getVedtak()).first().hasFieldOrPropertyWithValue("fom", VEDTAK_FOM);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads().get(0).getVedtak()).first().hasFieldOrPropertyWithValue("tom", VEDTAK_TOM);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads().get(0).getVedtak()).first().hasFieldOrPropertyWithValue("utbetalingsgrad", UTBETALINGSGRAD);
    }

    @Test
    public void testKardinalitetPåUnderfelter() throws FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        Foreldrepenger foreldrepenger = new InfotrygdBeregningsgrunnlagBuilder.ForeldrePengerBuilder()
            .medArbeidsforhold(new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().build())
            .medArbeidsforhold(new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().build())
            .medArbeidsforhold(new InfotrygdBeregningsgrunnlagBuilder.ArbeidsforholdBuilder().build())
            .medVedtak(new InfotrygdBeregningsgrunnlagBuilder.VedtakBuilder().build())
            .medVedtak(new InfotrygdBeregningsgrunnlagBuilder.VedtakBuilder().build())
            .medVedtak(new InfotrygdBeregningsgrunnlagBuilder.VedtakBuilder().build())
            .build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder().medForeldrepenger(foreldrepenger).build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger().get(0).getVedtak()).hasSize(3);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger().get(0).getArbeidsforhold()).hasSize(3);
    }

    @Test
    public void testKardinalitet() throws FinnGrunnlagListeUgyldigInput, FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListePersonIkkeFunnet {

        Foreldrepenger foreldrepenger = new InfotrygdBeregningsgrunnlagBuilder.ForeldrePengerBuilder().build();
        Engangsstoenad engangsstoenad = new InfotrygdBeregningsgrunnlagBuilder.EngangsstoenadBuilder().build();
        Sykepenger sykepenger = new InfotrygdBeregningsgrunnlagBuilder.SykepengerBuilder().build();
        PaaroerendeSykdom paaroerendeSykdom = new InfotrygdBeregningsgrunnlagBuilder.PaaroerendeSykdomBuilder().build();

        FinnGrunnlagListeResponse build = new InfotrygdBeregningsgrunnlagBuilder()
            .medForeldrepenger(foreldrepenger)
            .medForeldrepenger(foreldrepenger)
            .medPaaroerendeSykdom(paaroerendeSykdom)
            .medPaaroerendeSykdom(paaroerendeSykdom)
            .medSykepenger(sykepenger)
            .medSykepenger(sykepenger)
            .medEngangstoenad(engangsstoenad)
            .medEngangstoenad(engangsstoenad)
            .build();

        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = hentInfotrygdBeregningsgrunnlag(build);
        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).hasSize(2);
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).hasSize(2);
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).hasSize(2);
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).hasSize(2);
    }



    private YtelsesBeregningsgrunnlag hentInfotrygdBeregningsgrunnlag(FinnGrunnlagListeResponse build) throws FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListePersonIkkeFunnet {
        when(infotrygdBeregningsgrunnlagConsumer.finnBeregningsgrunnlagListe(any())).thenReturn(build);
        InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste = new InfotrygdBeregningsgrunnlagTjenesteImpl(infotrygdBeregningsgrunnlagConsumer, kodeverkRepository);

        return infotrygdBeregningsgrunnlagTjeneste.hentGrunnlagListeFull(behandling, FNR, LocalDate.now());
    }


}
