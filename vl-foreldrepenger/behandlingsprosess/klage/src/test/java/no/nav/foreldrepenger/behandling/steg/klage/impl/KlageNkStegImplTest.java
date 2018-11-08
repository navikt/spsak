package no.nav.foreldrepenger.behandling.steg.klage.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;

public class KlageNkStegImplTest {

    private KlageNkStegImpl steg;
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void setUp() {
        kontekst = mock(BehandlingskontrollKontekst.class);
    }


    @Test
    public void skalOppretteAksjonspunktKlageNKVedYtelsesStadfestet() {
        // Arrange
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forStadfestetNFP(ScenarioMorSøkerEngangsstønad.forAdopsjon());
        Behandling klageBehandling = scenario.lagMocked();

        steg = new KlageNkStegImpl(scenario.mockBehandlingRepository());
        when(kontekst.getBehandlingId()).thenReturn(klageBehandling.getId());
        // Act
        BehandleStegResultat behandlingStegResultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(behandlingStegResultat).isNotNull();
        assertThat(behandlingStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandlingStegResultat.getAksjonspunktListe().size()).isEqualTo(1);

        AksjonspunktDefinisjon aksjonspunktDefinisjon = behandlingStegResultat.getAksjonspunktListe().get(0);
        assertThat(aksjonspunktDefinisjon).isEqualTo(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NK);
    }

    @Test
    public void skalUtføreUtenAskjonspunktNårBehandlingsresultatTypeIkkeErYtelsesStadfestet() {
        // Arrange
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(ScenarioMorSøkerEngangsstønad.forAdopsjon());
        Behandling klageBehandling = scenario.lagMocked();

        steg = new KlageNkStegImpl(scenario.mockBehandlingRepository());
        when(kontekst.getBehandlingId()).thenReturn(klageBehandling.getId());

        // Act
        BehandleStegResultat behandlingStegResultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(behandlingStegResultat).isNotNull();
        assertThat(behandlingStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandlingStegResultat.getAksjonspunktListe()).isEmpty();
    }

    @Test
    public void skalOverhoppBakoverRyddeKlageVurderingRestultat() {
        // Arrange
        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forAvvistNK(ScenarioMorSøkerEngangsstønad.forAdopsjon());
        Behandling klageBehandling = scenario.lagMocked();
        BehandlingRepository behandlingRepository = scenario.mockBehandlingRepository();

        steg = new KlageNkStegImpl(behandlingRepository);
        // Act
        steg.vedTransisjon(kontekst, klageBehandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        verify(behandlingRepository).slettKlageVurderingResultat(eq(klageBehandling), any(), eq(KlageVurdertAv.NK));
    }

}
