package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag;

import java.util.List;

public interface Frilans {

    boolean getHarInntektFraFosterhjem();

    boolean getErNyoppstartet();

    boolean getHarNærRelasjon();

    List<Frilansoppdrag> getFrilansoppdrag();

}
