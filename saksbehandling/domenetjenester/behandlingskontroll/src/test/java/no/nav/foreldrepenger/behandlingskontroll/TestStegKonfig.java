package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public class TestStegKonfig {
    private final BehandlingStegType behandlingStegType;
    private final BehandlingType behandlingType;
    private final FagsakYtelseType fagsakYtelseType;
    private final BehandlingSteg steg;
    private final List<AksjonspunktDefinisjon> inngangAksjonspunkter;
    private final List<AksjonspunktDefinisjon> utgangAksjonspunkter;

    public TestStegKonfig(BehandlingStegType behandlingStegType, BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType, BehandlingSteg steg, List<AksjonspunktDefinisjon> inngangAksjonspunkter, List<AksjonspunktDefinisjon> utgangAksjonspunkter) {
        this.behandlingStegType = behandlingStegType;
        this.behandlingType = behandlingType;
        this.fagsakYtelseType = fagsakYtelseType;
        this.steg = steg;
        this.inngangAksjonspunkter = inngangAksjonspunkter;
        this.utgangAksjonspunkter = utgangAksjonspunkter;
    }

    public TestStegKonfig(BehandlingStegType behandlingStegType, BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType, BehandlingSteg steg) {
        this(behandlingStegType, behandlingType, fagsakYtelseType, steg, Collections.emptyList(), Collections.emptyList());
    }

    public BehandlingStegType getBehandlingStegType() {
        return behandlingStegType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public BehandlingSteg getSteg() {
        return steg;
    }

    public List<AksjonspunktDefinisjon> getInngangAksjonspunkter() {
        return inngangAksjonspunkter;
    }

    public List<AksjonspunktDefinisjon> getUtgangAksjonspunkter() {
        return utgangAksjonspunkter;
    }
}
