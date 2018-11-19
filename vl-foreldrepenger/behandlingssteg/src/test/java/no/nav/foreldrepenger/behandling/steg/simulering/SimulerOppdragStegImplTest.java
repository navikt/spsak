package no.nav.foreldrepenger.behandling.steg.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;

public class SimulerOppdragStegImplTest {

    private SimulerOppdragSteg steg;
    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void setup() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
    }

    @Test
    public void skalReturnereUtenAksjonspunkterNårFeatureDisabled() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.disableAll();
        steg = new SimulerOppdragStegImpl(repositoryProvider, fakeUnleash);

        BehandlingskontrollKontekst kontekst = mock(BehandlingskontrollKontekst.class);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getAksjonspunktListe()).isEmpty();
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
    }

    @Test
    public void skalSettesPåVentNårFeatureEnabled() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable("fpsak.simuler-oppdrag");
        steg = new SimulerOppdragStegImpl(repositoryProvider, fakeUnleash);

        BehandlingskontrollKontekst kontekst = mock(BehandlingskontrollKontekst.class);
        BehandleStegResultat resultat = steg.utførSteg(kontekst);
        assertThat(resultat.getAksjonspunktListe()).isEmpty();
        assertThat(resultat.getTransisjon()).isEqualTo(FellesTransisjoner.SETT_PÅ_VENT);
    }
}
