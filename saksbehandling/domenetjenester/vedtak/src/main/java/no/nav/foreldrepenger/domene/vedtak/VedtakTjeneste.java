package no.nav.foreldrepenger.domene.vedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;

public interface VedtakTjeneste {
    void lagHistorikkinnslagFattVedtak(Behandling behandling);

    VedtakResultatType utledVedtakResultatType(Behandling behandling);
}
