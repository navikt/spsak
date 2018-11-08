package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;

public interface FastsettePerioderRegelGrunnlagBygger {
    FastsettePeriodeGrunnlag byggGrunnlag(Behandling behandling);
}
