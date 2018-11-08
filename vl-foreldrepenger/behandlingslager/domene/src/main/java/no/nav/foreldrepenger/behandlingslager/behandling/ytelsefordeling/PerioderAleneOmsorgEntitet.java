package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity
@DiscriminatorValue("ALENEOMSORG")
public class PerioderAleneOmsorgEntitet extends DokumentasjonPerioderEntitet implements PerioderAleneOmsorg {

    @OneToMany(mappedBy = "perioder")
    @ChangeTracked
    private List<PeriodeAleneOmsorgEntitet> perioder = new ArrayList<>();


    public PerioderAleneOmsorgEntitet() {
        //for
    }

    public PerioderAleneOmsorgEntitet(PerioderAleneOmsorg perioder) {
        this();
        for (PeriodeAleneOmsorg periode : perioder.getPerioder()) {
            leggTil(periode);
        }
    }

    @Override
    public List<PeriodeAleneOmsorg> getPerioder() {
        return Collections.unmodifiableList(perioder);
    }

    public void leggTil(PeriodeAleneOmsorg periode) {
        final PeriodeAleneOmsorgEntitet entitet = new PeriodeAleneOmsorgEntitet(periode);
        entitet.setPerioder(this);
        this.perioder.add(entitet);
    }

}
