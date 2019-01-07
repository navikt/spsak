package no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap;

import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "MedlemskapVilkårPeriodeGrunnlag")
@Table(name = "GR_MEDLEMSKAP_VILKAR_PERIODE")
public class MedlemskapVilkårPeriodeGrunnlagEntitet extends BaseEntitet implements MedlemskapVilkårPeriodeGrunnlag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_MEDLEMSKAP_VILKAR_PER")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "vilkar_resultat_id", nullable = false, updatable = false)
    private VilkårResultat vilkårResultat;

    @OneToOne(optional = false)
    @JoinColumn(name = "medlemskap_vilkar_periode_id", nullable = false, updatable = false, unique = true)
    @ChangeTracked
    private MedlemskapsvilkårPeriodeEntitet medlemskapsvilkårPeriode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    MedlemskapVilkårPeriodeGrunnlagEntitet() {
    }

    private MedlemskapVilkårPeriodeGrunnlagEntitet(VilkårResultat vilkårResultat) {
        this.vilkårResultat = vilkårResultat;
    }

    MedlemskapVilkårPeriodeGrunnlagEntitet(MedlemskapVilkårPeriodeGrunnlagEntitet entitet) {
        this.vilkårResultat = entitet.vilkårResultat;
        this.medlemskapsvilkårPeriode = entitet.medlemskapsvilkårPeriode;
    }

    public static MedlemskapVilkårPeriodeGrunnlagEntitet fra(Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> eksisterendeGrunnlag, Behandling nyBehandling) {
        return kopierTidligerGrunnlag(eksisterendeGrunnlag, nyBehandling);
    }

    private static MedlemskapVilkårPeriodeGrunnlagEntitet kopierTidligerGrunnlag(
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> tidligereGrunnlagOpt, Behandling nyBehandling) {
        VilkårResultat vilkårResultat = nyBehandling.getBehandlingsresultat().getVilkårResultat();
        MedlemskapVilkårPeriodeGrunnlagEntitet nyttGrunnlag = new MedlemskapVilkårPeriodeGrunnlagEntitet(vilkårResultat);

        if (tidligereGrunnlagOpt.isPresent()) {
            MedlemskapVilkårPeriodeGrunnlagEntitet tidligereGrunnlag = tidligereGrunnlagOpt.get();
            nyttGrunnlag.setMedlemskapsvilkårPeriode(tidligereGrunnlag.getMedlemskapsvilkårPeriode());
        }
        return nyttGrunnlag;
    }

    @Override
    public MedlemskapsvilkårPeriodeEntitet getMedlemskapsvilkårPeriode() {
        return medlemskapsvilkårPeriode;
    }

    void setMedlemskapsvilkårPeriode(MedlemskapsvilkårPeriodeEntitet medlemskapsvilkårPeriode) {
        this.medlemskapsvilkårPeriode = medlemskapsvilkårPeriode;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    void setVilkårResultat(VilkårResultat vilkårResultat) {
        this.vilkårResultat = vilkårResultat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedlemskapVilkårPeriodeGrunnlagEntitet that = (MedlemskapVilkårPeriodeGrunnlagEntitet) o;
        return aktiv == that.aktiv &&
            Objects.equals(medlemskapsvilkårPeriode, that.medlemskapsvilkårPeriode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktiv, medlemskapsvilkårPeriode);
    }

    public static class Builder {
        private MedlemskapVilkårPeriodeGrunnlagEntitet kladd;

        private Builder() {
            this.kladd = new MedlemskapVilkårPeriodeGrunnlagEntitet();
        }

        private Builder(MedlemskapVilkårPeriodeGrunnlagEntitet kladd) {
            this.kladd = kladd;
        }

        private static Builder oppdatere(MedlemskapVilkårPeriodeGrunnlagEntitet aggregat) {
            return new Builder(aggregat);
        }

        public static Builder oppdatere(Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> aggregat) {
            return aggregat.map(Builder::oppdatere).orElseGet(Builder::new);
        }

        public Builder medMedlemskapsvilkårPeriode(MedlemskapsvilkårPeriodeEntitet.Builder builder) {
            kladd.setMedlemskapsvilkårPeriode(builder.build());
            return this;
        }

        public MedlemskapsvilkårPeriodeEntitet.Builder getPeriodeBuilder() {
            return MedlemskapsvilkårPeriodeEntitet.Builder.oppdatere(Optional.ofNullable(kladd.medlemskapsvilkårPeriode));
        }

        public MedlemskapVilkårPeriodeGrunnlagEntitet build() {
            return kladd;
        }
    }
}
