package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@ApplicationScoped
public class KompletthetsjekkerProvider {

    public Kompletthetsjekker finnKompletthetsjekkerFor(Behandling behandling) {
        String fagsakYtelseType = behandling.getFagsakYtelseType().getKode();
        String behandlingType = behandling.getType().getKode();

        Instance<Kompletthetsjekker> instance = CDI.current()
            .select(Kompletthetsjekker.class, new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakYtelseType));

        if (instance.isAmbiguous()) {
            instance = instance.select(new BehandlingTypeRef.BehandlingTypeRefLiteral(behandlingType));
        }

        if (instance.isAmbiguous()) {
            throw KompletthetFeil.FACTORY.flereImplementasjonerAvKompletthetsjekker(fagsakYtelseType, behandlingType).toException();
        } else if (instance.isUnsatisfied()) {
            throw KompletthetFeil.FACTORY.ingenImplementasjonerAvKompletthetssjekker(fagsakYtelseType, behandlingType).toException();
        }
        Kompletthetsjekker minInstans = instance.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return instance.get();
    }
}
