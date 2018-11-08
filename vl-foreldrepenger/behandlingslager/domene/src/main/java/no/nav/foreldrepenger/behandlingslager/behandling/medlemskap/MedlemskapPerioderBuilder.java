package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.vedtak.konfig.Tid;

public class MedlemskapPerioderBuilder {
    private MedlemskapPerioderEntitet medlemskapPerioderMal;

    public MedlemskapPerioderBuilder() {
        medlemskapPerioderMal = new MedlemskapPerioderEntitet();
    }

    public MedlemskapPerioderBuilder(RegistrertMedlemskapPerioder medlemskapPerioder) {
        if (medlemskapPerioder != null) {
            this.medlemskapPerioderMal = new MedlemskapPerioderEntitet(medlemskapPerioder);
        } else {
            medlemskapPerioderMal = new MedlemskapPerioderEntitet();
        }
    }

    public MedlemskapPerioderBuilder medPeriode(LocalDate fom, LocalDate tom) {
        medlemskapPerioderMal.setPeriode(fom != null ? fom : Tid.TIDENES_BEGYNNELSE, tom != null ? tom : Tid.TIDENES_ENDE);
        return this;
    }

    public MedlemskapPerioderBuilder medBeslutningsdato(LocalDate beslutningsdato) {
        medlemskapPerioderMal.setBeslutningsdato(beslutningsdato);
        return this;
    }

    public MedlemskapPerioderBuilder medErMedlem(boolean erMedlem) {
        medlemskapPerioderMal.setErMedlem(erMedlem);
        return this;
    }

    public MedlemskapPerioderBuilder medLovvalgLand(Landkoder lovvalgsland) {
        medlemskapPerioderMal.setLovvalgLand(lovvalgsland);
        return this;
    }

    public MedlemskapPerioderBuilder medStudieLand(Landkoder studieland) {
        medlemskapPerioderMal.setStudieland(studieland);
        return this;
    }

    public MedlemskapPerioderBuilder medMedlemskapType(MedlemskapType medlemskapType) {
        medlemskapPerioderMal.setMedlemskapType(medlemskapType);
        return this;
    }

    public MedlemskapPerioderBuilder medDekningType(MedlemskapDekningType dekningType) {
        medlemskapPerioderMal.setDekningType(dekningType);
        return this;
    }

    public MedlemskapPerioderBuilder medKildeType(MedlemskapKildeType kildeType) {
        medlemskapPerioderMal.setKildeType(kildeType);
        return this;
    }

    public MedlemskapPerioderBuilder medMedlId(Long medlId){
        medlemskapPerioderMal.setMedlId(medlId);
        return this;
    }

    public RegistrertMedlemskapPerioder build() {
        return medlemskapPerioderMal;
    }
}
