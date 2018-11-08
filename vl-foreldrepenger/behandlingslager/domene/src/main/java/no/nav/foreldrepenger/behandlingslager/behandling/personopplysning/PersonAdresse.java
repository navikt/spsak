package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.domene.typer.HarAktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface PersonAdresse extends HarAktørId {

    AdresseType getAdresseType();

    String getAdresselinje1();

    String getAdresselinje2();

    String getAdresselinje3();

    String getAdresselinje4();

    String getPostnummer();

    String getPoststed();

    String getLand();

    DatoIntervallEntitet getPeriode();

}
