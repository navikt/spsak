package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.HarAktørId;

public interface Personopplysning extends Comparable<Personopplysning>, HarAktørId {

    String getNavn();

    NavBrukerKjønn getKjønn();

    SivilstandType getSivilstand();

    LocalDate getFødselsdato();

    LocalDate getDødsdato();

    Region getRegion();
}
