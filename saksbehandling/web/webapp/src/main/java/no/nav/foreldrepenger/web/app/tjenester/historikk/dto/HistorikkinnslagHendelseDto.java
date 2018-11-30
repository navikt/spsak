package no.nav.foreldrepenger.web.app.tjenester.historikk.dto;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public class HistorikkinnslagHendelseDto {
    private HistorikkinnslagType navn;
    private String verdi;

    public HistorikkinnslagType getNavn() {
        return navn;
    }

    public void setNavn(HistorikkinnslagType navn) {
        this.navn = navn;
    }

    public String getVerdi() {
        return verdi;
    }

    public void setVerdi(String verdi) {
        this.verdi = verdi;
    }

    public static HistorikkinnslagHendelseDto mapFra(HistorikkinnslagFelt hendelse, KodeverkRepository kodeverkRepository) {
        HistorikkinnslagHendelseDto dto = new HistorikkinnslagHendelseDto();
        dto.navn = kodeverkRepository.finn(HistorikkinnslagType.class, hendelse.getNavn());
        dto.verdi = hendelse.getTilVerdi();
        return dto;
    }
}
