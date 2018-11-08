package no.nav.foreldrepenger.domene.mottak;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.Forretningshendelse;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;

@Dependent
public class ForretningshendelseHåndtererProvider {

    private Instance<ForretningshendelseHåndterer<? extends Forretningshendelse>> håndterere;

    @Inject
    public ForretningshendelseHåndtererProvider(@Any Instance<ForretningshendelseHåndterer<? extends Forretningshendelse>> håndterere) {
        this.håndterere = håndterere;
    }

    @SuppressWarnings("unchecked")
    public <T extends Forretningshendelse> ForretningshendelseHåndterer<T> finnHåndterer(ForretningshendelseType forretningshendelseType) {
        Instance<ForretningshendelseHåndterer<? extends Forretningshendelse>> selected = håndterere.select(new ForretningshendelsestypeRef.ForretningshendelsestypeRefLiteral(forretningshendelseType));
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for forretningshendelsetype:" + forretningshendelseType);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for forretningshendelsetype:" + forretningshendelseType);
        }
        ForretningshendelseHåndterer<? extends Forretningshendelse> minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return (ForretningshendelseHåndterer<T>) minInstans;
    }
}
