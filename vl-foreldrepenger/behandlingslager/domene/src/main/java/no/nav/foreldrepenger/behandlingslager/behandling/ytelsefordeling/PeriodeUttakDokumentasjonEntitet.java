package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("UTTAK_DOK")
public class PeriodeUttakDokumentasjonEntitet extends DokumentasjonPeriodeEntitet<PeriodeUttakDokumentasjon> implements PeriodeUttakDokumentasjon {

    @ManyToOne(optional = false)
    @JoinColumn(name = "perioder_id", nullable = false, updatable = false, unique = true)
    private PerioderUttakDokumentasjonEntitet perioder;

    public PeriodeUttakDokumentasjonEntitet() {
        // For hibernate
    }

    public PeriodeUttakDokumentasjonEntitet(LocalDate fom, LocalDate tom, UttakDokumentasjonType dokumentasjonType) {
        super(fom, tom, dokumentasjonType);
    }

    PeriodeUttakDokumentasjonEntitet(PeriodeUttakDokumentasjon periode) {
        super(periode);
    }

    void setPerioder(PerioderUttakDokumentasjonEntitet perioder) {
        this.perioder = perioder;
    }
}
