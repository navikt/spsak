package no.nav.foreldrepenger.migrering.konverter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.migrering.HistorikkMigreringFeil;

@ApplicationScoped
public class HistorikkMigreringKonverterFactory {
    private HistorikkMigreringKonverter historikkKonvertMal1;
    private HistorikkMigreringKonverter historikkKonvertMal2;
    private HistorikkMigreringKonverter historikkKonvertMal3;
    private HistorikkMigreringKonverter historikkKonvertMal4;
    private HistorikkMigreringKonverter historikkKonvertMal5;
    private HistorikkMigreringKonverter historikkKonvertMal6;
    private HistorikkMigreringKonverter historikkKonvertMal7;

    HistorikkMigreringKonverterFactory() {
        // for CDI proxy
    }

    @Inject
    public HistorikkMigreringKonverterFactory(AksjonspunktRepository aksjonspunktRepository, BehandlingRepository behandlingRepository) {
        historikkKonvertMal1 = new HistorikkKonverterMal1();
        historikkKonvertMal2 = new HistorikkKonverterMal2();
        historikkKonvertMal3 = new HistorikkKonverterMal3(aksjonspunktRepository, behandlingRepository);
        historikkKonvertMal4 = new HistorikkKonverterMal4();
        historikkKonvertMal5 = new HistorikkKonverterMal5();
        historikkKonvertMal6 = new HistorikkKonverterMal6();
        historikkKonvertMal7 = new HistorikkKonverterMal7();
    }

    public HistorikkMigreringKonverter create(HistorikkinnslagType historikkinnslagType) {
        String mal = historikkinnslagType.getMal();
        switch (mal) {
            case "TYPE1": return historikkKonvertMal1;
            case "TYPE2": return historikkKonvertMal2;
            case "TYPE3": return historikkKonvertMal3;
            case "TYPE4": return historikkKonvertMal4;
            case "TYPE5": return historikkKonvertMal5;
            case "TYPE6": return historikkKonvertMal6;
            case "TYPE7": return historikkKonvertMal7;
            default: throw HistorikkMigreringFeil.FACTORY.malTypeIkkeStottet(mal).toException();
        }
    }
}
