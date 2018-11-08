package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

public interface VurdertLøpendeMedlemskap extends VurdertMedlemskap {

    Boolean getOppholdsrettVurdering();

    Boolean getLovligOppholdVurdering();

    Boolean getBosattVurdering();

    MedlemskapManuellVurderingType getMedlemsperiodeManuellVurdering();

    Boolean getErEøsBorger();

    LocalDate getVurderingsdato();

}
