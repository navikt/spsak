package no.nav.foreldrepenger.web.app.tjenester.saksbehandler.dto;

import java.time.LocalDateTime;

import no.nav.vedtak.util.FPDateUtil;

public class InnloggetNavAnsattDto {

    private final String brukernavn;
    private final String navn;
    private final boolean kanSaksbehandle;
    private final boolean kanVeilede;
    private final boolean kanBeslutte;
    private final boolean kanOverstyre;
    private final boolean kanBehandleKodeEgenAnsatt;
    private final boolean kanBehandleKode6;
    private final boolean kanBehandleKode7;
    private final LocalDateTime funksjonellTid;

    private InnloggetNavAnsattDto(String brukernavn, String navn, boolean kanSaksbehandle, boolean kanVeilede, boolean kanBeslutte, boolean kanOverstyre, boolean kanBehandleKodeEgenAnsatt, boolean kanBehandleKode6, boolean kanBehandleKode7) {
        this.brukernavn = brukernavn;
        this.navn = navn;
        this.kanSaksbehandle = kanSaksbehandle;
        this.kanVeilede = kanVeilede;
        this.kanBeslutte = kanBeslutte;
        this.kanOverstyre = kanOverstyre;
        this.kanBehandleKodeEgenAnsatt = kanBehandleKodeEgenAnsatt;
        this.kanBehandleKode6 = kanBehandleKode6;
        this.kanBehandleKode7 = kanBehandleKode7;
        this.funksjonellTid = LocalDateTime.now(FPDateUtil.getOffset());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBrukernavn() {
        return brukernavn;
    }

    public String getNavn() {
        return navn;
    }

    public boolean getKanSaksbehandle() {
        return kanSaksbehandle;
    }

    public boolean getKanVeilede() {
        return kanVeilede;
    }

    public boolean getKanBeslutte() {
        return kanBeslutte;
    }

    public boolean getKanOverstyre() {
        return kanOverstyre;
    }

    public boolean getKanBehandleKodeEgenAnsatt() {
        return kanBehandleKodeEgenAnsatt;
    }

    public boolean getKanBehandleKode6() {
        return kanBehandleKode6;
    }

    public boolean getKanBehandleKode7() {
        return kanBehandleKode7;
    }

    public LocalDateTime getFunksjonellTid() {
        return funksjonellTid;
    }

    public static class Builder {
        private String brukernavn;
        private String navn;
        private Boolean kanSaksbehandle;
        private Boolean kanVeilede;
        private Boolean kanBeslutte;
        private Boolean kanOverstyre;
        private Boolean kanBehandleKodeEgenAnsatt;
        private Boolean kanBehandleKode6;
        private Boolean kanBehandleKode7;

        Builder() {
            kanSaksbehandle = false;
            kanVeilede = false;
            kanBeslutte = false;
            kanOverstyre = false;
            kanBehandleKodeEgenAnsatt = false;
            kanBehandleKode6 = false;
            kanBehandleKode7 = false;
        }

        public Builder setBrukernavn(String brukernavn) {
            this.brukernavn = brukernavn;
            return this;
        }

        public Builder setNavn(String navn) {
            this.navn = navn;
            return this;
        }

        public Builder setKanSaksbehandle(Boolean kanSaksbehandle) {
            this.kanSaksbehandle = kanSaksbehandle;
            return this;
        }

        public Builder setKanVeilede(Boolean kanVeilede) {
            this.kanVeilede = kanVeilede;
            return this;
        }

        public Builder setKanBeslutte(Boolean kanBeslutte) {
            this.kanBeslutte = kanBeslutte;
            return this;
        }

        public Builder setKanOverstyre(Boolean kanOverstyre) {
            this.kanOverstyre = kanOverstyre;
            return this;
        }

        public Builder setKanBehandleKodeEgenAnsatt(Boolean kanBehandleKodeEgenAnsatt) {
            this.kanBehandleKodeEgenAnsatt = kanBehandleKodeEgenAnsatt;
            return this;
        }

        public Builder setKanBehandleKode6(Boolean kanBehandleKode6) {
            this.kanBehandleKode6 = kanBehandleKode6;
            return this;
        }

        public Builder setKanBehandleKode7(Boolean kanBehandleKode7) {
            this.kanBehandleKode7 = kanBehandleKode7;
            return this;
        }

        public InnloggetNavAnsattDto create() {
            return new InnloggetNavAnsattDto(brukernavn, navn, kanSaksbehandle, kanVeilede, kanBeslutte, kanOverstyre, kanBehandleKodeEgenAnsatt, kanBehandleKode6, kanBehandleKode7);
        }
    }
}
