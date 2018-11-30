package no.nav.foreldrepenger.domene.personopplysning;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public class AvklarForeldreansvarAksjonspunktData {
    private LocalDate omsorgsovertakelseDato;
    private LocalDate foreldreansvarDato;
    private Integer antallBarn;
    private List<AvklartDataForeldreAdapter> foreldre;
    private List<AvklartDataBarnAdapter> barn;
    private AksjonspunktDefinisjon aksjonspunktDefinisjon;

    public AvklarForeldreansvarAksjonspunktData(AksjonspunktDefinisjon aksjonspunktDefinisjon, LocalDate omsorgsovertakelseDato,  LocalDate foreldreansvarDato,
                                                Integer antallBarn, List<AvklartDataForeldreAdapter> foreldre, List<AvklartDataBarnAdapter> barn) {
        this.aksjonspunktDefinisjon = aksjonspunktDefinisjon;
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
        this.foreldreansvarDato = foreldreansvarDato;
        this.antallBarn = antallBarn;
        this.foreldre = foreldre;
        this.barn = barn;
    }

    public AksjonspunktDefinisjon getAksjonspunktDefinisjon() {
        return aksjonspunktDefinisjon;
    }

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public LocalDate getForeldreansvarDato() {return foreldreansvarDato;}

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public List<AvklartDataForeldreAdapter> getForeldre() {
        return foreldre;
    }

    public List<AvklartDataBarnAdapter> getBarn() {
        return barn;
    }




}
