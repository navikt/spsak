package no.nav.foreldrepenger.domene.familiehendelse;

public class BekreftAdopsjonsAksjonspunktDto {
    private boolean bekreft;

    public BekreftAdopsjonsAksjonspunktDto(boolean bekreft) {
        this.bekreft = bekreft;
    }

    public boolean getMannAdoptererAlene() {
        return bekreft;
    }

    public boolean getEktefellesBarn() {
        return bekreft;
    }
}
