package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDate;
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

@Entity(name = "BehandlingDvh")
@Table(name = "BEHANDLING_DVH")
public class BehandlingDvh extends DvhBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_DVH")
    @Column(name="TRANS_ID")
    private Long id;

    @Column(name = "BEHANDLING_ID", nullable = false)
    private Long behandlingId;

    @Column(name = "FAGSAK_ID", nullable = false)
    private Long fagsakId;

    @Column(name = "VEDTAK_ID", nullable = true)
    private Long vedtakId;

    @Column(name = "OPPRETTET_DATO", nullable = false)
    private LocalDate opprettetDato;

    @Column(name = "BEHANDLING_RESULTAT_TYPE", nullable = false)
    private String behandlingResultatType;

    @Column(name = "BEHANDLING_TYPE", nullable = false)
    private String behandlingType;

    @Column(name = "BEHANDLING_STATUS", nullable = false)
    private String behandlingStatus;

    @Column(name = "BEHANDLENDE_ENHET", nullable = false)
    private String behandlendeEnhet;

    @Column(name = "UTLANDSTILSNITT", nullable = false)
    private String utlandstilsnitt;

    @Column(name = "ANSVARLIG_SAKSBEHANDLER", nullable = true)
    private String ansvarligSaksbehandler;

    @Column(name = "ANSVARLIG_BESLUTTER", nullable = true)
    private String ansvarligBeslutter;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINNSBEHANDLING")
    private boolean toTrinnsBehandling;

    @Column(name = "RELATERT_TIL", nullable = true)
    private Long relatertBehandling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "FERDIG")
    private boolean ferdig;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "VEDTATT")
    private boolean vedtatt;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "AVBRUTT")
    private boolean avbrutt;

    BehandlingDvh() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getVedtakId() {
        return vedtakId;
    }

    public LocalDate getOpprettetDato() {
        return opprettetDato;
    }

    public String getBehandlingResultatType() {
        return behandlingResultatType;
    }

    public String getBehandlingType() {
        return behandlingType;
    }

    public String getBehandlingStatus() {
        return behandlingStatus;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public String getUtlandstilsnitt() {
        return utlandstilsnitt;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public Long getRelatertBehandling() {
        return relatertBehandling;
    }

    public boolean isFerdig() {
        return ferdig;
    }

    public boolean isVedtatt() {
        return vedtatt;
    }

    public boolean isAvbrutt() {
        return avbrutt;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BehandlingDvh)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        BehandlingDvh other = (BehandlingDvh) obj;
        return Objects.equals(behandlingId, other.behandlingId)
                && Objects.equals(fagsakId, other.fagsakId)
                && Objects.equals(vedtakId, other.vedtakId)
                && Objects.equals(opprettetDato, other.opprettetDato)
                && Objects.equals(behandlingResultatType, other.behandlingResultatType)
                && Objects.equals(behandlingType, other.behandlingType)
                && Objects.equals(behandlingStatus, other.behandlingStatus)
                && Objects.equals(behandlendeEnhet, other.behandlendeEnhet)
                && Objects.equals(utlandstilsnitt, other.utlandstilsnitt)
                && Objects.equals(ansvarligSaksbehandler, other.ansvarligSaksbehandler)
                && Objects.equals(ansvarligBeslutter, other.ansvarligBeslutter)
                && Objects.equals(relatertBehandling, other.relatertBehandling)
                && Objects.equals(ferdig, other.ferdig)
                && Objects.equals(vedtatt, other.vedtatt)
                && Objects.equals(avbrutt, other.avbrutt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), behandlingId, fagsakId, vedtakId, opprettetDato, behandlingResultatType, behandlingType,
                behandlingStatus, behandlendeEnhet, utlandstilsnitt, ansvarligSaksbehandler, ansvarligBeslutter);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long behandlingId;
        private Long fagsakId;
        private Long vedtakId;
        private LocalDate opprettetDato;
        private String behandlingResultatType;
        private String behandlingType;
        private String behandlingStatus;
        private String behandlendeEnhet;
        private String utlandstilsnitt;
        private String ansvarligSaksbehandler;
        private String ansvarligBeslutter;
        private LocalDateTime funksjonellTid;
        private String endretAv;
        private boolean toTrinnsBehandling;
        private Long relatertBehandling;
        private boolean ferdig;
        private boolean vedtatt;
        private boolean avbrutt;

        public Builder behandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder fagsakId(Long fagsakId) {
            this.fagsakId = fagsakId;
            return this;
        }

        public Builder vedtakId(Long vedtakId) {
            this.vedtakId = vedtakId;
            return this;
        }

        public Builder opprettetDato(LocalDate opprettetDato) {
            this.opprettetDato = opprettetDato;
            return this;
        }

        public Builder behandlingResultatType(String behandlingResultatType) {
            this.behandlingResultatType = behandlingResultatType;
            return this;
        }

        public Builder behandlingType(String behandlingType) {
            this.behandlingType = behandlingType;
            return this;
        }

        public Builder behandlingStatus(String behandlingStatus) {
            this.behandlingStatus = behandlingStatus;
            return this;
        }

        public Builder behandlendeEnhet(String behandlendeEnhet) {
            this.behandlendeEnhet = behandlendeEnhet;
            return this;
        }

        public Builder utlandstilsnitt(String utlandstilsnitt) {
            this.utlandstilsnitt = utlandstilsnitt;
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

        public Builder toTrinnsBehandling(boolean toTrinnsBehandling) {
            this.toTrinnsBehandling = toTrinnsBehandling;
            return this;
        }

        public Builder relatertBehandling(Long behandlingId) {
            this.relatertBehandling = behandlingId;
            return this;
        }

        public Builder ferdig(boolean ferdig) {
            this.ferdig = ferdig;
            return this;
        }

        public Builder vedtatt(boolean vedtatt) {
            this.vedtatt = vedtatt;
            return this;
        }

        public Builder avbrutt(boolean avbrutt) {
            this.avbrutt = avbrutt;
            return this;
        }

        public BehandlingDvh build() {
            BehandlingDvh behandlingDvh = new BehandlingDvh();
            behandlingDvh.behandlingId = behandlingId;
            behandlingDvh.fagsakId = fagsakId;
            behandlingDvh.vedtakId = vedtakId;
            behandlingDvh.opprettetDato = opprettetDato;
            behandlingDvh.behandlingResultatType = behandlingResultatType;
            behandlingDvh.behandlingType = behandlingType;
            behandlingDvh.behandlingStatus = behandlingStatus;
            behandlingDvh.behandlendeEnhet = behandlendeEnhet;
            behandlingDvh.utlandstilsnitt = utlandstilsnitt;
            behandlingDvh.ansvarligSaksbehandler = ansvarligSaksbehandler;
            behandlingDvh.ansvarligBeslutter = ansvarligBeslutter;
            behandlingDvh.toTrinnsBehandling = toTrinnsBehandling;
            behandlingDvh.relatertBehandling = relatertBehandling;
            behandlingDvh.ferdig = ferdig;
            behandlingDvh.vedtatt = vedtatt;
            behandlingDvh.avbrutt = avbrutt;
            behandlingDvh.setFunksjonellTid(funksjonellTid);
            behandlingDvh.setEndretAv(endretAv);
            return behandlingDvh;
        }
    }
}
