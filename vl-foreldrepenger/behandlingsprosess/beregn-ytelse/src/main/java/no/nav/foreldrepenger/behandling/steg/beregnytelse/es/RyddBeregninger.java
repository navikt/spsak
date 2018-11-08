package no.nav.foreldrepenger.behandling.steg.beregnytelse.es;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;

// TODO (TOPAS): FLytt logikk til BeregningRepository
class RyddBeregninger {

    private BehandlingRepository behandlingRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;

    RyddBeregninger(BehandlingRepository behandlingRepository, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.behandlingRepository = behandlingRepository;
        this.behandling = behandling;
        this.kontekst = kontekst;
    }

    void ryddBeregninger() {
        behandlingRepository.slettTidligereBeregninger(behandling, kontekst.getSkriveLås());

        // TODO (eesv): Er dette nødvendig? Neppe
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null
            && behandlingsresultat.getBeregningResultat() != null
            && behandlingsresultat.getBeregningResultat().getBeregninger() != null
            && !behandlingsresultat.getBeregningResultat().getBeregninger().isEmpty()) {
            BeregningResultat.builderFraEksisterende(behandlingsresultat.getBeregningResultat())
                .nullstillBeregninger()
                .buildFor(behandling);
        }
    }
}
