package no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg;


import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;

public abstract class BekreftFaktaForOmsorgVurderingDto extends BekreftetAksjonspunktDto {

    private Boolean aleneomsorg;
    private Boolean omsorg;
    
    @Valid
    @Size(max = 50)
    private List<PeriodeDto> ikkeOmsorgPerioder;


    BekreftFaktaForOmsorgVurderingDto() { // NOSONAR
        //For Jackson
    }

    public BekreftFaktaForOmsorgVurderingDto(String begrunnelse,
                                             Boolean aleneomsorg, Boolean omsorg,
                                             List<PeriodeDto> ikkeOmsorgPerioder) { // NOSONAR
        super(begrunnelse);
        this.aleneomsorg = aleneomsorg;
        this.omsorg = omsorg;
        this.ikkeOmsorgPerioder = ikkeOmsorgPerioder;
    }

    public Boolean getAleneomsorg() {
        return aleneomsorg;
    }

    public Boolean getOmsorg() {
        return omsorg;
    }

    public List<PeriodeDto> getIkkeOmsorgPerioder() {
        return ikkeOmsorgPerioder;
    }

    @JsonTypeName(BekreftAleneomsorgVurderingDto.AKSJONSPUNKT_KODE)
    public static class BekreftAleneomsorgVurderingDto extends BekreftFaktaForOmsorgVurderingDto {

        public static final String AKSJONSPUNKT_KODE = "5060";

        @SuppressWarnings("unused") // NOSONAR
        private BekreftAleneomsorgVurderingDto() {
            // For Jackson
        }

        public BekreftAleneomsorgVurderingDto(String begrunnelse,
                                              Boolean aleneomsorg, Boolean omsorg,
                                              List<PeriodeDto> ikkeOmsorgPerioder) { // NOSONAR
            super(begrunnelse, aleneomsorg, omsorg, ikkeOmsorgPerioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }

    }

    @JsonTypeName(BekreftOmsorgVurderingDto.AKSJONSPUNKT_KODE)
    public static class BekreftOmsorgVurderingDto extends BekreftFaktaForOmsorgVurderingDto {

        public static final String AKSJONSPUNKT_KODE = "5061";

        @SuppressWarnings("unused") // NOSONAR
        private BekreftOmsorgVurderingDto() {
            // For Jackson
        }

        public BekreftOmsorgVurderingDto(String begrunnelse,
                                         Boolean aleneomsorg, Boolean omsorg,
                                         List<PeriodeDto> ikkeOmsorgPerioder) { // NOSONAR
            super(begrunnelse, aleneomsorg, omsorg, ikkeOmsorgPerioder);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

    public static class PeriodeDto {

        private LocalDate periodeFom;
        private LocalDate periodeTom;

        public LocalDate getPeriodeFom() {
            return periodeFom;
        }

        public void setPeriodeFom(LocalDate periodeFom) {
            this.periodeFom = periodeFom;
        }

        public LocalDate getPeriodeTom() {
            return periodeTom;
        }

        public void setPeriodeTom(LocalDate periodeTom) {
            this.periodeTom = periodeTom;
        }
    }
}
