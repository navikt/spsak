package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær;

import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemeldinger;

public interface SykefraværGrunnlag {

    Sykemeldinger getSykemeldinger();

    Sykefravær getSykefravær();
}
