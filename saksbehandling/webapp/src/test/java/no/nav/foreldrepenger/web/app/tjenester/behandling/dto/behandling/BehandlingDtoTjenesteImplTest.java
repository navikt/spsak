package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.foreldrepenger.web.app.rest.ResourceLink;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BehandlingDtoTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private GrunnlagRepositoryProvider repositoryProvider;

    @Inject
    private ResultatRepositoryProvider resultatRepositoryProvider;

    @Inject
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    private FakeUnleash unleash = new FakeUnleash();

    private BehandlingDtoTjeneste tjeneste;

    @Before
    public void setUp() {
        System.setProperty("fpoppdrag.url", "https://foo/bar");
        tjeneste = new BehandlingDtoTjenesteImpl(resultatRepositoryProvider, skjæringstidspunktTjeneste, unleash);
    }

    @After
    public void tearDown() {
        System.clearProperty("fpoppdrag.url");
    }

    @Test
    public void skal_ha_med_simuleringsresultatURL_når_feature_er_skrudd_på() {
        unleash.enable("fpsak.simuler-oppdrag");
        Behandling behandling = lagBehandling();

        UtvidetBehandlingDto dto = tjeneste.lagUtvidetBehandlingDto(behandling, null);

        assertThat(dto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).contains("simuleringResultat");
        assertThat(dto.getLinks().stream().map(ResourceLink::getHref).collect(Collectors.toList())).contains(URI.create("https://foo/bar/fpoppdrag/api/simulering/resultat"));
    }

    @Test
    public void skal_ikke_ha_med_simuleringsresultatURL_når_feature_er_skrudd_på() {
        unleash.disable("fpsak.simuler-oppdrag");
        Behandling behandling = lagBehandling();

        UtvidetBehandlingDto dto = tjeneste.lagUtvidetBehandlingDto(behandling, null);

        assertThat(dto.getLinks().stream().map(ResourceLink::getRel).collect(Collectors.toList())).doesNotContain("simuleringResultat");
        assertThat(dto.getLinks().stream().map(ResourceLink::getHref).collect(Collectors.toList())).doesNotContain(URI.create("https://foo/bar/fpoppdrag/api/simulering/resultat"));
    }

    private Behandling lagBehandling() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();

        SykemeldingerBuilder smBuilder = scenario.getSykemeldingerBuilder();
        SykemeldingBuilder sykemeldingBuilder = smBuilder.sykemeldingBuilder("ASDF-ASDF-ASDF");
        sykemeldingBuilder.medPeriode(LocalDate.now(), LocalDate.now().plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(new AktørId(1234L)))
            .medGrad(new Prosentsats(100));
        smBuilder.medSykemelding(sykemeldingBuilder);
        scenario.medSykemeldinger(smBuilder);
        return scenario
            .lagre(repositoryProvider, resultatRepositoryProvider);
    }
}
