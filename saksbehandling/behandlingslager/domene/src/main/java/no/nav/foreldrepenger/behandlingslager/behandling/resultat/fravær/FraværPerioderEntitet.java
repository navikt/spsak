package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "FraværPerioder")
@Table(name = "FR_FRAVAER_PERIODER")
public class FraværPerioderEntitet extends BaseEntitet implements FraværPerioder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FR_FRAVAER_PERIODER")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = "perioder")
    private List<FraværPeriodeEntitet> fraværPerioder = new ArrayList<>();

    FraværPerioderEntitet() {
    }

    FraværPerioderEntitet(FraværPerioder perioder) {
        fraværPerioder = perioder.getPerioder()
            .stream()
            .map(FraværPeriodeEntitet::new)
            .peek(it -> it.setPerioder(this))
            .collect(Collectors.toList());
    }

    @Override
    public List<FraværPeriode> getPerioder() {
        return Collections.unmodifiableList(fraværPerioder);
    }

    void leggTil(FraværPeriode periode) {
        FraværPeriodeEntitet entitet = (FraværPeriodeEntitet) periode;
        fraværPerioder.add(entitet);
        entitet.setPerioder(this);
    }

    void tilbakestillPerioder() {
        fraværPerioder.clear();
    }

    @Override
    public String toString() {
        return "FraværPerioderEntitet{" +
            "id=" + id +
            ", fraværPerioder=" + fraværPerioder +
            '}';
    }

}
