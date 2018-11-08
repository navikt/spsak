package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.time.LocalDate;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;

public class SoknadAdopsjonDto extends SoknadDto {
    private LocalDate omsorgsovertakelseDato;
    private LocalDate barnetsAnkomstTilNorgeDato;
    private Map<Integer, LocalDate> adopsjonFodelsedatoer;
    private FarSøkerType farSokerType;

    public SoknadAdopsjonDto() {
        super();
    }

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public Map<Integer, LocalDate> getAdopsjonFodelsedatoer() {
        return adopsjonFodelsedatoer;
    }


    public FarSøkerType getFarSokerType() {
        return farSokerType;
    }

    public boolean erOmsorgsovertakelse() {
        return farSokerType != null && !farSokerType.equals(FarSøkerType.ADOPTERER_ALENE);
    }

    public void setOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
    }

    public void setAdopsjonFodelsedatoer(Map<Integer, LocalDate> adopsjonFodelsedatoer) {
        this.adopsjonFodelsedatoer = adopsjonFodelsedatoer;
    }

    public void setFarSokerType(FarSøkerType farSokerType) {
        this.farSokerType = farSokerType;
    }

    public LocalDate getBarnetsAnkomstTilNorgeDato() {
        return barnetsAnkomstTilNorgeDato;
    }

    public void setBarnetsAnkomstTilNorgeDato(LocalDate barnetsAnkomstTilNorgeDato) {
        this.barnetsAnkomstTilNorgeDato = barnetsAnkomstTilNorgeDato;
    }
}
