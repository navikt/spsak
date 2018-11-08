package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "FagsakDvh")
@Table(name = "FAGSAK_DVH")
public class FagsakDvh extends DvhBaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK_DVH")
    @Column(name="TRANS_ID")
    private Long id;

    @Column(name = "FAGSAK_ID", nullable = false)
    private Long fagsakId;

    @Column(name = "BRUKER_ID", nullable = false)
    private Long brukerId;

    @Column(name = "BRUKER_AKTOER_ID", nullable = false)
    private String brukerAktørId;

    @Column(name = "EPS_AKTOER_ID")
    private String epsAktørId;

    @Column(name = "OPPRETTET_DATO", nullable = false)
    private LocalDate opprettetDato;

    @Column(name = "SAKSNUMMER", nullable = true)
    private Long saksnummer;

    @Column(name = "FAGSAK_STATUS", nullable = true)
    private String fagsakStatus;

    @Column(name = "FAGSAK_YTELSE", nullable = true)
    private String fagsakYtelse;

    @Column(name = "FAGSAK_AARSAK", nullable = true)
    private String fagsakAarsak;

    FagsakDvh() {
        // hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getBrukerId() {
        return brukerId;
    }

    public String getBrukerAktørId() {
        return brukerAktørId;
    }

    public String getEpsAktørId() {
        return epsAktørId;
    }

    public LocalDate getOpprettetDato() {
        return opprettetDato;
    }

    public Long getSaksnummer() {
        return saksnummer;
    }

    public String getFagsakStatus() {
        return fagsakStatus;
    }

    public String getFagsakYtelse() {
        return fagsakYtelse;
    }

    public String getFagsakAarsak() {
        return fagsakAarsak;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FagsakDvh)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        FagsakDvh castOther = (FagsakDvh) other;
        return Objects.equals(fagsakId, castOther.fagsakId)
                && Objects.equals(brukerId, castOther.brukerId)
                && Objects.equals(brukerAktørId, castOther.brukerAktørId)
                && Objects.equals(epsAktørId, castOther.epsAktørId)
                && Objects.equals(opprettetDato, castOther.opprettetDato)
                && Objects.equals(saksnummer, castOther.saksnummer)
                && Objects.equals(fagsakStatus, castOther.fagsakStatus)
                && Objects.equals(fagsakYtelse, castOther.fagsakYtelse)
                && Objects.equals(fagsakAarsak, castOther.fagsakAarsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fagsakId, brukerId, brukerAktørId, epsAktørId, opprettetDato, saksnummer, fagsakStatus,
                fagsakYtelse, fagsakAarsak);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long fagsakId;
        private Long brukerId;
        private String brukerAktørId;
        private String epsAktørId;
        private LocalDate opprettetDato;
        private Long saksnummer;
        private String fagsakStatus;
        private String fagsakYtelse;
        private String fagsakAarsak;
        private LocalDateTime funksjonellTid;
        private String endretAv;

        public Builder fagsakId(Long fagsakId) {
            this.fagsakId = fagsakId;
            return this;
        }

        public Builder brukerId(Long brukerId) {
            this.brukerId = brukerId;
            return this;
        }

        public Builder brukerAktørId(String brukerAktørId) {
            this.brukerAktørId = brukerAktørId;
            return this;
        }

        public Builder epsAktørId(Optional<String> epsAktørId) {
            epsAktørId.ifPresent(aLong -> this.epsAktørId = aLong);
            return this;
        }

        public Builder opprettetDato(LocalDate opprettetDato) {
            this.opprettetDato = opprettetDato;
            return this;
        }

        public Builder saksnummer(Long saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        public Builder saksnummer(String saksnummer) {
            if(saksnummer != null) {
                this.saksnummer = Long.parseLong(saksnummer);
            } else {
                this.saksnummer = null;
            }
            return this;
        }

        public Builder fagsakStatus(String fagsakStatus) {
            this.fagsakStatus = fagsakStatus;
            return this;
        }

        public Builder fagsakYtelse(String fagsakYtelse) {
            this.fagsakYtelse = fagsakYtelse;
            return this;
        }

        public Builder fagsakAarsak(String fagsakAarsak) {
            this.fagsakAarsak = fagsakAarsak;
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

        public FagsakDvh build() {
            FagsakDvh fagsakDvh = new FagsakDvh();
            fagsakDvh.fagsakId = fagsakId;
            fagsakDvh.brukerId = brukerId;
            fagsakDvh.brukerAktørId = brukerAktørId;
            fagsakDvh.epsAktørId = epsAktørId;
            fagsakDvh.opprettetDato = opprettetDato;
            fagsakDvh.saksnummer = saksnummer;
            fagsakDvh.fagsakStatus = fagsakStatus;
            fagsakDvh.fagsakYtelse = fagsakYtelse;
            fagsakDvh.fagsakAarsak = fagsakAarsak;
            fagsakDvh.setFunksjonellTid(funksjonellTid);
            fagsakDvh.setEndretAv(endretAv);
            return fagsakDvh;
        }
    }
}
