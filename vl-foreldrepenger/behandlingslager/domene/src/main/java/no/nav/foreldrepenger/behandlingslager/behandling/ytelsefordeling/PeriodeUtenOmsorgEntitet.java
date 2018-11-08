package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("UTEN_OMSORG")
public class PeriodeUtenOmsorgEntitet extends DokumentasjonPeriodeEntitet<PeriodeUtenOmsorg> implements PeriodeUtenOmsorg {

    @ManyToOne(optional = false)
    @JoinColumn(name = "perioder_id", nullable = false, updatable = false, unique = true)
    private PerioderUtenOmsorgEntitet perioder;

    public PeriodeUtenOmsorgEntitet() {
        // For hibernate
    }

    public PeriodeUtenOmsorgEntitet(LocalDate fom, LocalDate tom) {
        super(fom, tom, UttakDokumentasjonType.UTEN_OMSORG);
    }

    PeriodeUtenOmsorgEntitet(PeriodeUtenOmsorg periode) {
        super(periode);
    }

    void setPerioder(PerioderUtenOmsorgEntitet perioder) {
        this.perioder = perioder;
    }
}
