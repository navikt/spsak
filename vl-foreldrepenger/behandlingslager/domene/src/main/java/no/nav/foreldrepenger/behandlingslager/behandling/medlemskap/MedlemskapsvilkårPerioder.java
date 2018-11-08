package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

public interface MedlemskapsvilkårPerioder {
    LocalDate getFom();

    LocalDate getTom();

    VilkårUtfallType getVilkårUtfall();

    LocalDate getVurderingsdato();
}
