package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Optional;

public class VurdertMedlemskapBuilder {
    private VurdertMedlemskapEntitet medlemskapMal;

    public VurdertMedlemskapBuilder() {
        medlemskapMal = new VurdertMedlemskapEntitet();
    }

    public VurdertMedlemskapBuilder(VurdertMedlemskap medlemskap) {
        if (medlemskap != null) {
            this.medlemskapMal = new VurdertMedlemskapEntitet(medlemskap);
        } else {
            medlemskapMal = new VurdertMedlemskapEntitet();
        }
    }

    public VurdertMedlemskapBuilder(Optional<VurdertMedlemskap> vurdertMedlemskap) {
        this(vurdertMedlemskap.isPresent() ? vurdertMedlemskap.get() : null);
    }

    public VurdertMedlemskapBuilder medOppholdsrettVurdering(Boolean oppholdsrettVurdering) {
        medlemskapMal.setOppholdsrettVurdering(oppholdsrettVurdering);
        return this;
    }

    public VurdertMedlemskapBuilder medLovligOppholdVurdering(Boolean lovligOppholdVurdering) {
        medlemskapMal.setLovligOppholdVurdering(lovligOppholdVurdering);
        return this;
    }

    public VurdertMedlemskapBuilder medBosattVurdering(Boolean bosattVurdering) {
        medlemskapMal.setBosattVurdering(bosattVurdering);
        return this;
    }

    public VurdertMedlemskapBuilder medErEosBorger(Boolean erEosBorger) {
        medlemskapMal.setErEøsBorger(erEosBorger);
        return this;
    }

    public VurdertMedlemskapBuilder medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType manuellVurderingType) {
        medlemskapMal.setMedlemsperiodeManuellVurdering(manuellVurderingType);
        return this;
    }

    public VurdertMedlemskapBuilder medFom(LocalDate fom) {
        medlemskapMal.setFom(fom);
        return this;
    }

    public VurdertMedlemskap build() {
        return medlemskapMal;
    }
}
