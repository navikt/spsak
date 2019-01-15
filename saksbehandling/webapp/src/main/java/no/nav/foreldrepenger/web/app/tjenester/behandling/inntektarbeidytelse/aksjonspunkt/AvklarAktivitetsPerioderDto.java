package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.aksjonspunkt;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(AvklarAktivitetsPerioderDto.AKSJONSPUNKT_KODE)
public class AvklarAktivitetsPerioderDto extends BekreftetAksjonspunktDto  {

    static final String AKSJONSPUNKT_KODE = "5051";

    @Valid
    @Size(max = 1000)
    private List<OpptjeningAktivitetDto> opptjeningAktivitetList;

    @SuppressWarnings("unused") // NOSONAR
    private AvklarAktivitetsPerioderDto() {
        super();
        // For Jackson
    }

    public AvklarAktivitetsPerioderDto(String begrunnelse, List<OpptjeningAktivitetDto> opptjeningAktivitetList) {
        super(begrunnelse);
        this.opptjeningAktivitetList = opptjeningAktivitetList;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public List<OpptjeningAktivitetDto> getOpptjeningAktivitetList() {
        return opptjeningAktivitetList;
    }

}
