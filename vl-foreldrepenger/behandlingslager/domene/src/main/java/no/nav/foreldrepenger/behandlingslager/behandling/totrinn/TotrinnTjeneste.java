package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface TotrinnTjeneste {

    void settNyttTotrinnsgrunnlag(Behandling behandling);

    Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlagHvisEksisterer(Behandling behandling);

    Collection<Totrinnsvurdering> hentTotrinnaksjonspunktvurderinger(Behandling behandling);

    void settNyeTotrinnaksjonspunktvurderinger(Behandling behandling, List<Totrinnsvurdering> vurderinger);

}
