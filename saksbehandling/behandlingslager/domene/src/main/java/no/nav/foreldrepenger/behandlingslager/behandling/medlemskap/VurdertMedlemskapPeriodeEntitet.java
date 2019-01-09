package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "VurdertMedlemskapPeriode")
@Table(name = "MEDLEMSKAP_VURDERING_PERIODE")
public class VurdertMedlemskapPeriodeEntitet extends BaseEntitet implements VurdertMedlemskapPeriode {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_VP")
    private Long id;

    @OneToMany(mappedBy = "periodeHolder")
    @ChangeTracked
    private Set<VurdertLøpendeMedlemskapEntitet> perioder = new HashSet<>();

    public VurdertMedlemskapPeriodeEntitet() {
        // hibernate
    }

    VurdertMedlemskapPeriodeEntitet(VurdertMedlemskapPeriode løpendeMedlemskap) {
        løpendeMedlemskap.getPerioder().forEach(vurdertLøpendeMedlemskap ->
            {
                VurdertLøpendeMedlemskapEntitet entitet = new VurdertLøpendeMedlemskapEntitet(vurdertLøpendeMedlemskap);
                entitet.setPeriodeHolder(this);
                perioder.add(entitet);
            }
        );
    }

    public VurdertLøpendeMedlemskapBuilder getBuilderFor(LocalDate vurderingsdato) {
        Optional<VurdertLøpendeMedlemskapEntitet> first = perioder.stream().filter(p -> p.getVurderingsdato().equals(vurderingsdato)).findFirst();
        return new VurdertLøpendeMedlemskapBuilder(first);
    }

    private void leggTil(VurdertLøpendeMedlemskapEntitet entitet) {
        perioder.add(entitet);
    }

    @Override
    public Set<VurdertLøpendeMedlemskap> getPerioder() {
        return Collections.unmodifiableSet(perioder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VurdertMedlemskapPeriodeEntitet that = (VurdertMedlemskapPeriodeEntitet) o;
        return Objects.equals(perioder, that.perioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(perioder);
    }

    public static class VurdertMedlemskapPeriodeEntitetBuilder {
        private VurdertMedlemskapPeriodeEntitet medlemskapMal;

        public VurdertMedlemskapPeriodeEntitetBuilder() {
            medlemskapMal = new VurdertMedlemskapPeriodeEntitet();
        }

        public VurdertMedlemskapPeriodeEntitetBuilder(VurdertMedlemskapPeriode medlemskap) {
            if (medlemskap != null) {
                medlemskapMal = new VurdertMedlemskapPeriodeEntitet(medlemskap);
            } else {
                medlemskapMal = new VurdertMedlemskapPeriodeEntitet();
            }
        }

        public VurdertMedlemskapPeriodeEntitetBuilder leggTil(VurdertLøpendeMedlemskapBuilder builder) {
            VurdertLøpendeMedlemskapEntitet entitet = (VurdertLøpendeMedlemskapEntitet) builder.build();
            medlemskapMal.leggTil(entitet);
            return this;
        }

        public VurdertMedlemskapPeriodeEntitet build() {
            return medlemskapMal;
        }

        public VurdertLøpendeMedlemskapBuilder getBuilderFor(LocalDate vurderingsdato) {
            return medlemskapMal.getBuilderFor(vurderingsdato);
        }
    }
}
