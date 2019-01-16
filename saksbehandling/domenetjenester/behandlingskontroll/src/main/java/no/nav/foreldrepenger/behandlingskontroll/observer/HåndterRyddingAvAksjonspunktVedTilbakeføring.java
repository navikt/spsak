package no.nav.foreldrepenger.behandlingskontroll.observer;

import java.util.Objects;
import java.util.function.Consumer;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;

final class HåndterRyddingAvAksjonspunktVedTilbakeføring implements Consumer<Aksjonspunkt> {
    private final BehandlingStegType førsteSteg;
    private final BehandlingModellImpl modell;
    private AksjonspunktRepository aksjonspunktRepository;

    HåndterRyddingAvAksjonspunktVedTilbakeføring(AksjonspunktRepository aksjonspunktRepository, BehandlingStegType førsteSteg, BehandlingModellImpl modell) {
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.førsteSteg = førsteSteg;
        this.modell = modell;
    }

    @Override
    public void accept(Aksjonspunkt a) {
        if (a.erManueltOpprettet() && !a.erAktivt()) {
            aksjonspunktRepository.reaktiver(a);
        }
        if (skalAvbryte(a)) {
            aksjonspunktRepository.setTilAvbrutt(a);
        } else if (skalReåpne(a)) {
            aksjonspunktRepository.setReåpnet(a);
        }
    }

    /**
     * Ved tilbakeføring skal følgende reåpnes:
     * - Påfølgende aksjonspunkt som er OVERSTYRING
     * - Aksjonspunkter som er identifisert før steget og skal håndteres i eller etter steget
     * - Aksjonspunkter som er identifisert i selve steget
     */
    boolean skalReåpne(Aksjonspunkt a) {
        BehandlingStegType måTidligstLøsesISteg = modell.finnTidligsteStegFor(a.getAksjonspunktDefinisjon())
            .getBehandlingStegType();
        boolean måLøsesIEllerEtterFørsteSteg = !modell.erStegAFørStegB(måTidligstLøsesISteg, førsteSteg);
        boolean erFunnetFørMåLøsesEtterFørsteSteg = måLøsesIEllerEtterFørsteSteg && modell.erStegAFørStegB(a.getBehandlingStegFunnet(), førsteSteg);
        boolean erOpprettetIFørsteSteg = erOpprettetIFørsteSteg(a);
        boolean reåpne = (a.erManueltOpprettet() && måLøsesIEllerEtterFørsteSteg) || erFunnetFørMåLøsesEtterFørsteSteg ||
            erOpprettetIFørsteSteg;
        return reåpne;
    }

    /**
     * Ved tilbakeføring skal alle påfølgende aksjonspunkt (som IKKE ER OVERSTYRING) som identifiseres i eller
     * senere steg Avbrytes
     */
    boolean skalAvbryte(Aksjonspunkt a) {
        boolean erFunnetIFørsteStegEllerSenere = !modell.erStegAFørStegB(a.getBehandlingStegFunnet(), førsteSteg);
        boolean erManueltOpprettet = a.erManueltOpprettet();
        boolean erOpprettetIFørsteSteg = erOpprettetIFørsteSteg(a);
        boolean avbryt = !erManueltOpprettet && erFunnetIFørsteStegEllerSenere && !erOpprettetIFørsteSteg;
        return avbryt;
    }

    private boolean erOpprettetIFørsteSteg(Aksjonspunkt ap) {
        return Objects.equals(førsteSteg, ap.getBehandlingStegFunnet());
    }
}
