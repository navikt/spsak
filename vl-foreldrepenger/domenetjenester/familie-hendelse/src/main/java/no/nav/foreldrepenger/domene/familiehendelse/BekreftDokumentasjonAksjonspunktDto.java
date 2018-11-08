package no.nav.foreldrepenger.domene.familiehendelse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BekreftDokumentasjonAksjonspunktDto {
    private LocalDate omsorgsovertakelseDato;
    private Map<Integer, LocalDate> fodselsdatoer;

    public BekreftDokumentasjonAksjonspunktDto(LocalDate omsorgsovertakelseDato, Map<Integer, LocalDate> fodselsdatoer) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
        this.fodselsdatoer = new HashMap<>(fodselsdatoer);
    }

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public Map<Integer, LocalDate> getFodselsdatoer() {
        return fodselsdatoer;
    }
}
