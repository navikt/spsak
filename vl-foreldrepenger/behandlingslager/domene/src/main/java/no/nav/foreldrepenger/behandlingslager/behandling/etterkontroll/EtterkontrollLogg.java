package no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "EtterkontrollLog")
@Table(name = "ETTERKONTROLL_LOGG")
public class EtterkontrollLogg extends BaseEntitet {

    @Id
    @SequenceGenerator(name = "etterkontroll_logg_sekvens", sequenceName = "SEQ_ETTERKONTROLL_LOGG")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "etterkontroll_logg_sekvens")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    EtterkontrollLogg() {
        // hibernarium
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public static class Builder {
        private Behandling behandling;

        public Builder(Behandling behandling) {
            Objects.requireNonNull(behandling, "behandling");
            this.behandling = behandling;
        }

        public EtterkontrollLogg build() {
            EtterkontrollLogg etterkontrollLog = new EtterkontrollLogg();
            etterkontrollLog.behandling = this.behandling;

            return etterkontrollLog;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EtterkontrollLogg)) {
            return false;
        }
        EtterkontrollLogg that = (EtterkontrollLogg) o;
        return Objects.equals(getBehandling(), that.getBehandling());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBehandling());
    }
}
