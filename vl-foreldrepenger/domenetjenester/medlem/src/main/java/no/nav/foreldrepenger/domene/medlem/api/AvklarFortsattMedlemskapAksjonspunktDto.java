package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;

public class AvklarFortsattMedlemskapAksjonspunktDto {
    private LocalDate fomDato;
    private boolean gjelderEndringIPersonopplysninger;

    public AvklarFortsattMedlemskapAksjonspunktDto(LocalDate fomDato, boolean gjelderEndringIPersonopplysninger) {
        this.fomDato = fomDato;
        this.gjelderEndringIPersonopplysninger = gjelderEndringIPersonopplysninger;
    }

    public LocalDate getFomDato() {
        return fomDato;
    }

    public boolean isGjelderEndringIPersonopplysninger() {
        return gjelderEndringIPersonopplysninger;
    }
}
