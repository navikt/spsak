package no.nav.foreldrepenger.web.app.tjenester.historikk.dto;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

@RequestScoped
public class HistorikkInnslagKonverter {

    private KodeverkRepository kodeverkRepository;
    private AksjonspunktRepository aksjonspunktRepository;

    public HistorikkInnslagKonverter() {// NOSONAR
    }

    @Inject
    public HistorikkInnslagKonverter(KodeverkRepository kodeverkRepository, AksjonspunktRepository aksjonspunktRepository) {
        this.kodeverkRepository = kodeverkRepository;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    public HistorikkinnslagDto mapFra(Historikkinnslag hi) {
        HistorikkinnslagDto dto = new HistorikkinnslagDto();
        dto.setBehandlingId(hi.getBehandlingId());
        List<HistorikkinnslagDelDto> historikkinnslagDeler = HistorikkinnslagDelDto.mapFra(hi.getHistorikkinnslagDeler(), kodeverkRepository, aksjonspunktRepository);
        dto.setHistorikkinnslagDeler(historikkinnslagDeler);
        List<HistorikkInnslagDokumentLinkDto> dokumentLinks = HistorikkInnslagDokumentLinkDto.mapFra(hi.getDokumentLinker());
        dto.setDokumentLinks(dokumentLinks);
        dto.setOpprettetAv(medStorBokstav(hi.getOpprettetAv()));
        dto.setOpprettetTidspunkt(hi.getOpprettetTidspunkt());
        dto.setType(hi.getType());
        dto.setAktoer(hi.getAktør());
        dto.setKjoenn(hi.getKjoenn());
        return dto;
    }

    private String medStorBokstav(String opprettetAv) {
        return opprettetAv.substring(0,1).toUpperCase() + opprettetAv.substring(1);
    }
}
