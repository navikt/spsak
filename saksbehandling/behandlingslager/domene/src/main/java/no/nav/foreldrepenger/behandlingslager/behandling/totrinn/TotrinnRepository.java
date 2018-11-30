package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface TotrinnRepository {

    void lagreTotrinnsresultatgrunnlag(Behandling behandling, Totrinnresultatgrunnlag totrinnresultatgrunnlag);

    void lagreOgFlush(Behandling behandling, Totrinnresultatgrunnlag totrinnresultatgrunnlag);

    Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlag(Behandling behandling);

    void lagreTotrinnaksjonspunktvurdering(Totrinnsvurdering totrinnsvurdering);

    void lagreOgFlush(Behandling behandling, Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger);

    Collection<Totrinnsvurdering> hentTotrinnaksjonspunktvurderinger(Behandling behandling);
}
