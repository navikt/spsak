package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger.api;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import java.sql.Clob;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;

public class InnhentDokumentTjenesteRequest {
    private Long fagsakId;
    private JournalpostId journalpostId;
    private BehandlingTema behandlingTema;
    private DokumentTypeId dokumentTypeId;
    private LocalDate forsendelseMottatt;
    private Boolean elektroniskSøknad;
    private Clob payloadXml;

    InnhentDokumentTjenesteRequest(Long fagsakId, JournalpostId journalpostId, BehandlingTema behandlingTema,
                                   DokumentTypeId dokumentTypeId, LocalDate forsendelseMottatt,
                                   Boolean elektroniskSøknad, Clob payloadXml) {

        Objects.requireNonNull(forsendelseMottatt, "forsendelseMottatt");
        Objects.requireNonNull(behandlingTema, "behandlingTema");

        this.fagsakId = fagsakId;
        this.journalpostId = journalpostId;
        this.behandlingTema = behandlingTema;
        this.dokumentTypeId = dokumentTypeId;
        this.forsendelseMottatt = forsendelseMottatt;
        this.elektroniskSøknad = elektroniskSøknad;
        this.payloadXml = payloadXml;
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

    public Boolean getElektroniskSøknad() {
        return elektroniskSøknad;
    }

    public Clob getPayloadXml() {
        return payloadXml;
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
        private Boolean elektroniskSøknad;
        private Clob payloadXml;

        public InnhentDokumentTjenesteRequest.Builder medFagsakId(Long fagsakId) {
            this.fagsakId = fagsakId;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medJournalpostId(JournalpostId journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medBehandlingstemaKode(BehandlingTema behandlingTema) {
            this.behandlingTema = behandlingTema;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medForsendelseMottatt(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medElektroniskSøknad(Boolean elektroniskSøknad) {
            this.elektroniskSøknad = elektroniskSøknad;
            return this;
        }

        public InnhentDokumentTjenesteRequest.Builder medPayloadXml(Clob payloadXml) {
            this.payloadXml = payloadXml;
            return this;
        }

        public InnhentDokumentTjenesteRequest build() {
            return new InnhentDokumentTjenesteRequest(fagsakId, journalpostId, behandlingTema, dokumentTypeId, forsendelseMottatt, elektroniskSøknad, payloadXml);
        }
    }
}
