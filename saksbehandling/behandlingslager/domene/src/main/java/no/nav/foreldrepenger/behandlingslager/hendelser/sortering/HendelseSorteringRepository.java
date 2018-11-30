package no.nav.foreldrepenger.behandlingslager.hendelser.sortering;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;

public interface HendelseSorteringRepository {

    List<AktørId> hentEksisterendeAktørIderMedSak(List<AktørId> aktørIdListe);

}
