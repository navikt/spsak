package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import java.time.LocalDate;
import java.util.UUID;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public class InngåendeSaksdokument {

    private Long fagsakId;
    private JournalpostId journalpostId;
    private BehandlingTema behandlingTema;
    private DokumentTypeId dokumentTypeId;
    private LocalDate forsendelseMottatt;
    private Boolean elektroniskSøknad;
    private String payloadXml;
    private String behandlingÅrsakType;
    private UUID forsendelseId;
    private DokumentKategori dokumentKategori;
    private String journalEnhet;

    private InngåendeSaksdokument() {
        // Skjult.
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
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

    public Boolean isElektroniskSøknad() {
        return elektroniskSøknad;
    }

    public String getPayloadXml() {
        return payloadXml;
    }

    public String getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public DokumentKategori getDokumentKategori() { return dokumentKategori; }

    public String getJournalEnhet() {
        return journalEnhet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long fagsakId;
        private JournalpostId journalpostId;
        private BehandlingTema behandlingTema;
        private DokumentTypeId dokumentTypeId;
        private LocalDate forsendelseMottatt;
        private Boolean elektroniskSøknad = Boolean.TRUE;
        private String payloadXml;
        private String behandlingÅrsakType;
        private UUID forsendelseId;
        private DokumentKategori dokumentKategori;
        private String journalEnhet;

        public InngåendeSaksdokument.Builder medFagsakId(Long fagsakId) {
            this.fagsakId = fagsakId;
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

        public InngåendeSaksdokument.Builder medForsendelseMottatt(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public InngåendeSaksdokument.Builder medElektroniskSøknad(Boolean elektroniskSøknad) {
            this.elektroniskSøknad = elektroniskSøknad;
            return this;
        }

        public InngåendeSaksdokument.Builder medJournalførendeEnhet(String journalEnhet) {
            this.journalEnhet = journalEnhet;
            return this;
        }

        public InngåendeSaksdokument.Builder medPayloadXml(String payloadXml) {
            this.payloadXml = payloadXml;
            return this;
        }

        public InngåendeSaksdokument build() {
            InngåendeSaksdokument saksdokument = new InngåendeSaksdokument();

            saksdokument.fagsakId = this.fagsakId;
            saksdokument.journalpostId = this.journalpostId;
            saksdokument.behandlingTema = this.behandlingTema;
            saksdokument.dokumentTypeId = this.dokumentTypeId;
            saksdokument.forsendelseMottatt = this.forsendelseMottatt;
            saksdokument.elektroniskSøknad = this.elektroniskSøknad;
            saksdokument.payloadXml = this.payloadXml;
            saksdokument.behandlingÅrsakType = this.behandlingÅrsakType;
            saksdokument.forsendelseId = this.forsendelseId;
            saksdokument.dokumentKategori = this.dokumentKategori;
            saksdokument.journalEnhet = this.journalEnhet;
            return saksdokument;
        }
    }
}
