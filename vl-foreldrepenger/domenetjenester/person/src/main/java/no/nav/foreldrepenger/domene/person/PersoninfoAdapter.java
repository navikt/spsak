package no.nav.foreldrepenger.domene.person;

import java.util.List;
import java.util.Optional;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public interface PersoninfoAdapter {

    Personinfo innhentSaksopplysningerForSøker(AktørId aktørId);

    Optional<Personinfo> innhentSaksopplysningerForEktefelle(Optional<AktørId> aktørId);

    Optional<Personinfo> innhentSaksopplysningerForBarn(PersonIdent personIdent);

    Adresseinfo innhentAdresseopplysningerForDokumentsending(AktørId aktørId);

    Optional<Personinfo> innhentSaksopplysninger(PersonIdent personIdent);

    Personhistorikkinfo innhentPersonopplysningerHistorikk(AktørId aktørId, Interval interval);

    // Inkluderer FDAT-barn. Denne skal brukes for å lagre bekreftet grunnlag!
    List<FødtBarnInfo> innhentAlleFødteForBehandling(Behandling behandling, FamilieHendelseGrunnlag familieHendelseGrunnlag);
}
