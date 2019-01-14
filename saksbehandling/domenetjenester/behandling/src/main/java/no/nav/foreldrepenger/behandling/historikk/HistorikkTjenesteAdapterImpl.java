package no.nav.foreldrepenger.behandling.historikk;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@RequestScoped
public class HistorikkTjenesteAdapterImpl implements HistorikkTjenesteAdapter {
    private HistorikkRepository historikkRepository;
    private HistorikkInnslagTekstBuilder builder;
    private HistorikkInnslagKonverter historikkinnslagKonverter;

    HistorikkTjenesteAdapterImpl() {
        // for CDI proxy
    }

    @Inject
    public HistorikkTjenesteAdapterImpl(HistorikkRepository historikkRepository, HistorikkInnslagKonverter historikkinnslagKonverter) {
        this.historikkRepository = historikkRepository;
        this.historikkinnslagKonverter = historikkinnslagKonverter;
        this.builder = new HistorikkInnslagTekstBuilder();
    }

    @Override
    public List<HistorikkinnslagDto> hentAlleHistorikkInnslagForSak(Saksnummer saksnummer) {
        List<Historikkinnslag> historikkinnslagList = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        return historikkinnslagList.stream()
            .map(historikkinnslagKonverter::mapFra)
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public void lagInnslag(Historikkinnslag historikkinnslag) {
        historikkRepository.lagre(historikkinnslag);
    }

    @Override
    public HistorikkInnslagTekstBuilder tekstBuilder() {
        return builder;
    }

    @Override
    public void opprettHistorikkInnslag(Behandling behandling, HistorikkinnslagType hisType) {
        if (!builder.getHistorikkinnslagDeler().isEmpty() || builder.antallEndredeFelter() > 0 ||
            builder.getErBegrunnelseEndret() || builder.getErGjeldendeFraSatt()) {

            Historikkinnslag innslag = new Historikkinnslag();

            builder.medHendelse(hisType);
            innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
            innslag.setType(hisType);
            innslag.setBehandlingId(behandling.getId());
            builder.build(innslag);

            resetBuilder();

            lagInnslag(innslag);
        }
    }

    private void resetBuilder() {
        builder = new HistorikkInnslagTekstBuilder();
    }
}
