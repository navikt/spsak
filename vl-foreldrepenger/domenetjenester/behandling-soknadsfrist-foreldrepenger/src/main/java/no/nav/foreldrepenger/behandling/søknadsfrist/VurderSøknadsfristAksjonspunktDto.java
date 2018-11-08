package no.nav.foreldrepenger.behandling.søknadsfrist;

import java.time.LocalDate;

public class VurderSøknadsfristAksjonspunktDto {
    private final LocalDate mottattDato;
    private final String begrunnelse;


    public VurderSøknadsfristAksjonspunktDto(LocalDate mottattDato, String begrunnelse) {
        this.mottattDato = mottattDato;
        this.begrunnelse = begrunnelse;
    }

    public VurderSøknadsfristAksjonspunktDto(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
        this.begrunnelse = null;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }


}
