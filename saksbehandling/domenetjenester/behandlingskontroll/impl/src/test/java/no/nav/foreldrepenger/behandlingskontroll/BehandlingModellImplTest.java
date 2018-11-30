package no.nav.foreldrepenger.behandlingskontroll;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTypeStegSekvens;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkFraJson;

@SuppressWarnings("resource")
public class BehandlingModellImplTest {
    
    @Test
    public void skal_bygge_BehandlingModell_fra_eksportert_kodeverk_for_enklere_tester_senere() throws Exception {
        List<BehandlingTypeStegSekvens> stegSekvens = new KodeverkFraJson().lesKodeverkFraFil(BehandlingTypeStegSekvens.class);
        
        /* dummy lookup, uten CDI siden disse ikke er på classpath i denne modulen, kan i senere tester benytte CDI (så trenger ikke definere denne) */
        TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> lookup = (t, u, v) -> Mockito.mock(BehandlingSteg.class);
        
        BehandlingModellImpl modellImpl = new BehandlingModellImpl(
                BehandlingType.FØRSTEGANGSSØKNAD,
                FagsakYtelseType.FORELDREPENGER,
                lookup);

        modellImpl.leggTil(stegSekvens);

        Assertions.assertThat(modellImpl.finnNesteSteg(BehandlingStegType.FORESLÅ_VEDTAK).getBehandlingStegType())
                .isEqualTo(BehandlingStegType.FATTE_VEDTAK);

    }
}
