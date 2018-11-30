package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface BehandlingRelatertInformasjonApplikasjonTjeneste {

    List<TilgrensendeYtelserDto> hentRelaterteYtelser(Behandling behandling, AktørId aktørId, Boolean bareInnvilget);

}
