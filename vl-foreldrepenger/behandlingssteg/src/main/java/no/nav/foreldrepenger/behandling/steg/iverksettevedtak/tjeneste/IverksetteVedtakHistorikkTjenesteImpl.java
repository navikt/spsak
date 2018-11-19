package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakHistorikkTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;

@ApplicationScoped
public class IverksetteVedtakHistorikkTjenesteImpl implements IverksetteVedtakHistorikkTjeneste {

    private HistorikkRepository historikkRepository;

    IverksetteVedtakHistorikkTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public IverksetteVedtakHistorikkTjenesteImpl(HistorikkRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    @Override
    public void opprettHistorikkinnslagNårIverksettelsePåVent(Behandling behandling, boolean venterTidligereBehandling, boolean kanIverksettes) {
        HistorikkInnslagTekstBuilder delBuilder = new HistorikkInnslagTekstBuilder();
        delBuilder.medHendelse(HistorikkinnslagType.IVERKSETTELSE_VENT);
        if (!kanIverksettes && !venterTidligereBehandling) {
            delBuilder.medÅrsak(Venteårsak.VENT_INFOTRYGD);
        } else if (kanIverksettes && venterTidligereBehandling) {
            delBuilder.medÅrsak(Venteårsak.VENT_TIDLIGERE_BEHANDLING);
        } else {
            delBuilder.medÅrsak(Venteårsak.VENT_TIDLIGERE_BEHANDLING);
            delBuilder.ferdigstillHistorikkinnslagDel();
            delBuilder.medÅrsak(Venteårsak.VENT_INFOTRYGD);
        }

        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.IVERKSETTELSE_VENT);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setFagsakId(behandling.getFagsakId());
        historikkinnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        delBuilder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }
}
