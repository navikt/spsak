package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto;

import java.util.List;
import java.util.Set;

public class TotrinnskontrollAksjonspunkterDto {

    private String aksjonspunktKode;
    private List<TotrinnskontrollAktivitetDto> opptjeningAktiviteter;
    private TotrinnsBeregningDto beregningDto;
    private String besluttersBegrunnelse;
    private Boolean totrinnskontrollGodkjent;
    private Set<TotrinnskontrollVurderÅrsak> vurderPaNyttArsaker;
    private List<TotrinnsArbeidsforholdDto> arbeidforholdDtos;


    public String getAksjonspunktKode() {
        return aksjonspunktKode;
    }

    public List<TotrinnskontrollAktivitetDto> getOpptjeningAktiviteter() {
        return opptjeningAktiviteter;
    }

    public String getBesluttersBegrunnelse() {
        return besluttersBegrunnelse;
    }

    public Boolean getTotrinnskontrollGodkjent() {
        return totrinnskontrollGodkjent;
    }

    public Set<TotrinnskontrollVurderÅrsak> getVurderPaNyttArsaker() {
        return vurderPaNyttArsaker;
    }

    public TotrinnsBeregningDto getBeregningDto() {
        return beregningDto;
    }

    public List<TotrinnsArbeidsforholdDto> getArbeidforholdDtos() {
        return arbeidforholdDtos;
    }

    public static class Builder {
        TotrinnskontrollAksjonspunkterDto kladd = new TotrinnskontrollAksjonspunkterDto();

        public Builder medAksjonspunktKode(String aksjonspunktKode) {
            kladd.aksjonspunktKode = aksjonspunktKode;
            return this;
        }

        public Builder medOpptjeningAktiviteter(List<TotrinnskontrollAktivitetDto> opptjeningAktiviteter) {
            kladd.opptjeningAktiviteter = opptjeningAktiviteter;
            return this;
        }

        public Builder medBeregningDto(TotrinnsBeregningDto beregningDto) {
            kladd.beregningDto = beregningDto;
            return this;
        }

        public Builder medBesluttersBegrunnelse(String besluttersBegrunnelse) {
            kladd.besluttersBegrunnelse = besluttersBegrunnelse;
            return this;
        }

        public Builder medTotrinnskontrollGodkjent(Boolean totrinnskontrollGodkjent) {
            kladd.totrinnskontrollGodkjent = totrinnskontrollGodkjent;
            return this;
        }

        public Builder medVurderPaNyttArsaker(Set<TotrinnskontrollVurderÅrsak> vurderPaNyttArsaker) {
            kladd.vurderPaNyttArsaker = vurderPaNyttArsaker;
            return this;
        }

        public TotrinnskontrollAksjonspunkterDto build() {
            return kladd;
        }

        public Builder medArbeidsforhold(List<TotrinnsArbeidsforholdDto> totrinnsArbeidsforholdDtos) {
            kladd.arbeidforholdDtos = totrinnsArbeidsforholdDtos;
            return this;
        }
    }
}
