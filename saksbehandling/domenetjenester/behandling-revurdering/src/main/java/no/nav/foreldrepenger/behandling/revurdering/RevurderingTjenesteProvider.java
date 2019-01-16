package no.nav.foreldrepenger.behandling.revurdering;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.behandling.revurdering.fp.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

@ApplicationScoped
public class RevurderingTjenesteProvider {
    public RevurderingTjeneste finnRevurderingTjenesteFor(Fagsak fagsak) {

        Instance<RevurderingTjeneste> selected = CDI.current().select(RevurderingTjeneste.class);
        if (selected.isAmbiguous()) {
            String fagsakYtelseType = fagsak.getYtelseType().getKode();

            selected = CDI.current()
                .select(RevurderingTjeneste.class, new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(fagsakYtelseType));
            if (selected.isAmbiguous()) {
                throw RevurderingFeil.FACTORY.flereImplementasjonerAvRevurderingtjeneste(fagsakYtelseType).toException();
            } else if (selected.isUnsatisfied()) {
                throw RevurderingFeil.FACTORY.ingenImplementasjonerAvRevurderingtjeneste(fagsakYtelseType).toException();
            }
        } else if (selected.isUnsatisfied()) {
            throw RevurderingFeil.FACTORY.ingenImplementasjonerAvRevurderingtjeneste("<alle fagsak typer>").toException();
        }

        RevurderingTjeneste tjeneste = selected.get();
        if (tjeneste.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException(
                "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + tjeneste.getClass());
        }
        return tjeneste;
    }
}
