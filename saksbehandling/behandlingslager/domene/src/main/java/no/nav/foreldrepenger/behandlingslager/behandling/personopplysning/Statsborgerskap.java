package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.HarAktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Statsborgerskap extends HarAktørId {

    DatoIntervallEntitet getPeriode();

    Landkoder getStatsborgerskap();

    Region getRegion();

}
