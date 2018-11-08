package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;

public interface ArbeidTidslinjeTjeneste {

    Map<AktivitetIdentifikator, ArbeidTidslinje> lagTidslinjer(Behandling behandling);

}
