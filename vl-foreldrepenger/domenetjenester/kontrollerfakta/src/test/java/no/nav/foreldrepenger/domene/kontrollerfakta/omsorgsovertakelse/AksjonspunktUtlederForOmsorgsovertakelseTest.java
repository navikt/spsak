package no.nav.foreldrepenger.domene.kontrollerfakta.omsorgsovertakelse;

import static no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType.ADOPTERER_ALENE;
import static no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType.ANDRE_FORELDER_DØD;
import static no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType.OVERTATT_OMSORG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.kontrollerfakta.omsorgsovertakelse.AksjonspunktUtlederForOmsorgsovertakelse;

public class AksjonspunktUtlederForOmsorgsovertakelseTest {

    private static final LocalDate FØDSELSDATO_BARN = LocalDate.of(2017, Month.JANUARY, 1);
    private AksjonspunktUtlederForOmsorgsovertakelse aksjonspunktUtleder;

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_engangsstønad_til_far() {
        List<AksjonspunktResultat> overtattOmsorg = aksjonspunktForFakta(OVERTATT_OMSORG);
        assertThat(overtattOmsorg.size()).isEqualTo(1);
        assertThat(overtattOmsorg.get(0).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE);

        List<AksjonspunktResultat> andreForelderDød = aksjonspunktForFakta(ANDRE_FORELDER_DØD);
        assertThat(andreForelderDød.size()).isEqualTo(1);
        assertThat(andreForelderDød.get(0).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE);

        assertThat(aksjonspunktForFakta(ADOPTERER_ALENE)).isEmpty();
    }

    private List<AksjonspunktResultat> aksjonspunktForFakta(FarSøkerType farSøkerType) {
        Behandling behandling = byggBehandling(farSøkerType);
        return aksjonspunktUtleder.utledAksjonspunkterFor(behandling);
    }

    private Behandling byggBehandling(FarSøkerType farSøkerType) {

        ScenarioFarSøkerEngangsstønad farSøkerAdopsjonScenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();

        farSøkerAdopsjonScenario.medSøknad()
            .medFarSøkerType(farSøkerType);
        farSøkerAdopsjonScenario.medSøknadHendelse()
            .medFødselsDato(FØDSELSDATO_BARN);
        if (farSøkerType.equals(FarSøkerType.ADOPTERER_ALENE)) {
            farSøkerAdopsjonScenario.medSøknadHendelse()
                .medAdopsjon(farSøkerAdopsjonScenario.medSøknadHendelse().getAdopsjonBuilder()
                    .medAdoptererAlene(true));
        }

        final Behandling behandling = farSøkerAdopsjonScenario.lagMocked();
        final BehandlingRepositoryProvider repositoryProvider = farSøkerAdopsjonScenario.mockBehandlingRepositoryProvider();
        aksjonspunktUtleder = new AksjonspunktUtlederForOmsorgsovertakelse(repositoryProvider);
        return behandling;
    }

}
