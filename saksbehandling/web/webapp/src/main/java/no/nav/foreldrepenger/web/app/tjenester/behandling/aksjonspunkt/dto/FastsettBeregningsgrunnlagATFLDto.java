package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.BekreftetAksjonspunktDto;

@JsonTypeName(FastsettBeregningsgrunnlagATFLDto.AKSJONSPUNKT_KODE)
public class FastsettBeregningsgrunnlagATFLDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5038";

    @Valid
    @Size(max = 100)
    private List<InntektPrAndelDto> inntektPrAndelList;

    @Min(0)
    @Max(100 * 1000 * 1000)
    private Integer inntektFrilanser;


    FastsettBeregningsgrunnlagATFLDto() {
        // For Jackson
    }


    public FastsettBeregningsgrunnlagATFLDto(String begrunnelse, List<InntektPrAndelDto> inntektPrAndelList, Integer inntektFrilanser) { // NOSONAR
        super(begrunnelse);
        this.inntektPrAndelList = new ArrayList<>(inntektPrAndelList);
        this.inntektFrilanser = inntektFrilanser;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Integer getInntektFrilanser() {
        return inntektFrilanser;
    }

    public List<InntektPrAndelDto> getInntektPrAndelList() {
        return inntektPrAndelList;
    }
}
