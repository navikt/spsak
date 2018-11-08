package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "BehandlingStegDvh")
@Table(name = "BEHANDLING_STEG_DVH")
public class BehandlingStegDvh extends DvhBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_STEG_DVH")
    @Column(name="TRANS_ID")
    private Long id;

    @Column(name = "BEHANDLING_STEG_ID", nullable = true)
    private Long behandlingStegId;

    @Column(name = "BEHANDLING_ID", nullable = false)
    private Long behandlingId;

    @Column(name = "BEHANDLING_STEG_TYPE", nullable = true)
    private String behandlingStegType;

    @Column(name = "BEHANDLING_STEG_STATUS", nullable = true)
    private String behandlingStegStatus;

    BehandlingStegDvh() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingStegId() {
        return behandlingStegId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getBehandlingStegType() {
        return behandlingStegType;
    }

    public String getBehandlingStegStatus() {
        return behandlingStegStatus;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BehandlingStegDvh)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        BehandlingStegDvh castOther = (BehandlingStegDvh) other;
        return Objects.equals(behandlingStegId, castOther.behandlingStegId)
                && Objects.equals(behandlingId, castOther.behandlingId)
                && Objects.equals(behandlingStegType, castOther.behandlingStegType)
                && Objects.equals(behandlingStegStatus, castOther.behandlingStegStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), behandlingStegId, behandlingId, behandlingStegType, behandlingStegStatus);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long behandlingStegId;
        private Long behandlingId;
        private String behandlingStegType;
        private String behandlingStegStatus;
        private LocalDateTime funksjonellTid;
        private String endretAv;

        public Builder behandlingStegId(Long behandlingStegId) {
            this.behandlingStegId = behandlingStegId;
            return this;
        }

        public Builder behandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder behandlingStegType(String behandlingStegType) {
            this.behandlingStegType = behandlingStegType;
            return this;
        }

        public Builder behandlingStegStatus(String behandlingStegStatus) {
            this.behandlingStegStatus = behandlingStegStatus;
            return this;
        }

        public Builder funksjonellTid(LocalDateTime funksjonellTid) {
            this.funksjonellTid = funksjonellTid;
            return this;
        }

        public Builder endretAv(String endretAv) {
            this.endretAv = endretAv;
            return this;
        }

        public BehandlingStegDvh build() {
            BehandlingStegDvh behandlingStegDvh = new BehandlingStegDvh();
            behandlingStegDvh.behandlingStegId = behandlingStegId;
            behandlingStegDvh.behandlingId = behandlingId;
            behandlingStegDvh.behandlingStegType = behandlingStegType;
            behandlingStegDvh.behandlingStegStatus = behandlingStegStatus;
            behandlingStegDvh.setFunksjonellTid(funksjonellTid);
            behandlingStegDvh.setEndretAv(endretAv);
            return behandlingStegDvh;
        }
    }
}
