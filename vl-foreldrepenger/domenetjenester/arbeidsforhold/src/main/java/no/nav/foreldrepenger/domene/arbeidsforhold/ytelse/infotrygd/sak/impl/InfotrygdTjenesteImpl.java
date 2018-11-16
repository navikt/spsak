package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.impl;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdSak;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak.InfotrygdTjeneste;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;

@ApplicationScoped
public class InfotrygdTjenesteImpl implements InfotrygdTjeneste {
    public static final String TJENESTE = "InfotrygdSak";
    private static final String INFOTRYGD_NEDE_EXCEPTION_TEXT = "Basene i Infotrygd er ikke tilgjengelige";
    private static final Logger log = LoggerFactory.getLogger(InfotrygdTjenesteImpl.class);
    private InfotrygdSakConsumer infotrygdSakConsumer;
    private KodeverkRepository kodeverkRepository;

    InfotrygdTjenesteImpl() {
        // CDI
    }

    @Inject
    public InfotrygdTjenesteImpl(InfotrygdSakConsumer infotrygdSakConsumer, KodeverkRepository kodeverkRepository) {
        this.infotrygdSakConsumer = infotrygdSakConsumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public List<InfotrygdSak> finnSakListe(Behandling behandling, String fnr, LocalDate fom) {
        FinnSakListeResponse response = finnSakListeFull(fnr, fom);
        List<InfotrygdSak> sakene = mapInfotrygdResponseToInfotrygdSak(response);
        log.info("Infotrygd antall saker/vedtak: {} fom {}", sakene.size(), fom);
        return sakene;
    }

    private List<InfotrygdSak> mapInfotrygdResponseToInfotrygdSak(FinnSakListeResponse response) {
        List<InfotrygdSak> saker = new ArrayList<>();
        if (response != null) {
            saker.addAll(response.getVedtakListe().stream().map(vedtak -> new InfotrygdSak(vedtak, kodeverkRepository))
                .collect(Collectors.toList()));
        }
        return saker;
    }

    private FinnSakListeResponse finnSakListeFull(String fnr, LocalDate fom) {
        FinnSakListeRequest request = new FinnSakListeRequest();
        Periode periode = new Periode();
        try {
            periode.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
            periode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(9999, Month.DECEMBER, 31)));
            request.setPeriode(periode);
            request.setPersonident(fnr);
            return infotrygdSakConsumer.finnSakListe(request);
        } catch (FinnSakListePersonIkkeFunnet e) { //$NON-NLS-1$ //NOSONAR
            // Skal ut ifra erfaringer fra fundamentet ikke gjøres noe med, fordi dette er normalt.
            InfotrygdTjenesteFeil.FACTORY.personIkkeFunnet(e).log(log);
        } catch (FinnSakListeUgyldigInput e) {
            throw InfotrygdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (FinnSakListeSikkerhetsbegrensning e) {
            throw InfotrygdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (IntegrasjonException e) {
            if (e.getFeil().getFeilmelding().contains(INFOTRYGD_NEDE_EXCEPTION_TEXT)) {
                throw InfotrygdTjenesteFeil.FACTORY.nedetid(TJENESTE, e).toException();
            } else {
                throw e;
            }
        } catch (DatatypeConfigurationException e) {
            throw InfotrygdTjenesteFeil.FACTORY.tekniskFeil(TJENESTE, e).toException();
        }
        return new FinnSakListeResponse();
    }

}
