package no.nav.foreldrepenger.web.app.tjenester.historikk.dto;

import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public class HistorikkinnslagOpplysningDto {

    private HistorikkOpplysningType opplysningType;
    private String tilVerdi;

    public HistorikkinnslagOpplysningDto(){
    }

    public HistorikkOpplysningType getOpplysningType() {
        return opplysningType;
    }

    public void setOpplysningType(HistorikkOpplysningType opplysningType) {
        this.opplysningType = opplysningType;
    }

    public String getTilVerdi() {
        return tilVerdi;
    }

    public void setTilVerdi(String tilVerdi) {
        this.tilVerdi = tilVerdi;
    }

    static List<HistorikkinnslagOpplysningDto> mapFra(List<HistorikkinnslagFelt> opplysninger, KodeverkRepository kodeverkRepository) {
        return opplysninger.stream().map(o -> mapFra(o, kodeverkRepository)).collect(Collectors.toList());
    }

    private static HistorikkinnslagOpplysningDto mapFra(HistorikkinnslagFelt opplysning, KodeverkRepository kodeverkRepository) {
        HistorikkinnslagOpplysningDto dto = new HistorikkinnslagOpplysningDto();
        HistorikkOpplysningType opplysningType = kodeverkRepository.finn(HistorikkOpplysningType.class, opplysning.getNavn());
        dto.setOpplysningType(opplysningType);
        dto.setTilVerdi(opplysning.getTilVerdi());
        return dto;
    }
}
