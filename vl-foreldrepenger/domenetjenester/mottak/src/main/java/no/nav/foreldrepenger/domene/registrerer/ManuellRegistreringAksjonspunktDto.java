package no.nav.foreldrepenger.domene.registrerer;

import java.time.LocalDate;

public class ManuellRegistreringAksjonspunktDto {

    private boolean erFullstendigSøknad;
    private String søknadsXml;
    private String dokumentTypeIdKode;
    private LocalDate mottattDato;
    private boolean erRegistrertVerge;

    public ManuellRegistreringAksjonspunktDto(boolean erFullstendigSøknad) {
        this.erFullstendigSøknad = erFullstendigSøknad;
    }

    public ManuellRegistreringAksjonspunktDto(boolean erFullstendigSøknad, String søknadsXml, String dokumentTypeIdKode,
                                              LocalDate mottattDato, boolean erRegistrertVerge) {
        this.erFullstendigSøknad = erFullstendigSøknad;
        this.søknadsXml = søknadsXml;
        this.dokumentTypeIdKode = dokumentTypeIdKode;
        this.mottattDato = mottattDato;
        this.erRegistrertVerge = erRegistrertVerge;
    }

    public boolean getErFullstendigSøknad() {
        return erFullstendigSøknad;
    }

    public String getSøknadsXml() {
        return søknadsXml;
    }

    public String getDokumentTypeIdKode() {
        return dokumentTypeIdKode;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public boolean getErRegistrertVerge() {
        return erRegistrertVerge;
    }
}
