package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("ALENEOMSORG")
public class PeriodeAleneOmsorgEntitet extends DokumentasjonPeriodeEntitet<PeriodeAleneOmsorg> implements PeriodeAleneOmsorg {

    @ManyToOne(optional = false)
    @JoinColumn(name = "perioder_id", nullable = false, updatable = false, unique = true)
    private PerioderAleneOmsorgEntitet perioder;

    public PeriodeAleneOmsorgEntitet() {
        // For hibernate
    }

    public PeriodeAleneOmsorgEntitet(LocalDate fom, LocalDate tom) {
        super(fom, tom, UttakDokumentasjonType.ALENEOMSORG);
    }

    PeriodeAleneOmsorgEntitet(PeriodeAleneOmsorg periode) {
        super(periode);
    }

    void setPerioder(PerioderAleneOmsorgEntitet perioder) {
        this.perioder = perioder;
    }
}
