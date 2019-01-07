package no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "MedlemskapsvilkårPeriode")
@Table(name = "MEDLEMSKAP_VILKAR_PERIODE")
public class MedlemskapsvilkårPeriodeEntitet extends BaseEntitet implements MedlemskapsvilkårPeriode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_VILKAR_PERIODE")
    private Long id;

    @OneToMany(mappedBy = "rot")
    @ChangeTracked
    private Set<MedlemskapsvilkårPerioderEntitet> perioder = new HashSet<>();

    MedlemskapsvilkårPeriodeEntitet() {
        // For Hibernate
    }

    private MedlemskapsvilkårPeriodeEntitet(MedlemskapsvilkårPeriodeEntitet kladd) {
        perioder = kladd.getPerioder().stream()
            .map(MedlemskapsvilkårPerioderEntitet::new)
            .peek(pr -> pr.setRot(this))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<MedlemskapsvilkårPerioder> getPerioder() {
        return Collections.unmodifiableSet(perioder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedlemskapsvilkårPeriodeEntitet that = (MedlemskapsvilkårPeriodeEntitet) o;
        return Objects.equals(perioder, that.perioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(perioder);
    }

    MedlemskapsvilkårPerioderEntitet.Builder getBuilderFor(LocalDate vurderingsdato) {
        Optional<MedlemskapsvilkårPerioderEntitet> medlemOpt = perioder.stream()
            .filter(medlem -> vurderingsdato.equals(medlem.getVurderingsdato()))
            .findFirst();
        return MedlemskapsvilkårPerioderEntitet.Builder.oppdater(medlemOpt, vurderingsdato);
    }

    void leggTil(MedlemskapsvilkårPerioderEntitet entitet) {
        entitet.setRot(this);
        perioder.add(entitet);
    }

    public static class Builder {
        private MedlemskapsvilkårPeriodeEntitet kladd;

        private Builder() {
            this.kladd = new MedlemskapsvilkårPeriodeEntitet();
        }

        private Builder(MedlemskapsvilkårPeriodeEntitet kladd) {
            this.kladd = new MedlemskapsvilkårPeriodeEntitet(kladd);
        }

        private static MedlemskapsvilkårPeriodeEntitet.Builder oppdatere(MedlemskapsvilkårPeriodeEntitet aggregat) {
            return new MedlemskapsvilkårPeriodeEntitet.Builder(aggregat);
        }

        public static MedlemskapsvilkårPeriodeEntitet.Builder oppdatere(Optional<MedlemskapsvilkårPeriodeEntitet> aggregat) {
            return aggregat.map(MedlemskapsvilkårPeriodeEntitet.Builder::oppdatere).orElseGet(MedlemskapsvilkårPeriodeEntitet.Builder::new);
        }

        public MedlemskapsvilkårPeriodeEntitet.Builder leggTil(MedlemskapsvilkårPerioderEntitet.Builder builder) {
            if (!builder.erOppdatering()) {
                kladd.leggTil(builder.build());
            }
            return this;
        }

        public MedlemskapsvilkårPerioderEntitet.Builder getBuilderForVurderingsdato(LocalDate vurderingsdato) {
            return kladd.getBuilderFor(vurderingsdato);
        }

        public MedlemskapsvilkårPeriodeEntitet build() {
            return kladd;
        }
    }
}
