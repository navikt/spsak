package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.HarAktørId;

public interface PersonRelasjon extends HarAktørId {
    @Override
    AktørId getAktørId();

    AktørId getTilAktørId();

    RelasjonsRolleType getRelasjonsrolle();

    Boolean getHarSammeBosted();
}
