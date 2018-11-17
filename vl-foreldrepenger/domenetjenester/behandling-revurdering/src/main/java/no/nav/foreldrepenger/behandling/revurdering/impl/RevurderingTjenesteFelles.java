package no.nav.foreldrepenger.behandling.revurdering.impl;

import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public class RevurderingTjenesteFelles {

    private FagsakRevurdering fagsakRevurdering;
    private KodeverkRepository kodeverkRepository;

    public RevurderingTjenesteFelles() {
        // for CDI proxy
    }

    @Inject
    public RevurderingTjenesteFelles(BehandlingRepositoryProvider repositoryProvider) {
        this.fagsakRevurdering = new FagsakRevurdering(repositoryProvider.getBehandlingRepository());
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    public Behandling opprettRevurderingsbehandling(BehandlingÅrsakType revurderingÅrsakType, Behandling opprinneligBehandling, boolean manueltOpprettet) {
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.REVURDERING);
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(revurderingÅrsakType)
            .medOriginalBehandling(opprinneligBehandling)
            .medManueltOpprettet(manueltOpprettet);
        return Behandling.fraTidligereBehandling(opprinneligBehandling, behandlingType)
            .medBehandlingÅrsak(revurderingÅrsak).build();
    }

    public Boolean kanRevurderingOpprettes(Fagsak fagsak) {
        return fagsakRevurdering.kanRevurderingOpprettes(fagsak);
    }

}
