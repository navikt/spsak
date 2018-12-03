package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class InngåendeSaksdokument {

    private Long fagsakId;
    private Long behandlingId;
    private DokumentKategori dokumentKategori;
    private DokumentTypeId dokumentTypeId;
    private JournalpostId journalpostId;
    private String dokumentId;
    private BehandlingTema behandlingTema;
    private LocalDate forsendelseMottatt;
    private PayloadType payloadType;
    private String payloadAsBase64;
    private String behandlingÅrsakType;
    private UUID forsendelseId;
    private String journalEnhet;

    private InngåendeSaksdokument() {
        // Skjult.
    }

    @JsonIgnore
    public static Builder builder() {
        return new Builder();
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public BehandlingTema getBehandlingTema() {
        return behandlingTema;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public LocalDate getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    @JsonIgnore
    public boolean harPayload() {
        return payloadAsBase64 != null;
    }

    @JsonIgnore
    public String getPayload() {
        if (harPayload()) {
            byte[] bytes = Base64.getDecoder().decode(payloadAsBase64);
            return new String(bytes, Charset.forName("UTF-8"));
        }
        return payloadAsBase64;
    }

    public String getPayloadAsBase64() {
        return payloadAsBase64;
    }

    public String getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
    }

    public String getJournalEnhet() {
        return journalEnhet;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "InngåendeSaksdokument{" +
            "fagsakId=" + fagsakId +
            ", behandlingId=" + behandlingId +
            ", journalpostId=" + journalpostId +
            ", dokumentTypeId=" + dokumentTypeId +
            ", forsendelseMottatt=" + forsendelseMottatt +
            ", dokumentKategori=" + dokumentKategori +
            '}';
    }

    public static class Builder {
        private Long fagsakId;
        private Long behandlingId;
        private JournalpostId journalpostId;
        private BehandlingTema behandlingTema;
        private DokumentTypeId dokumentTypeId;
        private LocalDate forsendelseMottatt;
        private String payload;
        private PayloadType payloadType;
        private String behandlingÅrsakType;
        private UUID forsendelseId;
        private DokumentKategori dokumentKategori;
        private String journalEnhet;
        private String dokumentId;

        public InngåendeSaksdokument.Builder medFagsakId(Long fagsakId) {
            this.fagsakId = fagsakId;
            return this;
        }

        public InngåendeSaksdokument.Builder medBehandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
            return this;
        }

        public InngåendeSaksdokument.Builder medJournalpostId(JournalpostId journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public InngåendeSaksdokument.Builder medForsendelseId(UUID forsendelseId) {
            this.forsendelseId = forsendelseId;
            return this;
        }

        public InngåendeSaksdokument.Builder medBehandlingTema(BehandlingTema behandlingTema) {
            this.behandlingTema = behandlingTema;
            return this;
        }

        public InngåendeSaksdokument.Builder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public InngåendeSaksdokument.Builder medDokumentKategori(DokumentKategori dokumentKategori) {
            this.dokumentKategori = dokumentKategori;
            return this;
        }

        public InngåendeSaksdokument.Builder medBehandlingÅrsak(String behandlingÅrsakType) {
            this.behandlingÅrsakType = behandlingÅrsakType;
            return this;
        }

        public InngåendeSaksdokument.Builder medMottattDato(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public InngåendeSaksdokument.Builder medJournalførendeEnhet(String journalEnhet) {
            this.journalEnhet = journalEnhet;
            return this;
        }

        public InngåendeSaksdokument.Builder medDokumentId(String dokumentId) {
            this.dokumentId = dokumentId;
            return this;
        }

        public InngåendeSaksdokument.Builder medPayload(PayloadType type, String payload) {
            if (payload != null) {
                this.payload = Base64.getEncoder().encodeToString(payload.getBytes(Charset.forName("UTF-8")));
                this.payloadType = type;
            }
            return this;
        }

        public InngåendeSaksdokument.Builder medPayloadAsBase64(PayloadType type, String payload) {
            this.payload = payload;
            this.payloadType = type;
            return this;
        }

        public InngåendeSaksdokument build() {
            InngåendeSaksdokument saksdokument = new InngåendeSaksdokument();

            saksdokument.fagsakId = this.fagsakId;
            saksdokument.behandlingId = this.behandlingId;
            saksdokument.journalpostId = this.journalpostId;
            saksdokument.dokumentId = this.dokumentId;
            saksdokument.behandlingTema = this.behandlingTema;
            saksdokument.dokumentTypeId = this.dokumentTypeId;
            saksdokument.forsendelseMottatt = this.forsendelseMottatt;
            saksdokument.payloadAsBase64 = this.payload;
            saksdokument.payloadType = this.payloadType;
            saksdokument.behandlingÅrsakType = this.behandlingÅrsakType;
            saksdokument.forsendelseId = this.forsendelseId;
            saksdokument.dokumentKategori = this.dokumentKategori;
            saksdokument.journalEnhet = this.journalEnhet;
            return saksdokument;
        }

    }
}
