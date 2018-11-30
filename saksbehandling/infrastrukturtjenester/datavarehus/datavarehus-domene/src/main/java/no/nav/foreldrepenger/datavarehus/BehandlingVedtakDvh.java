package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "BehandlingVedtakDvh")
@Table(name = "BEHANDLING_VEDTAK_DVH")
public class BehandlingVedtakDvh extends DvhBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_VEDTAK_DVH")
    @Column(name="TRANS_ID")
    private Long id;

    @Column(name = "VEDTAK_ID", nullable = false)
    private Long vedtakId;

    @Column(name = "BEHANDLING_ID", nullable = false)
    private Long behandlingId;

    @Column(name = "OPPRETTET_DATO", nullable = false)
    private LocalDate opprettetDato;

    @Column(name = "VEDTAK_DATO", nullable = false)
    private LocalDate vedtakDato;

    @Column(name = "IVERKSETTING_STATUS", nullable = false)
    private String iverksettingStatus;

    @Column(name = "GODKJENNENDE_ENHET", nullable = true)
    private String godkjennendeEnhet;

    @Column(name = "ANSVARLIG_SAKSBEHANDLER", nullable = true)
    private String ansvarligSaksbehandler;

    @Column(name = "ANSVARLIG_BESLUTTER", nullable = true)
    private String ansvarligBeslutter;
    
    @Column(name = "VEDTAK_RESULTAT_TYPE_KODE", nullable = true)
    public String vedtakResultatTypeKode;

    BehandlingVedtakDvh() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getVedtakId() {
        return vedtakId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public LocalDate getOpprettetDato() {
        return opprettetDato;
    }

    public LocalDate getVedtakDato() {
        return vedtakDato;
    }

    public String getIverksettingStatus() {
        return iverksettingStatus;
    }

    public String getGodkjennendeEnhet() {
        return godkjennendeEnhet;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public String getVedtakResultatTypeKode() {
        return vedtakResultatTypeKode;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BehandlingVedtakDvh)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        BehandlingVedtakDvh castOther = (BehandlingVedtakDvh) other;
        return Objects.equals(vedtakId, castOther.vedtakId)
            && Objects.equals(behandlingId, castOther.behandlingId)
                && Objects.equals(opprettetDato, castOther.opprettetDato)
            && Objects.equals(vedtakDato, castOther.vedtakDato)
                && Objects.equals(iverksettingStatus, castOther.iverksettingStatus)
                && Objects.equals(godkjennendeEnhet, castOther.godkjennendeEnhet)
                && Objects.equals(ansvarligSaksbehandler, castOther.ansvarligSaksbehandler)
                && Objects.equals(ansvarligBeslutter, castOther.ansvarligBeslutter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vedtakId, behandlingId, opprettetDato, vedtakDato, iverksettingStatus, godkjennendeEnhet,
                ansvarligSaksbehandler, ansvarligBeslutter);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long vedtakId;
        private Long behandlingId;
        private LocalDate opprettetDato;
        private LocalDate vedtakDato;
        private String iverksettingStatus;
        private String godkjennendeEnhet;
        private String ansvarligSaksbehandler;
        private String ansvarligBeslutter;
        private LocalDateTime funksjonellTid;
        private String endretAv;
        private String vedtakResultatTypeKode;

        public Builder vedtakId(Long vedtakId) {
            this.vedtakId = vedtakId;
            return this;
        }

        public Builder behandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder opprettetDato(LocalDate opprettetDato) {
            this.opprettetDato = opprettetDato;
            return this;
        }

        public Builder vedtakDato(LocalDate vedtakDato) {
            this.vedtakDato = vedtakDato;
            return this;
        }

        public Builder iverksettingStatus(String iverksettingStatus) {
            this.iverksettingStatus = iverksettingStatus;
            return this;
        }

        public Builder godkjennendeEnhet(String godkjennendeEnhet) {
            this.godkjennendeEnhet = godkjennendeEnhet;
            return this;
        }

        public Builder ansvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Builder ansvarligBeslutter(String ansvarligBeslutter) {
            this.ansvarligBeslutter = ansvarligBeslutter;
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
        
        public Builder vedtakResultatTypeKode(String vedtakResultatTypeKode) {
            this.vedtakResultatTypeKode = vedtakResultatTypeKode;
            return this;
        }

        public BehandlingVedtakDvh build() {
            BehandlingVedtakDvh vedtak = new BehandlingVedtakDvh();
            vedtak.ansvarligBeslutter = ansvarligBeslutter;
            vedtak.ansvarligSaksbehandler = ansvarligSaksbehandler;
            vedtak.behandlingId = behandlingId;
            vedtak.godkjennendeEnhet = godkjennendeEnhet;
            vedtak.iverksettingStatus = iverksettingStatus;
            vedtak.opprettetDato = opprettetDato;
            vedtak.vedtakId = vedtakId;
            vedtak.vedtakDato = vedtakDato;
            vedtak.vedtakResultatTypeKode = vedtakResultatTypeKode;
            vedtak.setFunksjonellTid(funksjonellTid);
            vedtak.setEndretAv(endretAv);
            return vedtak;
        }

        
    }
}
