package no.nav.foreldrepenger.migrering;

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringRepository;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringTjeneste;
import no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter;
import no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverterFactory;

@ApplicationScoped
public class HistorikkMigreringTjenesteImpl implements HistorikkMigreringTjeneste {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistorikkMigreringTjenesteImpl.class);
    private static final int FLUSH_INTERVAL = 1000;

    private HistorikkMigreringKonverterFactory historikkMigreringKonverterFactory;
    private HistorikkMigreringRepository historikkMigreringRepository;

    HistorikkMigreringTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    HistorikkMigreringTjenesteImpl(HistorikkMigreringKonverterFactory historikkMigreringKonverterFactory, HistorikkMigreringRepository historikkMigreringRepository) {
        this.historikkMigreringKonverterFactory = historikkMigreringKonverterFactory;
        this.historikkMigreringRepository = historikkMigreringRepository;
    }

    @Override
    public void migrerAlleHistorikkinnslag() {
        Iterator<Historikkinnslag> historikkinnslagIterator = historikkMigreringRepository.hentAlleHistorikkinnslag();

        for (int i = 0; historikkinnslagIterator.hasNext(); i++) {
            Historikkinnslag historikkinnslag = historikkinnslagIterator.next();
            try {
                migrerHistorikkinnslag(historikkinnslag);
                if (i % FLUSH_INTERVAL == ((FLUSH_INTERVAL - 1))) {
                    historikkMigreringRepository.flush();
                    LOGGER.info("Konvertert historikkinnslag: {} - {}", i - (FLUSH_INTERVAL - 1), i);
                }
            } catch (Exception e) {
                HistorikkMigreringFeil.FACTORY.kanIkkeKonvertereHistorikkInnslag(historikkinnslag.getId(), e).log(LOGGER);
            }
        }
        historikkMigreringRepository.flush();
    }

    @SuppressWarnings("deprecation")
    private void migrerHistorikkinnslag(Historikkinnslag historikkinnslag) {
        if (!historikkinnslag.getHistorikkinnslagDeler().isEmpty()) {
            return;
        }
        if (historikkinnslag.getTekst() == null) {
            lagHistorikkinnslagDel(historikkinnslag);
            return;
        }
        HistorikkinnslagType type = historikkinnslag.getType();
        HistorikkMigreringKonverter konverter = historikkMigreringKonverterFactory.create(type);
        konverter.konverter(historikkinnslag);
        historikkMigreringRepository.lagre(historikkinnslag);
    }

    private void lagHistorikkinnslagDel(Historikkinnslag historikkinnslag) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        tekstBuilder.medHendelse(historikkinnslag.getType());
        if (HistorikkinnslagType.NYE_REGOPPLYSNINGER.equals(historikkinnslag.getType())) {
            tekstBuilder.medBegrunnelse(HistorikkBegrunnelseType.SAKSBEH_START_PA_NYTT);
        }
        tekstBuilder.build(historikkinnslag);
        historikkMigreringRepository.lagre(historikkinnslag);
    }
}
