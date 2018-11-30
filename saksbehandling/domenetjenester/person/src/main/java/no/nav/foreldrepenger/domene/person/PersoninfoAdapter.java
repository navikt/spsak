package no.nav.foreldrepenger.domene.person;

import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public interface PersoninfoAdapter {

    Personinfo innhentSaksopplysningerForSøker(AktørId aktørId);

    Optional<Personinfo> innhentSaksopplysningerForEktefelle(Optional<AktørId> aktørId);

    Optional<Personinfo> innhentSaksopplysningerForBarn(PersonIdent personIdent);

    Adresseinfo innhentAdresseopplysningerForDokumentsending(AktørId aktørId);

    Optional<Personinfo> innhentSaksopplysninger(PersonIdent personIdent);

    Personhistorikkinfo innhentPersonopplysningerHistorikk(AktørId aktørId, Interval interval);
}
