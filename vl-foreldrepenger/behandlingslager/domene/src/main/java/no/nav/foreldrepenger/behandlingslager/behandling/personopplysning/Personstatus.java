package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.domene.typer.HarAktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Personstatus extends HarAktørId {

    DatoIntervallEntitet getPeriode();

    PersonstatusType getPersonstatus();

}
