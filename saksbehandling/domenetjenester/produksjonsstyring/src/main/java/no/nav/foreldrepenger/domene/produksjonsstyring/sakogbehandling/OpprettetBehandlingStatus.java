package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling;

import java.time.LocalDate;

public class OpprettetBehandlingStatus extends Behandlingsstatus {

    private String behandlingsTemaKode; //Kodeverk: http://nav.no/kodeverk/Kodeverk/Behandlingstemaer
    private LocalDate hendelsesTidspunkt;
    private String primaerBehandlingsRef; //Legger til knytning mellom behandlinger. Sett denne til forrige behandling hvis relasjon mellom behandlinger.

    public String getBehandlingsTemaKode() {
        return behandlingsTemaKode;
    }

    public void setBehandlingsTemaKode(String behandlingsTemaKode) {
        this.behandlingsTemaKode = behandlingsTemaKode;
    }

    public void setHendelsesTidspunkt(LocalDate hendelsesTidspunkt) {
        this.hendelsesTidspunkt = hendelsesTidspunkt;
    }

    public LocalDate getHendelsesTidspunkt() {
        return hendelsesTidspunkt;
    }

    public String getPrimaerBehandlingsRef() {
        return primaerBehandlingsRef;
    }

    public void setPrimaerBehandlingsRef(String primaerBehandlingsRef) {
        this.primaerBehandlingsRef = primaerBehandlingsRef;
    }
}
