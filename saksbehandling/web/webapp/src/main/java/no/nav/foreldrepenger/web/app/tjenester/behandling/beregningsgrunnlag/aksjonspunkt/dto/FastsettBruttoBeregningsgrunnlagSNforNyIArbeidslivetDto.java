package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto.AKSJONSPUNKT_KODE)
public class FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto extends BekreftetAksjonspunktDto {

    public static final String AKSJONSPUNKT_KODE = "5049";

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer bruttoBeregningsgrunnlag;

    FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto() {
        // For Jackson
    }

    public FastsettBruttoBeregningsgrunnlagSNforNyIArbeidslivetDto(String begrunnelse, Integer bruttoBeregningsgrunnlag) {
        super(begrunnelse);
        this.bruttoBeregningsgrunnlag = bruttoBeregningsgrunnlag;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public Integer getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }
}
