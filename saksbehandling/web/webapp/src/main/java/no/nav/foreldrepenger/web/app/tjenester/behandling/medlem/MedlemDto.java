package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;

public class MedlemDto {

    private List<InntektDto> inntekt;
    private LocalDate skjearingstidspunkt;
    private List<MedlemskapPerioderDto> medlemskapPerioder;
    private Boolean oppholdsrettVurdering;
    private Boolean erEosBorger;
    private Boolean lovligOppholdVurdering;
    private Boolean bosattVurdering;
    private MedlemskapManuellVurderingType medlemskapManuellVurderingType;

    private LocalDate fom; // gjeldendeFra
    private List<EndringIPersonopplysningDto> endringer = new ArrayList<>();

    public MedlemDto() {
        // trengs for deserialisering av JSON
    }

    public List<InntektDto> getInntekt() {
        return inntekt;
    }

    public LocalDate getSkjearingstidspunkt() {
        return skjearingstidspunkt;
    }

    public List<MedlemskapPerioderDto> getMedlemskapPerioder() {
        return medlemskapPerioder;
    }

    public Boolean getOppholdsrettVurdering() {
        return oppholdsrettVurdering;
    }

    public Boolean getLovligOppholdVurdering() {
        return lovligOppholdVurdering;
    }

    public Boolean getBosattVurdering() {
        return bosattVurdering;
    }

    public MedlemskapManuellVurderingType getMedlemskapManuellVurderingType() {
        return medlemskapManuellVurderingType;
    }

    public LocalDate getFom() {
        return fom;
    }

    public Boolean getErEosBorger() {
        return erEosBorger;
    }

    public List<EndringIPersonopplysningDto> getEndringer() {
        return endringer;
    }

    void setInntekt(List<InntektDto> inntekt) {
        this.inntekt = inntekt;
    }

    void setSkjearingstidspunkt(LocalDate skjearingstidspunkt) {
        this.skjearingstidspunkt = skjearingstidspunkt;
    }

    void setMedlemskapPerioder(List<MedlemskapPerioderDto> medlemskapPerioder) {
        this.medlemskapPerioder = medlemskapPerioder;
    }

    void setOppholdsrettVurdering(Boolean oppholdsrettVurdering) {
        this.oppholdsrettVurdering = oppholdsrettVurdering;
    }

    void setErEosBorger(Boolean erEosBorger) {
        this.erEosBorger = erEosBorger;
    }

    void setLovligOppholdVurdering(Boolean lovligOppholdVurdering) {
        this.lovligOppholdVurdering = lovligOppholdVurdering;
    }

    void setBosattVurdering(Boolean bosattVurdering) {
        this.bosattVurdering = bosattVurdering;
    }

    void setMedlemskapManuellVurderingType(MedlemskapManuellVurderingType medlemskapManuellVurderingType) {
        this.medlemskapManuellVurderingType = medlemskapManuellVurderingType;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public void setEndringer(List<EndringIPersonopplysningDto> endringer) {
        this.endringer = endringer;
    }
}
