package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

public interface VurdertMedlemskap {

    Boolean getOppholdsrettVurdering();

    Boolean getLovligOppholdVurdering();

    Boolean getBosattVurdering();

    MedlemskapManuellVurderingType getMedlemsperiodeManuellVurdering();

    Boolean getErEøsBorger();

}
