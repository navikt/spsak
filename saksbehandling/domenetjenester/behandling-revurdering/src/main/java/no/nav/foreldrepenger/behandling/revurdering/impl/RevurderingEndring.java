package no.nav.foreldrepenger.behandling.revurdering.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface RevurderingEndring {
    /**
     * Tjeneste som vurderer om revurderingen har endret utrfall i forhold til original behandling
     * @param behandling
     * @return
     */
    boolean erRevurderingMedUendretUtfall(Behandling behandling);
}
