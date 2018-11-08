package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface RegistrertMedlemskapPerioder extends Comparable<RegistrertMedlemskapPerioder> {

    LocalDate getFom();

    LocalDate getTom();

    DatoIntervallEntitet getPeriode();

    LocalDate getBeslutningsdato();

    boolean getErMedlem();

    MedlemskapType getMedlemskapType();

    MedlemskapDekningType getDekningType();

    MedlemskapKildeType getKildeType();

    Landkoder getLovvalgLand();

    Landkoder getStudieland();

    Long getMedlId();

}
