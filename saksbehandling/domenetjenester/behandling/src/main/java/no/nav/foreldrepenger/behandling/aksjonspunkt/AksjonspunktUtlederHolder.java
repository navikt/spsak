package no.nav.foreldrepenger.behandling.aksjonspunkt;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AksjonspunktUtlederHolder {

    private final List<AksjonspunktUtleder> utledere;

    public AksjonspunktUtlederHolder() {
        this.utledere = new ArrayList<>();
    }

    public AksjonspunktUtlederHolder leggTil(Class<? extends AksjonspunktUtleder> utleder) {
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
        AksjonspunktUtleder minInstans = instance.get();
        
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }

    public List<AksjonspunktUtleder> getUtledere() {
        return Collections.unmodifiableList(utledere);
    }
}
