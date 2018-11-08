package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(VurderSøknadsfristDto.AKSJONSPUNKT_KODE)
public class VurderSøknadsfristDto extends BekreftetAksjonspunktDto   {

    static final String AKSJONSPUNKT_KODE = "5043";

    private LocalDate ansesMottattDato;

    private boolean harGyldigGrunn;

    VurderSøknadsfristDto() {
        // for json deserialisering
    }


    public VurderSøknadsfristDto(String begrunnelse, Boolean harGyldigGrunn) { // NOSONAR
        super(begrunnelse);
        this.harGyldigGrunn = harGyldigGrunn;
    }

    public LocalDate getAnsesMottattDato() {
        return ansesMottattDato;
    }

    public void setAnsesMottattDato(LocalDate mottatDato) {
        this.ansesMottattDato = mottatDato;
    }

    public boolean harGyldigGrunn(){return harGyldigGrunn;}

    public void setHarGyldigGrunn(boolean harGyldigGrunn) {
        this.harGyldigGrunn = harGyldigGrunn;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
