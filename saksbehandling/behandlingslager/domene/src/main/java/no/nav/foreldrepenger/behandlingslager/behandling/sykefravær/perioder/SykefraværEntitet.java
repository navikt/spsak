package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "SykefraværEntitet")
@Table(name = "SF_SYKEFRAVAER")
public class SykefraværEntitet extends BaseEntitet implements Sykefravær {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SF_SYKEFRAVAER")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = "sykefravær")
    private List<SykefraværPeriodeEntitet> fraværsPeriode = new ArrayList<>();

    SykefraværEntitet() {

    }

    public SykefraværEntitet(Sykefravær sykefravær) {
        this.fraværsPeriode = sykefravær.getPerioder()
            .stream()
            .map(SykefraværPeriodeEntitet::new)
            .peek(sfp -> sfp.setSykefravær(this))
            .collect(Collectors.toList());
    }

    @Override
    public List<SykefraværPeriode> getPerioder() {
        return Collections.unmodifiableList(fraværsPeriode);
    }


    void tilbakestillPerioder() {
        fraværsPeriode.clear();
    }

    public void leggTil(SykefraværPeriode periode) {
        SykefraværPeriodeEntitet entitet = (SykefraværPeriodeEntitet) periode;
        entitet.setSykefravær(this);
        fraværsPeriode.add(entitet);
    }

    @Override
    public String toString() {
        return "SykefraværEntitet{" +
            "fraværsPeriode=" + fraværsPeriode +
            '}';
    }
}
