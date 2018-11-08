package no.nav.foreldrepenger.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;

public class FinnAnsvarligSaksbehandlerTest {

    private static final String BESLUTTER = "Beslutter";
    private static final String SAKSBEHANDLER = "Saksbehandler";

    private Behandling behandling;

    @Before
    public void setup() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        behandling = scenario.lagMocked();
    }

    @Test
    public void ansvarligSaksbehandlerSettesTilAnsvarligBeslutterNårSatt() {
        // Arrange
        behandling.setAnsvarligSaksbehandler(SAKSBEHANDLER);
        behandling.setAnsvarligBeslutter(BESLUTTER);

        // Act
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);

        // Assert
        assertThat(ansvarligSaksbehandler).isEqualTo(BESLUTTER);
    }

    @Test
    public void ansvarligSaksbehandlerSettesTilAnsvarligSaksbehandlerNårAnsvarligBeslutterIkkeErSatt() {
        // Arrange
        behandling.setAnsvarligSaksbehandler(SAKSBEHANDLER);

        // Act
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);

        // Assert
        assertThat(ansvarligSaksbehandler).isEqualTo(SAKSBEHANDLER);
    }

    @Test
    public void ansvarligSaksbehandlerSettesTilVLNårBeslutterOgSaksbehandlerMangler() {
        // Act
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);

        // Assert
        assertThat(ansvarligSaksbehandler).isEqualTo("VL");

    }
}
