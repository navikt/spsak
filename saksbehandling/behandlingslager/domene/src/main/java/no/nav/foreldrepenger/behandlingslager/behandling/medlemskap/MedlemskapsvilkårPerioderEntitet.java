package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;

@Entity(name = "MedlemskapsvilkårPerioder")
@Table(name = "MEDLEMSKAP_VILKAR_PERIODER")
public class MedlemskapsvilkårPerioderEntitet extends BaseEntitet implements MedlemskapsvilkårPerioder, IndexKey {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_VILKAR_PERIODER")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medlemskap_vilkar_periode_id", nullable = false, updatable = false)
    private MedlemskapsvilkårPeriodeEntitet rot;

    @Column(name = "fom", nullable = false)
    @ChangeTracked
    private LocalDate fom;

    @Column(name = "tom")
    @ChangeTracked
    private LocalDate tom;

    @Column(name = "vurderingsdato")
    @ChangeTracked
    private LocalDate vurderingsdato;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_utfall", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårUtfallType.DISCRIMINATOR
                + "'")) })
    private VilkårUtfallType vilkårUtfall = VilkårUtfallType.UDEFINERT;

    public MedlemskapsvilkårPerioderEntitet() {
    }

    // copy constructor
    MedlemskapsvilkårPerioderEntitet(MedlemskapsvilkårPerioder m) {
        this.fom = m.getFom();
        this.tom = m.getTom();
        this.vilkårUtfall = m.getVilkårUtfall();
        this.vurderingsdato = m.getVurderingsdato();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(fom, tom);
    }

    public Long getId() {
        return id;
    }

    @Override
    public LocalDate getFom() {
        return fom;
    }

    private void setFom(LocalDate fom) {
        this.fom = fom;
    }

    @Override
    public LocalDate getTom() {
        return tom;
    }

    private void setTom(LocalDate tom) {
        this.tom = tom;
    }

    @Override
    public VilkårUtfallType getVilkårUtfall() {
        return vilkårUtfall;
    }

    @Override
    public LocalDate getVurderingsdato() {
        return vurderingsdato;
    }

    private void setVilkårUtfall(VilkårUtfallType vilkårUtfall) {
        this.vilkårUtfall = vilkårUtfall;
    }

    public MedlemskapsvilkårPeriodeEntitet getRot() {
        return rot;
    }

    void setRot(MedlemskapsvilkårPeriodeEntitet rot) {
        this.rot = rot;
    }
    
    private void setVurderingsdato(LocalDate vurderingsdato) {
        this.vurderingsdato = vurderingsdato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MedlemskapsvilkårPerioderEntitet that = (MedlemskapsvilkårPerioderEntitet) o;
        return Objects.equals(getFom(), that.getFom()) &&
            Objects.equals(getTom(), that.getTom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFom(), getTom());
    }

    public static class Builder {
        private MedlemskapsvilkårPerioderEntitet mal;

        private Builder() {
            mal = new MedlemskapsvilkårPerioderEntitet();
        }

        private Builder(MedlemskapsvilkårPerioderEntitet mal) {
            this.mal = mal;
        }

        static Builder oppdater(Optional<MedlemskapsvilkårPerioderEntitet> entitet, LocalDate vurderingsdato) {
            if (entitet.isPresent()) {
                return new Builder(entitet.get());
            }
            Builder builder = new Builder();
            builder.medVurderingsdato(vurderingsdato);
            return builder;
        }

        public Builder medVilkårUtfall(VilkårUtfallType vilkårUtfall) {
            mal.setVilkårUtfall(vilkårUtfall);
            return this;
        }

        public Builder medVurderingsdato(LocalDate vurderingsdato) {
            mal.setVurderingsdato(vurderingsdato);

            // fjernes når fom og tom ryddesvekk
            mal.setFom(vurderingsdato);
            mal.setTom(vurderingsdato);
            return this;
        }

        MedlemskapsvilkårPerioderEntitet build() {
            return mal;
        }
    }

}
