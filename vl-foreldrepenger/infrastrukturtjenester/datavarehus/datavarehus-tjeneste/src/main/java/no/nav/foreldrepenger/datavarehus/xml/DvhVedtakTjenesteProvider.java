package no.nav.foreldrepenger.datavarehus.xml;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

@Dependent
public class DvhVedtakTjenesteProvider {

    private Instance<DvhVedtakTjeneste> alleDvhVedtakTjenester;

    DvhVedtakTjenesteProvider() {
        // CDI
    }

    @Inject
    public DvhVedtakTjenesteProvider(@Any Instance<DvhVedtakTjeneste> alleDvhVedtakTjenester) {
        this.alleDvhVedtakTjenester = alleDvhVedtakTjenester;
    }

    public DvhVedtakTjeneste getVedtakTjeneste(Behandling behandling) {
        String fagsakTypeKode = behandling.getFagsak().getYtelseType().getKode();
        FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral fagsakTypeRef = new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakTypeKode);
        Instance<DvhVedtakTjeneste> selected = alleDvhVedtakTjenester.select(fagsakTypeRef);

        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for fagsakType: " + fagsakTypeKode);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for fagsakTypeKode: " + fagsakTypeKode);
        }
        DvhVedtakTjeneste minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }

        return minInstans;
    }

}
