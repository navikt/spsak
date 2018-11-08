package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag;

import java.time.LocalDate;
import java.util.List;

public class BekreftetAdopsjon {

    private LocalDate omsorgsovertakelseDato;
    private List<BekreftetAdopsjonBarn> adopsjonBarn;
    private boolean ektefellesBarn;
    private boolean adoptererAlene;

    public BekreftetAdopsjon(LocalDate omsorgsovertakelseDato, List<BekreftetAdopsjonBarn> adopsjonBarn) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
        this.adopsjonBarn = adopsjonBarn;
    }

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public boolean isEktefellesBarn() {
        return ektefellesBarn;
    }

    public boolean isAdoptererAlene() {
        return adoptererAlene;
    }

    public List<BekreftetAdopsjonBarn> getAdopsjonBarn() {
        return adopsjonBarn;
    }

    public void setEktefellesBarn(boolean ektefellesBarn) {
        this.ektefellesBarn = ektefellesBarn;
    }

    public void setAdoptererAlene(boolean adoptererAlene) {
        this.adoptererAlene = adoptererAlene;
    }
}
