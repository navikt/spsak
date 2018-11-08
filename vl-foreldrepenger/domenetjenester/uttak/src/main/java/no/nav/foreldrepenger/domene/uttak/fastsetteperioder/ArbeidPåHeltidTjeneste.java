package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;

public interface ArbeidPåHeltidTjeneste {

    boolean jobberFulltid(OppgittPeriode søknadsperiode);
}
