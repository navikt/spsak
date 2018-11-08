package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

@JsonTypeName(BekreftDokumentertDatoAksjonspunktDto.AKSJONSPUNKT_KODE)
public class BekreftDokumentertDatoAksjonspunktDto extends BekreftetAksjonspunktDto implements OmsorgsOvertakelse {

    static final String AKSJONSPUNKT_KODE = "5004";

    @NotNull
    private LocalDate omsorgsovertakelseDato;

    @Valid
    @Size(max = 9)
    private Map<Integer, LocalDate> fodselsdatoer;

    BekreftDokumentertDatoAksjonspunktDto() { // NOSONAR
        // For Jackson
    }

    public BekreftDokumentertDatoAksjonspunktDto(String begrunnelse, LocalDate omsorgsovertakelseDato,
            Map<Integer, LocalDate> fodselsdatoer) { // NOSONAR

        super(begrunnelse);
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
        this.fodselsdatoer = fodselsdatoer;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    @Override
    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public Map<Integer, LocalDate> getFodselsdatoer() {
        return fodselsdatoer;
    }

}
