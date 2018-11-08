package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtlederFeil;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AksjonspunktUtlederHolder {

    private final List<AksjonspunktUtleder> utledere;

    AksjonspunktUtlederHolder() {
        this.utledere = new ArrayList<>();
    }

    AksjonspunktUtlederHolder leggTil(Class<? extends AksjonspunktUtleder> utleder) {
        utledere.add(hentUtleder(utleder));
        return this;
    }

    private AksjonspunktUtleder hentUtleder(Class<? extends AksjonspunktUtleder> aksjonspunktUtlederClass) {
        final Instance<? extends AksjonspunktUtleder> instance = CDI.current().select(aksjonspunktUtlederClass);

        if (instance.isAmbiguous()) {
            throw AksjonspunktUtlederFeil.FACTORY.flereImplementasjonerAvAksjonspunktUtleder(aksjonspunktUtlederClass.getSimpleName()).toException();
        } else if (instance.isUnsatisfied()) {
            throw AksjonspunktUtlederFeil.FACTORY.fantIkkeAksjonspunktUtleder(aksjonspunktUtlederClass.getSimpleName()).toException();
        }
        return instance.get();
    }

    public List<AksjonspunktUtleder> getUtledere() {
        return Collections.unmodifiableList(utledere);
    }
}
