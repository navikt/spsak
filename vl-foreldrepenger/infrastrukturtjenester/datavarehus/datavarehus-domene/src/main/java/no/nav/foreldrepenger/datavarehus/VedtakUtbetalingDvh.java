package no.nav.foreldrepenger.datavarehus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity(name = "VedtakUtbetalingDvh")
@Table(name = "VEDTAK_UTBETALING_DVH")
public class VedtakUtbetalingDvh extends DvhBaseEntitet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_UTBETALING_DVH")
    @Column(name = "TRANS_ID")
    private Long id;

    @Column(name = "VEDTAK_DATO", nullable = false)
    private LocalDate vedtakDato;
    @Lob
    @Column(name = "XML_CLOB", nullable = false)
    private String xmlClob;

    @Column(name = "FAGSAK_ID", nullable = false)
    private Long fagsakId;

    @Column(name = "VEDTAK_ID", nullable = false)
    private Long vedtakId;

    @Column(name = "BEHANDLING_ID", nullable = false)
    private Long behandlingId;

    @Column(name = "FAGSAK_TYPE", nullable = false)
    private String fagsakType;

    @Column(name = "BEHANDLING_TYPE", nullable = false)
    private String behandlingType;

    @Column(name = "SOEKNAD_TYPE", nullable = false)
    private String søknadType;

    VedtakUtbetalingDvh() {
        //Same procedure as last class miss Sophie?
    }

    public static VedtakUtbetalingDvh.Builder builder() {
        return new VedtakUtbetalingDvh.Builder();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getVedtakDato() {
        return vedtakDato;
    }

    public String getXmlClob() {
        return xmlClob;
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getVedtakId() {
        return vedtakId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getFagsakType() {
        return fagsakType;
    }

    public String getBehandlingType() {
        return behandlingType;
    }

    public String getSøknadType() {
        return søknadType;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VedtakUtbetalingDvh)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        VedtakUtbetalingDvh castOther = (VedtakUtbetalingDvh) other;
        return Objects.equals(vedtakDato, castOther.vedtakDato)
            && Objects.equals(xmlClob, castOther.xmlClob)
            && Objects.equals(fagsakId, castOther.fagsakId)
            && Objects.equals(vedtakId, castOther.vedtakId)
            && Objects.equals(behandlingId, castOther.behandlingId)
            && Objects.equals(fagsakType, castOther.fagsakType)
            && Objects.equals(behandlingType, castOther.behandlingType)
            && Objects.equals(søknadType, castOther.søknadType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vedtakDato, xmlClob, fagsakId, vedtakId, behandlingId, fagsakType,
            behandlingType, søknadType);
    }

    public static class Builder {
        private LocalDate vedtakDato;
        private String xmlClob;
        private Long fagsakId;
        private Long vedtakId;
        private Long behandlingId;
        private String fagsakType;
        private String behandlingType;
        private String søknadType;
        private LocalDateTime funksjonellTid;
        private String endretAv;

        public Builder vedtakDato(LocalDate vedtakDato) {
            this.vedtakDato = vedtakDato;
            return this;
        }

        public Builder xmlClob(String xmlClob) {
            this.xmlClob = xmlClob;
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

        public Builder behandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public Builder fagsakType(String fagsakType) {
            this.fagsakType = fagsakType;
            return this;
        }

        public Builder behandlingType(String behandlingType) {
            this.behandlingType = behandlingType;
            return this;
        }

        public Builder søknadType(String søknadType) {
            this.søknadType = søknadType;
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

        public VedtakUtbetalingDvh build() {
            VedtakUtbetalingDvh vedtakUtbetalingDvh = new VedtakUtbetalingDvh();
            vedtakUtbetalingDvh.vedtakDato = vedtakDato;
            vedtakUtbetalingDvh.xmlClob = xmlClob;
            vedtakUtbetalingDvh.fagsakId = fagsakId;
            vedtakUtbetalingDvh.vedtakId = vedtakId;
            vedtakUtbetalingDvh.behandlingId = behandlingId;
            vedtakUtbetalingDvh.fagsakType = fagsakType;
            vedtakUtbetalingDvh.behandlingType = behandlingType;
            vedtakUtbetalingDvh.søknadType = søknadType;
            vedtakUtbetalingDvh.setFunksjonellTid(funksjonellTid);
            vedtakUtbetalingDvh.setEndretAv(endretAv);

            return vedtakUtbetalingDvh;
        }
    }
}
