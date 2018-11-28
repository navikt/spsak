package no.nav.foreldrepenger.behandling.revurdering;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

public interface RevurderingTjeneste {

    Behandling opprettManuellRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak);

    Behandling opprettAutomatiskRevurdering(Fagsak fagsak, BehandlingÅrsakType revurderingsÅrsak);

    Boolean kanRevurderingOpprettes(Fagsak fagsak);

    boolean erRevurderingMedUendretUtfall(Behandling behandling);

}
