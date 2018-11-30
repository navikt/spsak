package no.nav.foreldrepenger.behandling.steg.vedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;

class UtledVedtakResultatType {
    private UtledVedtakResultatType() {
        // hide public contructor
    }

    static VedtakResultatType utled(Behandling behandling) {
        BehandlingResultatType behandlingResultatType = behandling.getBehandlingsresultat().getBehandlingResultatType();
        if (BehandlingResultatType.INNVILGET.equals(behandlingResultatType)) {
            return VedtakResultatType.INNVILGET;
        }
        if (BehandlingResultatType.FORELDREPENGER_ENDRET.equals(behandlingResultatType)) {
            return VedtakResultatType.INNVILGET;
        }
        if (BehandlingResultatType.INGEN_ENDRING.equals(behandlingResultatType)) {
            Behandling originalBehandling = behandling.getOriginalBehandling()
                .orElseThrow(() -> new IllegalStateException("Kan ikke ha resultat INGEN ENDRING uten å ha en original behandling"));
            return utled(originalBehandling);
        }
        return VedtakResultatType.AVSLAG;
    }
}
