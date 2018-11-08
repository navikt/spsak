package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;

public interface HenleggBehandlingTjeneste {

    void henleggBehandling(Long behandlingId, BehandlingResultatType årsakKode, String begrunnelse);

}
