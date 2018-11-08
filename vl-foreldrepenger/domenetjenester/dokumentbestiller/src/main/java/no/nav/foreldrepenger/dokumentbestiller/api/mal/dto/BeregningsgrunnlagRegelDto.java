package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BeregningsgrunnlagRegelDto implements Serializable {
    private static final long serialVersionUID = -6444717542495046944L;
    private String status;
    private String tilstøtendeYtelse;
    private int antallArbeidsgivereIBeregning;
    private Boolean navBetalerRestbeløp;
    private boolean besteBeregning;
    private long totaltRedusertBeregningsgrunnlag;
    private List<BeregningsgrunnlagAndelDto> beregningsgrunnlagAndelDto = new ArrayList<>();


    public boolean getBesteBeregning() {
        return besteBeregning;
    }

    public long getTotaltRedusertBeregningsgrunnlag() {
        return totaltRedusertBeregningsgrunnlag;
    }

    public void setTotaltRedusertBeregningsgrunnlag(long totaltRedusertBeregningsgrunnlag) {
        this.totaltRedusertBeregningsgrunnlag = totaltRedusertBeregningsgrunnlag;
    }

    public void setBesteBeregning(boolean besteBeregning) {
        this.besteBeregning = besteBeregning;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BeregningsgrunnlagAndelDto> getBeregningsgrunnlagAndelDto() {
        return beregningsgrunnlagAndelDto;
    }

    public void addBeregningsgrunnlagAndelDto(BeregningsgrunnlagAndelDto beregningsgrunnlagAndelDto) {
        this.beregningsgrunnlagAndelDto.add(beregningsgrunnlagAndelDto);
    }

    public int getAntallArbeidsgivereIBeregning() {
        return antallArbeidsgivereIBeregning;
    }

    public void setAntallArbeidsgivereIBeregning(int antallArbeidsgivereIBeregning) {
        this.antallArbeidsgivereIBeregning = antallArbeidsgivereIBeregning;
    }

    public Boolean getNavBetalerRestbeløp() {
        return navBetalerRestbeløp;
    }

    public void setNavBetalerRestbeløp(Boolean navBetalerRestbeløp) {
        this.navBetalerRestbeløp = navBetalerRestbeløp;
    }

    public String getTilstøtendeYtelse() {
        return tilstøtendeYtelse;
    }

    public void setTilstøtendeYtelse(String tilstøtendeYtelse) {
        this.tilstøtendeYtelse = tilstøtendeYtelse;
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagRegelDto{" +
            "status='" + status + '\'' +
            ", tilstøtendeYtelse='" + tilstøtendeYtelse + '\'' +
            ", antallArbeidsgivereIBeregning=" + antallArbeidsgivereIBeregning +
            ", navBetalerRestbeløp=" + navBetalerRestbeløp +
            ", besteBeregning=" + besteBeregning +
            ", totaltRedusertBeregningsgrunnlag='" + totaltRedusertBeregningsgrunnlag + '\'' +
            ", beregningsgrunnlagAndelDto=" + beregningsgrunnlagAndelDto +
            '}';
    }
}
