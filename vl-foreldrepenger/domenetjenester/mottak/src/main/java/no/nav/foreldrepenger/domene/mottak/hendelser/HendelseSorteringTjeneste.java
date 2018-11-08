package no.nav.foreldrepenger.domene.mottak.hendelser;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;

public interface HendelseSorteringTjeneste {

    List<AktørId> hentAktørIderTilknyttetSak(List<AktørId> aktørIdList);

}
