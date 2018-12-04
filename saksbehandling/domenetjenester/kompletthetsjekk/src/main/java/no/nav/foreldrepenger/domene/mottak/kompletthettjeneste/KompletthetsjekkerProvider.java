package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class KompletthetsjekkerProvider {

    private Instance<Kompletthetsjekker> instans;

    private final Map<Set<String>, Kompletthetsjekker> cachedinstanser = new ConcurrentHashMap<>();

    KompletthetsjekkerProvider() {
        // for CDI proxy
    }

    @Inject
    public KompletthetsjekkerProvider(Instance<Kompletthetsjekker> instans) {
        this.instans = instans;
    }

    public Kompletthetsjekker finnKompletthetsjekkerFor(Behandling behandling) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();

        var key = Set.of(fagsakYtelseType, behandlingType);

        if (!cachedinstanser.containsKey(key)) {
            cachedinstanser.putIfAbsent(key, nyInstans(fagsakYtelseType, behandlingType));
        }
        return cachedinstanser.get(key);

    }

    synchronized Kompletthetsjekker nyInstans(String fagsakYtelseType, String behandlingType) {
        if (instans.isUnsatisfied()) {
            throw KompletthetFeil.FACTORY.ingenImplementasjonerAvKompletthetssjekker(fagsakYtelseType, behandlingType).toException();
        } else if (!instans.isAmbiguous() && !instans.isUnsatisfied()) {
            return instans.get();
        } else {

            Instance<Kompletthetsjekker> instance = instans.select(new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingType));

            if (instance.isAmbiguous()) {
                instance = instance.select(new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakYtelseType));
            }

            if (instance.isAmbiguous()) {
                throw KompletthetFeil.FACTORY.flereImplementasjonerAvKompletthetsjekker(fagsakYtelseType, behandlingType).toException();
            } else if (instance.isUnsatisfied()) {
                throw KompletthetFeil.FACTORY.ingenImplementasjonerAvKompletthetssjekker(fagsakYtelseType, behandlingType).toException();
            }
            Kompletthetsjekker minInstans = instance.get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                    "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }
            return instance.get();
        }
    }
}
