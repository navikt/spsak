package no.nav.foreldrepenger.behandling.steg.vedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;

class UtledVedtakResultatType {
    private UtledVedtakResultatType() {
        // hide public contructor
    }

    static VedtakResultatType utled(BehandlingRepository behandlingRepository, Behandling behandling) {
        BehandlingResultatType behandlingResultatType = behandlingRepository.hentResultat(behandling.getId()).getBehandlingResultatType();
        if (BehandlingResultatType.INNVILGET.equals(behandlingResultatType)) {
            return VedtakResultatType.INNVILGET;
        }
        if (BehandlingResultatType.FORELDREPENGER_ENDRET.equals(behandlingResultatType)) {
            return VedtakResultatType.INNVILGET;
        }
        if (BehandlingResultatType.INGEN_ENDRING.equals(behandlingResultatType)) {
            Behandling originalBehandling = behandling.getOriginalBehandling()
                .orElseThrow(() -> new IllegalStateException("Kan ikke ha resultat INGEN ENDRING uten å ha en original behandling"));
            return utled(behandlingRepository, originalBehandling);
        }
        return VedtakResultatType.AVSLAG;
    }
}
