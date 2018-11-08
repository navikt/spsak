package no.nav.foreldrepenger.behandlingslager.lagretvedtak;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class LagretVedtakMedBehandlingType {

    // Id er behandlingId på lagret vedtak
    private Long id;
    private String behandlingType;
    private LocalDate opprettetDato;

    public LagretVedtakMedBehandlingType(BigDecimal id, String behandlingType, Object opprettetDato) {
        this.id = id.longValue();
        this.behandlingType = behandlingType;
        this.opprettetDato =    ((Timestamp)opprettetDato).toLocalDateTime().toLocalDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBehandlingType() {
        return behandlingType;
    }

    public void setBehandlingType(String behandlingType) {
        this.behandlingType = behandlingType;
    }

    public LocalDate getOpprettetDato() {
        return opprettetDato;
    }

    public void setOpprettetDato(LocalDate opprettetDato) {
        this.opprettetDato = opprettetDato;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LagretVedtakMedBehandlingType)) {
            return false;
        }

        LagretVedtakMedBehandlingType that = (LagretVedtakMedBehandlingType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, behandlingType, opprettetDato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "id=" + id + ", behandlingType='" + behandlingType +
            ", opprettetDato=" + opprettetDato + ">";
    }
}
