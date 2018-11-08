package no.nav.foreldrepenger.domene.ytelsefordeling;


import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

import java.util.List;

public class BekreftFaktaForOmsorgVurderingAksjonspunktDto {

    private Boolean aleneomsorg;
    private Boolean omsorg;
    private List<DatoIntervallEntitet> ikkeOmsorgPerioder;

    public BekreftFaktaForOmsorgVurderingAksjonspunktDto(Boolean aleneomsorg, Boolean omsorg, List<DatoIntervallEntitet> ikkeOmsorgPerioder) {
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

    public List<DatoIntervallEntitet> getIkkeOmsorgPerioder() {
        return ikkeOmsorgPerioder;
    }

}
