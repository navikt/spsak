package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

public interface VurdertLøpendeMedlemskap extends VurdertMedlemskap {

    @Override
    Boolean getOppholdsrettVurdering();

    @Override
    Boolean getLovligOppholdVurdering();

    @Override
    Boolean getBosattVurdering();

    @Override
    MedlemskapManuellVurderingType getMedlemsperiodeManuellVurdering();

    @Override
    Boolean getErEøsBorger();

    LocalDate getVurderingsdato();

}
