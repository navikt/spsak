package no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon;


import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon.AksjonspunktUtlederForEngangsstønadAdopsjon;

public class AksjonspunktUtlederForEngangsstønadAdopsjonTest {

    private AksjonspunktUtlederForEngangsstønadAdopsjon utleder = new AksjonspunktUtlederForEngangsstønadAdopsjon();

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_engangsstønad_til_far() {
        List<AksjonspunktResultat> aksjonspunktForFaktaForFar = aksjonspunktForFaktaForFar();

        assertThat(aksjonspunktForFaktaForFar.size()).isEqualTo(3);
        assertThat(aksjonspunktForFaktaForFar.get(0).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_ADOPSJONSDOKUMENTAJON);
        assertThat(aksjonspunktForFaktaForFar.get(1).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN);
        assertThat(aksjonspunktForFaktaForFar.get(2).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_OM_SØKER_ER_MANN_SOM_ADOPTERER_ALENE);
    }

    @Test
    public void skal_utledede_aksjonspunkt_basert_på_fakta_om_engangsstønad_til_mor() {
        List<AksjonspunktResultat> aksjonspunktForFaktaForMor = aksjonspunktForFaktaForMor();

        assertThat(aksjonspunktForFaktaForMor.size()).isEqualTo(2);
        assertThat(aksjonspunktForFaktaForMor.get(0).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_ADOPSJONSDOKUMENTAJON);
        assertThat(aksjonspunktForFaktaForMor.get(1).getAksjonspunktDefinisjon()).isEqualTo(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN);
    }

    private List<AksjonspunktResultat> aksjonspunktForFaktaForFar() {
        ScenarioFarSøkerEngangsstønad farSøkerAdopsjonScenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        Behandling behandling = farSøkerAdopsjonScenario.lagMocked();
        return utleder.utledAksjonspunkterFor(behandling);
    }

    private List<AksjonspunktResultat> aksjonspunktForFaktaForMor() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();
        return utleder.utledAksjonspunkterFor(behandling);
    }

}
