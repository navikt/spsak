package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity
@DiscriminatorValue("UTEN_OMSORG")
public class PerioderUtenOmsorgEntitet extends DokumentasjonPerioderEntitet implements PerioderUtenOmsorg {

    @OneToMany(mappedBy = "perioder")
    @ChangeTracked
    private List<PeriodeUtenOmsorgEntitet> perioder = new ArrayList<>();


    public PerioderUtenOmsorgEntitet() {
        //for
    }

    public PerioderUtenOmsorgEntitet(PerioderUtenOmsorg perioder) {
        this();
        for (PeriodeUtenOmsorg periode : perioder.getPerioder()) {
            leggTil(periode);
        }
    }

    @Override
    public List<PeriodeUtenOmsorg> getPerioder() {
        return Collections.unmodifiableList(perioder);
    }

    public void leggTil(PeriodeUtenOmsorg periode) {
        final PeriodeUtenOmsorgEntitet entitet = new PeriodeUtenOmsorgEntitet(periode);
        entitet.setPerioder(this);
        this.perioder.add(entitet);
    }

}
