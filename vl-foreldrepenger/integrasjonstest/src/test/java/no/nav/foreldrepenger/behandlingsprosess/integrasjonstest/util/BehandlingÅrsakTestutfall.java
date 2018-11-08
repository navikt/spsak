package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

public class BehandlingÅrsakTestutfall {

    private Long behandlingId;
    private List<BehandlingÅrsakType> forventet;

    public BehandlingÅrsakTestutfall(Long behandlingId, List<BehandlingÅrsakType> forventet) {
        this.behandlingId = behandlingId;
        this.forventet = forventet;
    }

    public static BehandlingÅrsakTestutfall resultat(Long behandlingId, List<BehandlingÅrsakType> forventet) {
        return new BehandlingÅrsakTestutfall(behandlingId, forventet);
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public List<BehandlingÅrsakType> getForventet() {
        return forventet;
    }
}
