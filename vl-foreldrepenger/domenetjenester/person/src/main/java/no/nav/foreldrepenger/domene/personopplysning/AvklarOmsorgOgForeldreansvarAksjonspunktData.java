package no.nav.foreldrepenger.domene.personopplysning;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class AvklarOmsorgOgForeldreansvarAksjonspunktData {
    private String vilkarTypeKode;
    private LocalDate omsorgsovertakelseDato;
    private Integer antallBarn;
    private List<AvklartDataBarnAdapter> barn;
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    public AvklarOmsorgOgForeldreansvarAksjonspunktData(String vilkarTypeKode, AksjonspunktDefinisjon aksjonspunktDefinisjon, LocalDate omsorgsovertakelseDato, Integer antallBarn, List<AvklartDataBarnAdapter> barn) {
        this.vilkarTypeKode = vilkarTypeKode;
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
        this.antallBarn = antallBarn;
        this.barn = barn;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public String getVilkarTypeKode() {
        return vilkarTypeKode;
    }

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public List<AvklartDataBarnAdapter> getBarn() {
        return barn;
    }


}
