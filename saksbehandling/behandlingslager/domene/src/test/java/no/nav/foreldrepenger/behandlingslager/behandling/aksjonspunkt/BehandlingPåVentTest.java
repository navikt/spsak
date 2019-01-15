package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_OPPTJENINGSOPPLYSNINGER;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class BehandlingPåVentTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Fagsak fagsak;

    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(repoRule.getEntityManager());

    @Before
    public void setup() {
        fagsak = Fagsak.opprettNy(null);
    }

    @Test
    public void testErIkkePåVentUtenInnslag() {
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        Assert.assertFalse(behandling.isBehandlingPåVent());
    }

    @Test
    public void testErPåVentEttInnslag() {
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AUTO_MANUELT_SATT_PÅ_VENT);
        Assert.assertTrue(behandling.isBehandlingPåVent());
    }

    @Test
    public void testErIkkePåVentEttInnslag() {
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AUTO_MANUELT_SATT_PÅ_VENT);
        aksjonspunktRepository.setTilUtført(aksjonspunkt, "");
        Assert.assertFalse(behandling.isBehandlingPåVent());
    }

    @Test // TODO PKMANTIS-1137 Har satt midlertidig frist, må endres når dynamisk frist er implementert
    public void testErPåVentNårVenterPåOpptjeningsopplysninger() {
        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AUTO_VENT_PÅ_OPPTJENINGSOPPLYSNINGER);
        Assert.assertTrue(behandling.isBehandlingPåVent());
        Assert.assertEquals(behandling.getOpprettetDato().plusWeeks(2).toLocalDate(), aksjonspunkt.getFristTid().toLocalDate());
        aksjonspunktRepository.setTilUtført(aksjonspunkt, "");
        Assert.assertFalse(behandling.isBehandlingPåVent());
    }
}
