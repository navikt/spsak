package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadAnnenPartType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.typer.HarAktørId;

public interface OppgittAnnenPart extends HarAktørId {

    String getNavn();

    String getUtenlandskPersonident();

    Landkoder getUtenlandskFnrLand();

    String getÅrsak();

    SøknadAnnenPartType getType();

    String getBegrunnelse();

}
