package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.InfotrygdBeregningsgrunnlagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag.YtelsesBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSakOgGrunnlag;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InfotrygdberegningsgrunnlagTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private InfotrygdBeregningsgrunnlagTjeneste infotrygdBeregningsgrunnlagTjeneste;
    @Inject
    @FagsakYtelseTypeRef("FP")
    private IAYRegisterInnhentingFPTjenesteImpl iayRegisterInnhentingTjeneste;

    @Inject
    private InnhentingSamletTjeneste innhentingSamletTjeneste;

    @Inject
    private RegisterKontekst registerKontekst;

    @Before
    public void setup() {
        registerKontekst.intialiser();
        // setter verdien slik at regler blir kjørt
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void kjørerTestHerfraForÅBrukeMockKlasse() {
        TpsPerson mor = TpsTestSett.kvinneUtenBarn().getBruker();
        InfotrygdVedtakTestSett.infotrygdsakStandard(mor.getFnr(), 20L);
        InfotrygdVedtakTestSett.infotrygdsakStandard("26048122867", 20L);
        ScenarioMorSøkerForeldrepenger førstegangsscenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medFødselAdopsjonsdato(Collections.singletonList(LocalDate.now().plusDays(1)))
            .medBruker(mor.getAktørId(), NavBrukerKjønn.KVINNE);
        Behandling behandling = førstegangsscenario.lagre(repositoryProvider);
        final List<OppgittPeriode> søknadsPerioder = Collections.singletonList(OppgittPeriodeBuilder.ny().medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(4)).medPeriodeType(UttakPeriodeType.FORELDREPENGER).build());
        final OppgittFordelingEntitet perioder = new OppgittFordelingEntitet(søknadsPerioder, true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, perioder);
        // Arrange
        YtelsesBeregningsgrunnlag ytelsesBeregningsgrunnlag = infotrygdBeregningsgrunnlagTjeneste.hentGrunnlagListeFull(behandling, "26048122867", LocalDate.now().minusDays(100));

        assertThat(ytelsesBeregningsgrunnlag.getForeldrePenger()).isNotEmpty();
        assertThat(ytelsesBeregningsgrunnlag.getEngangstoenads()).isNotEmpty();
        assertThat(ytelsesBeregningsgrunnlag.getPårørendesykdommer()).isNotEmpty();
        assertThat(ytelsesBeregningsgrunnlag.getSykepenger()).isNotEmpty();


        Interval interval = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling);
        List<InfotrygdSakOgGrunnlag> sammenstiltSakOgGrunnlag = innhentingSamletTjeneste.getSammenstiltSakOgGrunnlag(behandling, mor.getAktørId(), interval, true);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørArbeidBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER).getAktørYtelseBuilder(KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID);
        iayRegisterInnhentingTjeneste.oversettSakGrunnlagTilYtelse(aktørArbeidBuilder, sammenstiltSakOgGrunnlag.get(0));
    }

}
