package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(AvklarArbeidsforholdDto.AKSJONSPUNKT_KODE)
public class AvklarArbeidsforholdDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5080";

    @Valid
    @Size(max = 1000)
    private List<ArbeidsforholdDto> arbeidsforhold;

    @SuppressWarnings("unused") // NOSONAR
    private AvklarArbeidsforholdDto() {
        super();
        //For Jackson
    }

    public AvklarArbeidsforholdDto(String begrunnelse, List<ArbeidsforholdDto> arbeidsforhold) {
        super(begrunnelse);
        this.arbeidsforhold = arbeidsforhold;
    }

    public List<ArbeidsforholdDto> getArbeidsforhold() {
        return arbeidsforhold;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
