package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class ForeslåVedtakInnsynStegImplTest {

    @Test
    public void skal_gi_aksjonspunkt_for_å_manuelt_foreslå_vedtak_innsyn() throws Exception {
        ForeslåVedtakInnsynStegImpl steg = new ForeslåVedtakInnsynStegImpl();
        BehandleStegResultat resultat = steg.utførSteg(null);

        assertThat(resultat.getAksjonspunktListe()).containsOnly(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
    }
}
