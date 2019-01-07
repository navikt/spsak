package no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap;

import java.time.LocalDate;
import java.util.Optional;

public class MedlemskapsvilkårPeriodeGrunnlag {

    private MedlemskapsvilkårPeriodeEntitet medlemskapsvilkårPeriodeEntitet;

    MedlemskapsvilkårPeriodeGrunnlag() {
        medlemskapsvilkårPeriodeEntitet = new MedlemskapsvilkårPeriodeEntitet();
    }

    public MedlemskapsvilkårPeriode getMedlemskapsvilkårPeriode() {
        return medlemskapsvilkårPeriodeEntitet;
    }

    public static Builder oppdatere(Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlag) {
        return grunnlag.map(Builder::oppdatere).orElseGet(Builder::new);
    }

    public static class Builder {
        private MedlemskapsvilkårPeriodeGrunnlag kladd;

        private Builder() {
            this.kladd = new MedlemskapsvilkårPeriodeGrunnlag();
        }
        private Builder(MedlemskapsvilkårPeriodeGrunnlag kladd) {
            this.kladd = kladd;
        }

        private static Builder oppdatere(MedlemskapsvilkårPeriodeGrunnlag aggregat) {
            return new Builder(aggregat);
        }

        static Builder oppdatere(Optional<MedlemskapsvilkårPeriodeGrunnlag> aggregat) {
            return aggregat.map(Builder::oppdatere).orElseGet(Builder::new);
        }

        Builder medmedlemskapsvilkårPeriode(MedlemskapsvilkårPeriodeEntitet medlemskapsvilkår) {
            kladd.medlemskapsvilkårPeriodeEntitet = medlemskapsvilkår;
            return this;
        }

        public Builder leggTilMedlemskapsvilkårPeriode(MedlemskapsvilkårPerioderEntitet.Builder builder) {
            kladd.medlemskapsvilkårPeriodeEntitet.leggTil(builder.build());
            return this;
        }

        public MedlemskapsvilkårPerioderEntitet.Builder getBuilderForVurderingsdato(LocalDate vurderingsdato) {
            return kladd.medlemskapsvilkårPeriodeEntitet.getBuilderFor(vurderingsdato);
        }

        MedlemskapsvilkårPeriodeGrunnlag build() {
            return kladd;
        }
    }
}
