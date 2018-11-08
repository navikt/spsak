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

@Entity(name = "MedlemskapsvilkårPeriode")
@Table(name = "MEDLEMSKAP_VILKAR_PERIODE")
public class MedlemskapsvilkårPeriodeEntitet extends BaseEntitet implements MedlemskapsvilkårPeriode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEDLEMSKAP_VILKAR_PERIODE")
    private Long id;

    @OneToMany(mappedBy = "rot")
    @ChangeTracked
    private Set<MedlemskapsvilkårPerioderEntitet> perioder = new HashSet<>();

    public MedlemskapsvilkårPeriodeEntitet() {
        // For Hibernate
    }

    MedlemskapsvilkårPeriodeEntitet(Set<MedlemskapsvilkårPerioderEntitet> perioder) {
        this.perioder = perioder;
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

    public void leggTil(MedlemskapsvilkårPerioderEntitet entitet) {
        perioder.add(entitet);
    }
}
