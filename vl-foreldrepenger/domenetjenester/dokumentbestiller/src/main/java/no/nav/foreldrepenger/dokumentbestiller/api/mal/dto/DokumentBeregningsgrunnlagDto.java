package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.util.ArrayList;
import java.util.List;

public class DokumentBeregningsgrunnlagDto {
    //Innvilgelse
    private Boolean overbetaling;
    private Long seksG;
    private Integer dekningsgrad;
    private Long dagsats;
    private Long månedsbeløp;
    private String lovhjemmelBeregning;
    private Boolean inntektOverSeksG;
    private Long totalBrukerAndel;
    private Long totalArbeidsgiverAndel;
    private List<BeregningsgrunnlagRegelDto> beregningsgrunnlagRegel = new ArrayList<>();

    public Boolean getOverbetaling() {
        return overbetaling;
    }

    public void setOverbetaling(Boolean overbetaling) {
        this.overbetaling = overbetaling;
    }

    public Long getSeksG() {
        return seksG;
    }

    public void setSeksG(Long seksG) {
        this.seksG = seksG;
    }

    public Integer getDekningsgrad() {
        return dekningsgrad;
    }

    public void setDekningsgrad(Integer dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public void setDagsats(Long dagsats) {
        this.dagsats = dagsats;
    }

    public Long getMånedsbeløp() {
        return månedsbeløp;
    }

    public void setMånedsbeløp(Long månedsbeløp) {
        this.månedsbeløp = månedsbeløp;
    }

    public String getLovhjemmelBeregning() {
        return lovhjemmelBeregning;
    }

    public void setLovhjemmelBeregning(String lovhjemmelBeregning) {
        this.lovhjemmelBeregning = lovhjemmelBeregning;
    }

    public Boolean getInntektOverSeksG() {
        return inntektOverSeksG;
    }

    public void setInntektOverSeksG(Boolean inntektOverSeksG) {
        this.inntektOverSeksG = inntektOverSeksG;
    }

    public Long getTotalBrukerAndel() {
        return totalBrukerAndel;
    }

    public void setTotalBrukerAndel(Long totalBrukerAndel) {
        this.totalBrukerAndel = totalBrukerAndel;
    }

    public Long getTotalArbeidsgiverAndel() {
        return totalArbeidsgiverAndel;
    }

    public void setTotalArbeidsgiverAndel(Long totalArbeidsgiverAndel) {
        this.totalArbeidsgiverAndel = totalArbeidsgiverAndel;
    }

    public List<BeregningsgrunnlagRegelDto> getBeregningsgrunnlagRegel() {
        return beregningsgrunnlagRegel;
    }

    public void addBeregningsgrunnlagRegel(BeregningsgrunnlagRegelDto beregningsgrunnlagRegel) {
        this.beregningsgrunnlagRegel.add(beregningsgrunnlagRegel);
    }
}
