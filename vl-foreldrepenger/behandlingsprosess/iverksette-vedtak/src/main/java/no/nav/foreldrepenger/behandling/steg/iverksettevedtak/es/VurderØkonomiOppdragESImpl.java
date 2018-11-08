package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.es;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.VurderØkonomiOppdrag;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class VurderØkonomiOppdragESImpl extends VurderØkonomiOppdrag {

    VurderØkonomiOppdragESImpl() {
        // for CDI proxy
    }

    @Override
    public boolean skalSendeOppdrag(Behandling behandling, BehandlingVedtak behandlingVedtak) {
        if (BehandlingType.REVURDERING.equals(behandling.getType())) {
            return !behandlingVedtak.isBeslutningsvedtak();
        }
        return !erAvslagPåGrunnAvTidligereUtbetaltEngangsstønad(behandlingVedtak) && erInnvilgetVedtak(behandlingVedtak);
    }

    private boolean erAvslagPåGrunnAvTidligereUtbetaltEngangsstønad(BehandlingVedtak behandlingVedtak) {
        if (VedtakResultatType.AVSLAG.equals(behandlingVedtak.getVedtakResultatType())) {
            return Optional.ofNullable(behandlingVedtak.getBehandlingsresultat().getAvslagsårsak())
                .map(Avslagsårsak::erAlleredeUtbetaltEngangsstønad)
                .orElse(Boolean.FALSE);
        }
        return false;
    }

    private boolean erInnvilgetVedtak(BehandlingVedtak behandlingVedtak) {
        return VedtakResultatType.INNVILGET.equals(behandlingVedtak.getVedtakResultatType()) &&
            !behandlingVedtak.isBeslutningsvedtak();
    }
}
