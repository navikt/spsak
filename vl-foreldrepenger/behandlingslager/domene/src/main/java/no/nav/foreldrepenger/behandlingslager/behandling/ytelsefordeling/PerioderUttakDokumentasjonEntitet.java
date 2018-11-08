package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity
@DiscriminatorValue("UTTAK_DOK")
public class PerioderUttakDokumentasjonEntitet extends DokumentasjonPerioderEntitet implements PerioderUttakDokumentasjon {

    @OneToMany(mappedBy = "perioder")
    @ChangeTracked
    private List<PeriodeUttakDokumentasjonEntitet> perioder = new ArrayList<>();


    public PerioderUttakDokumentasjonEntitet() {
        //for
    }

    public PerioderUttakDokumentasjonEntitet(PerioderUttakDokumentasjon perioder) {
        this();
        for (PeriodeUttakDokumentasjon periode : perioder.getPerioder()) {
            leggTil(periode);
        }
    }

    @Override
    public List<PeriodeUttakDokumentasjon> getPerioder() {
        return Collections.unmodifiableList(perioder);
    }

    public void leggTil(PeriodeUttakDokumentasjon periode) {
        final PeriodeUttakDokumentasjonEntitet entitet = new PeriodeUttakDokumentasjonEntitet(periode);
        entitet.setPerioder(this);
        this.perioder.add(entitet);
    }

    public void tilbakestillPerioder() {
        this.perioder = new ArrayList<>();
    }
}
