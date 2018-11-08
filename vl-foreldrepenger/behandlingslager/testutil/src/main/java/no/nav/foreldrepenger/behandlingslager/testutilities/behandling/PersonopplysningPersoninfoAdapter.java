package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Statsborgerskap;
import no.nav.vedtak.konfig.Tid;

/**
 * Intern adapter for å bygge Personopplysning ut av Personinfo og evt. innhenting via providere.
 */
final class PersonopplysningPersoninfoAdapter {

    private PersonopplysningPersoninfoAdapter() {
        //for å hindre instanser av util klasse
    }

    static void mapPersonopplysningTilPerson(PersonInformasjon.Builder builder, Personinfo person) {
        Objects.requireNonNull(person.getFødselsdato(), "Aktør må ha satt fødselsdato");
        builder.leggTilPersonopplysninger(Personopplysning.builder()
            .aktørId(person.getAktørId())
            .navn(person.getNavn())
            .brukerKjønn(person.getKjønn())
            .fødselsdato(person.getFødselsdato())
            .dødsdato(person.getDødsdato())
            .region(person.getRegion())
            .sivilstand(person.getSivilstandType()));

        builder.leggTilStatsborgerskap(Statsborgerskap.builder()
            .aktørId(person.getAktørId())
            .statsborgerskap(person.getLandkode())
            .periode(person.getFødselsdato(), person.getDødsdato() != null ? person.getDødsdato() : Tid.TIDENES_ENDE));

        builder.leggTilPersonstatus(Personstatus.builder()
            .aktørId(person.getAktørId())
            .personstatus(person.getPersonstatus())
            .periode(person.getFødselsdato(), person.getDødsdato() != null ? person.getDødsdato() : Tid.TIDENES_ENDE));
    }

}
