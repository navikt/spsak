package no.nav.foreldrepenger.domene.person;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.GeografiskTilknytning;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.exception.TekniskException;

public interface TpsTjeneste {

    Optional<Personinfo> hentBrukerForAktør(AktørId aktørId);

    /**
     * Hent PersonIdent (FNR) for gitt aktørId.
     *
     * @throws TekniskException hvis ikke finner.
     */
    PersonIdent hentFnrForAktør(AktørId aktørId);

    Optional<Personinfo> hentBrukerForFnr(PersonIdent fnr);

    Optional<AktørId> hentAktørForFnr(PersonIdent fnr);

    Optional<String> hentDiskresjonskodeForAktør(PersonIdent fnr);

    GeografiskTilknytning hentGeografiskTilknytning(PersonIdent fnr);

    Optional<PersonIdent> hentFnr(AktørId aktørId);
}
