package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class AksjonspunktApplikasjonTjenesteImplTest {

    private static final String BEGRUNNELSE = "begrunnelse";

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    private AbstractTestScenario<?> lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.leggTilAksjonspunkt(aksjonspunktDefinisjon, BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        return scenario;
    }

    @Test
    public void skal_ikke_sette_ansvarlig_saksbehandler_hvis_bekreftet_aksjonspunkt_er_fatter_vedtak() {
        // Arrange
        // Bruker BekreftTerminbekreftelseAksjonspunktDto som konkret case
        AksjonspunktApplikasjonTjenesteImpl aksjonspunktApplikasjonTjenesteImpl = (AksjonspunktApplikasjonTjenesteImpl) aksjonspunktApplikasjonTjeneste;
        AbstractTestScenario<?> scenario = lagScenarioMedAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE);
        Behandling behandling = scenario.lagre(repositoryProvider);
        Behandling behandlingSpy = spy(behandling);

        FatterVedtakAksjonspunktDto dto = new FatterVedtakAksjonspunktDto(BEGRUNNELSE, Collections.emptyList());

        // Act
        aksjonspunktApplikasjonTjenesteImpl.setAnsvarligSaksbehandler(singletonList(dto), behandlingSpy);

        // Assert
        verify(behandlingSpy, never()).setAnsvarligSaksbehandler(any());
    }
}
