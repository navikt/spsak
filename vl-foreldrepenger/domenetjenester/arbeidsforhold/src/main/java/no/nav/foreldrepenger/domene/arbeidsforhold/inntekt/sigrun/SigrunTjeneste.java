package no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.typer.AktørId;

public interface SigrunTjeneste {

    void hentOgLagrePGI(Behandling behandling, AktørId aktørId);
}
