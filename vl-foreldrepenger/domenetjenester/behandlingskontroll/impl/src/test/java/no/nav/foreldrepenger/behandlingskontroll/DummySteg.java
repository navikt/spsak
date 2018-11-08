package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl.TriFunction;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

class DummySteg implements BehandlingSteg {

    private List<AksjonspunktResultat> aksjonspunkter;
    private boolean tilbakefør;
    private AtomicReference<BehandleStegResultat> sisteUtførStegResultat = new AtomicReference<>();

    public DummySteg() {
        aksjonspunkter = Collections.emptyList();
    }

    public DummySteg(AksjonspunktResultat... aksjonspunkt) {
        aksjonspunkter = Arrays.asList(aksjonspunkt);
    }

    public DummySteg(boolean tilbakefør, AksjonspunktResultat... aksjonspunkt) {
        this.aksjonspunkter = Arrays.asList(aksjonspunkt);
        this.tilbakefør = tilbakefør;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        if (tilbakefør) {
            BehandleStegResultat tilbakeført = BehandleStegResultat
                .tilbakeførtMedAksjonspunkter(aksjonspunkter.stream()
                    .map(AksjonspunktResultat::getAksjonspunktDefinisjon).collect(Collectors.toList()));
            sisteUtførStegResultat.set(tilbakeført);
            return tilbakeført;
        }
        BehandleStegResultat utførtMedAksjonspunkter = BehandleStegResultat.utførtMedAksjonspunktResultater(aksjonspunkter);
        sisteUtførStegResultat.set(utførtMedAksjonspunkter);
        return utførtMedAksjonspunkter;
    }

    public boolean isUtførStegKalt() {
        return sisteUtførStegResultat.get() != null;
    }

    public BehandleStegResultat sisteStegResultat() {
        return sisteUtførStegResultat.get();
    }

    public static TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> map(List<TestStegKonfig> input) {

        Map<List<?>, BehandlingSteg> resolver = new HashMap<>();

        for (TestStegKonfig konfig : input) {
            List<?> key = Arrays.asList(konfig.getBehandlingStegType(), konfig.getBehandlingType(), konfig.getFagsakYtelseType());
            resolver.put(key, konfig.getSteg());
        }

        TriFunction<BehandlingStegType, BehandlingType, FagsakYtelseType, BehandlingSteg> func = (t, u, r) -> resolver.get(Arrays.asList(t, u, r));
        return func;
    }

}
