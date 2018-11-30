package no.nav.foreldrepenger.web.app.tjenester.integrasjonstatus;

import java.time.LocalDateTime;

public class SystemNedeDto {

    private String systemNavn;
    private String endepunkt;
    private LocalDateTime nedeFremTilTidspunkt;
    private String feilmelding;
    private String stackTrace;

    public SystemNedeDto(String systemNavn, String endepunkt, LocalDateTime nedeFremTilTidspunkt, String feilmelding, String stackTrace) {
        this.systemNavn = systemNavn;
        this.endepunkt = endepunkt;
        this.nedeFremTilTidspunkt = nedeFremTilTidspunkt;
        this.feilmelding = feilmelding;
        this.stackTrace = stackTrace;
    }

    public String getSystemNavn() {
        return systemNavn;
    }

    public String getEndepunkt() {
        return endepunkt;
    }

    public LocalDateTime getNedeFremTilTidspunkt() {
        return nedeFremTilTidspunkt;
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
