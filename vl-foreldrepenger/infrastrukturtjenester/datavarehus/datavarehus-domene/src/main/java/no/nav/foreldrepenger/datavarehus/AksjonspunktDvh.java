package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "AksjonspunktDvh")
@Table(name = "AKSJONSPUNKT_DVH")
public class AksjonspunktDvh extends DvhBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKSJONSPUNKT_DVH")
    @Column(name="TRANS_ID")
    private Long id;

    @Column(name = "BEHANDLING_STEG_ID", nullable = true)
    private Long behandlingStegId;

    @Column(name = "AKSJONSPUNKT_ID", nullable = false)
    private Long aksjonspunktId;

    @Column(name = "BEHANDLING_ID", nullable = false)
    private Long behandlingId;

    @Column(name = "BEHANDLENDE_ENHET_KODE", nullable = true)
    private String behandlendeEnhetKode;

    @Column(name = "ANSVARLIG_BESLUTTER", nullable = true)
    private String ansvarligBeslutter;

    @Column(name = "ANSVARLIG_SAKSBEHANDLER", nullable = true)
    private String ansvarligSaksbehandler;

    @Column(name = "AKSJONSPUNKT_DEF", nullable = false)
    private String aksjonspunktDef;

    @Column(name = "AKSJONSPUNKT_STATUS", nullable = false)
    private String aksjonspunktStatus;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING")
    private boolean toTrinnsBehandling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING_GODKJENT")
    private Boolean toTrinnsBehandlingGodkjent;

    AksjonspunktDvh() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingStegId() {
        return behandlingStegId;
    }

    public Long getAksjonspunktId() {
        return aksjonspunktId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getBehandlendeEnhetKode() {
        return behandlendeEnhetKode;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getAksjonspunktDef() {
        return aksjonspunktDef;
    }

    public String getAksjonspunktStatus() {
        return aksjonspunktStatus;
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public Boolean getToTrinnsBehandlingGodkjent() {
        return toTrinnsBehandlingGodkjent;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AksjonspunktDvh)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        AksjonspunktDvh other = (AksjonspunktDvh) obj;
        return Objects.equals(behandlingStegId, other.behandlingStegId)
                && Objects.equals(aksjonspunktId, other.aksjonspunktId)
                && Objects.equals(behandlingId, other.behandlingId)
                && Objects.equals(behandlendeEnhetKode, other.behandlendeEnhetKode)
                && Objects.equals(ansvarligBeslutter, other.ansvarligBeslutter)
                && Objects.equals(ansvarligSaksbehandler, other.ansvarligSaksbehandler)
                && Objects.equals(aksjonspunktDef, other.aksjonspunktDef)
                && Objects.equals(aksjonspunktStatus, other.aksjonspunktStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), behandlingStegId, aksjonspunktId, behandlingId,
                behandlendeEnhetKode, ansvarligBeslutter, ansvarligSaksbehandler, aksjonspunktDef, aksjonspunktStatus);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long behandlingStegId;
        private Long aksjonspunktId;
        private Long behandlingId;
        private String behandlendeEnhetKode;
        private String ansvarligBeslutter;
        private String ansvarligSaksbehandler;
        private String aksjonspunktDef;
        private String aksjonspunktStatus;
        private LocalDateTime funksjonellTid;
        private String endretAv;
        private boolean toTrinnsBehandling;
        private Boolean toTrinnsBehandlingGodkjent;

        public Builder behandlingStegId(Long behandlingStegId) {
            this.behandlingStegId = behandlingStegId;
            return this;
        }

        public Builder aksjonspunktId(Long aksjonspunktId) {
            this.aksjonspunktId = aksjonspunktId;
            return this;
        }

        public Builder behandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder behandlendeEnhetKode(String behandlendeEnhetKode) {
            this.behandlendeEnhetKode = behandlendeEnhetKode;
            return this;
        }

        public Builder ansvarligBeslutter(String ansvarligBeslutter) {
            this.ansvarligBeslutter = ansvarligBeslutter;
            return this;
        }

        public Builder ansvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Builder aksjonspunktDef(String aksjonspunktDef) {
            this.aksjonspunktDef = aksjonspunktDef;
            return this;
        }

        public Builder aksjonspunktStatus(String aksjonspunktStatus) {
            this.aksjonspunktStatus = aksjonspunktStatus;
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

        public Builder toTrinnsBehandling(boolean toTrinnsBehandling) {
            this.toTrinnsBehandling = toTrinnsBehandling;
            return this;
        }

        public Builder toTrinnsBehandlingGodkjent(Boolean toTrinnsBehandlingGodkjent) {
            this.toTrinnsBehandlingGodkjent = toTrinnsBehandlingGodkjent;
            return this;
        }

        public AksjonspunktDvh build() {
            AksjonspunktDvh aksjonspunktDvh = new AksjonspunktDvh();
            aksjonspunktDvh.behandlingStegId = behandlingStegId;
            aksjonspunktDvh.aksjonspunktId = aksjonspunktId;
            aksjonspunktDvh.behandlingId = behandlingId;
            aksjonspunktDvh.behandlendeEnhetKode = behandlendeEnhetKode;
            aksjonspunktDvh.ansvarligBeslutter = ansvarligBeslutter;
            aksjonspunktDvh.ansvarligSaksbehandler = ansvarligSaksbehandler;
            aksjonspunktDvh.aksjonspunktDef = aksjonspunktDef;
            aksjonspunktDvh.aksjonspunktStatus = aksjonspunktStatus;
            aksjonspunktDvh.toTrinnsBehandling = toTrinnsBehandling;
            aksjonspunktDvh.toTrinnsBehandlingGodkjent = toTrinnsBehandlingGodkjent;
            aksjonspunktDvh.setFunksjonellTid(funksjonellTid);
            aksjonspunktDvh.setEndretAv(endretAv);
            return aksjonspunktDvh;
        }
    }
}
