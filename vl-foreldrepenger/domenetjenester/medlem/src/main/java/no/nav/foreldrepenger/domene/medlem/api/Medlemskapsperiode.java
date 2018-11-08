package no.nav.foreldrepenger.domene.medlem.api;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapKildeType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;

import java.time.LocalDate;
import java.util.Objects;

public class Medlemskapsperiode {
    private LocalDate fom;
    private LocalDate tom;
    private LocalDate datoBesluttet;
    private boolean erMedlem;
    private MedlemskapDekningType trygdedekning;
    private MedlemskapKildeType kilde;
    private MedlemskapType lovvalg;
    private Landkoder lovvalgsland;
    private Landkoder studieland;
    private Long medlId;

    public LocalDate getFom() {
        return fom;
    }

    void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public LocalDate getDatoBesluttet() {
        return datoBesluttet;
    }

    void setDatoBesluttet(LocalDate datoBesluttet) {
        this.datoBesluttet = datoBesluttet;
    }

    public boolean isErMedlem() {
        return erMedlem;
    }

    void setErMedlem(boolean erMedlem) {
        this.erMedlem = erMedlem;
    }

    public MedlemskapDekningType getTrygdedekning() {
        return trygdedekning;
    }

    void setTrygdedekning(MedlemskapDekningType trygdedekning) {
        this.trygdedekning = trygdedekning;
    }

    public MedlemskapKildeType getKilde() {
        return kilde;
    }

    void setKilde(MedlemskapKildeType kilde) {
        this.kilde = kilde;
    }

    public MedlemskapType getLovvalg() {
        return lovvalg;
    }

    void setLovvalg(MedlemskapType lovvalg) {
        this.lovvalg = lovvalg;
    }

    public Landkoder getLovvalgsland() {
        return lovvalgsland;
    }

    void setLovvalgsland(Landkoder lovvalgsland) {
        this.lovvalgsland = lovvalgsland;
    }

    public Landkoder getStudieland() {
        return studieland;
    }

    void setStudieland(Landkoder studieland) {
        this.studieland = studieland;
    }

    public Long getMedlId() {
        return medlId;
    }

    void setMedlId(Long medlId) {
        this.medlId = medlId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Medlemskapsperiode)) {
            return false;
        }
        Medlemskapsperiode other = (Medlemskapsperiode) obj;
        return Objects.equals(this.fom, other.fom)
            && Objects.equals(this.tom, other.tom)
            && Objects.equals(this.datoBesluttet, other.datoBesluttet)
            && Objects.equals(this.erMedlem, other.erMedlem)
            && Objects.equals(this.trygdedekning, other.trygdedekning)
            && Objects.equals(this.kilde, other.kilde)
            && Objects.equals(this.lovvalg, other.lovvalg)
            && Objects.equals(this.lovvalgsland, other.lovvalgsland)
            && Objects.equals(this.studieland, other.studieland)
            && Objects.equals(this.medlId, other.medlId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom, datoBesluttet, erMedlem, trygdedekning, kilde, lovvalg, lovvalgsland, studieland, medlId);
    }

    public static class Builder {
        private Medlemskapsperiode periodeMal = new Medlemskapsperiode();

        public Builder medFom(LocalDate fom){
            periodeMal.setFom(fom);
            return this;
        }
        public Builder medTom(LocalDate tom){
            periodeMal.setTom(tom);
            return this;
        }
        public Builder medDatoBesluttet(LocalDate datoBesluttet){
            periodeMal.setDatoBesluttet(datoBesluttet);
            return this;
        }
        public Builder medDekning(MedlemskapDekningType dekning){
            periodeMal.setTrygdedekning(dekning);
            return this;
        }
        public Builder medErMedlem(boolean erMedlem){
            periodeMal.setErMedlem(erMedlem);
            return this;
        }
        public Builder medKilde(MedlemskapKildeType kilde){
            periodeMal.setKilde(kilde);
            return this;
        }
        public Builder medLovvalg(MedlemskapType lovvalg){
            periodeMal.setLovvalg(lovvalg);
            return this;
        }
        public Builder medLovvalgsland(Landkoder lovvalgsland){
            periodeMal.setLovvalgsland(lovvalgsland);
            return this;
        }
        public Builder medStudieland(Landkoder studieland){
            periodeMal.setStudieland(studieland);
            return this;
        }
        public Builder medMedlId(Long medlId){
            periodeMal.setMedlId(medlId);
            return this;
        }
        public Medlemskapsperiode build(){
            return periodeMal;
        }
    }
}
