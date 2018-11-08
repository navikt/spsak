package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedDto;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;


@ApplicationScoped
public class InfotrygdHendelseTjenesteImpl implements InfotrygdHendelseTjeneste {

    private static final Logger log = LoggerFactory.getLogger(InfotrygdHendelseTjenesteImpl.class);
    private static final String ENDPOINT_KEY = "infotrygd.hendelser.api.url";
    private static final String FOM_DATO = "fomDato";
    private static final String AKTØR_ID = "aktorId";

    private OidcRestClient oidcRestClient;
    private URI endpoint;
    private InfotrygdHendelseMapper mapper;

    InfotrygdHendelseTjenesteImpl() {
        //CDI
    }

    @Inject
    public InfotrygdHendelseTjenesteImpl(@KonfigVerdi(ENDPOINT_KEY) URI endpoint,
                                         OidcRestClient oidcRestClient,
                                         InfotrygdHendelseMapper mapper) {
        this.endpoint = endpoint;
        this.oidcRestClient = oidcRestClient;
        this.mapper = mapper;
    }

    @Override
    public List<InfotrygdHendelse> hentHendelsesListFraInfotrygdFeed(Behandling behandling) {
        List<InfotrygdHendelse> hendelseList = new ArrayList<>();
        String fomStr = finnDatoAvForslagTilVedtak(behandling);
        URI request = request(fomStr, behandling.getAktørId().getId());

        FeedDto feed = oidcRestClient.get(request, FeedDto.class);

        log.debug("Fått response fra Infotrygd");

        if (feed == null) {
            log.warn("Kunne ikke hente infotrygdFeed for endpoint={}", request); // NOSONAR
            throw new IllegalStateException("Kunne ikke hente InfotrygdFeed");
        }

        if (feed.getElementer() != null && !feed.getElementer().isEmpty()) {
            List<FeedElement> feedElementList = feed.getElementer();
            for (FeedElement feedElement : feedElementList) {
                hendelseList.add(mapper.mapFraFeedTilInfotrygdHendelse(feedElement));
            }
            log.info("Hendelser som ble lest fra InfotrygdFeed {} med Sekvensnummer {}; Behandling: {}",
                hendelseList.stream().map(InfotrygdHendelse::getType).collect(Collectors.toList()),
                hendelseList.stream().map(InfotrygdHendelse::getSekvensnummer).collect(Collectors.toList()),
                behandling.getId());
            return hendelseList;
        }
        log.info("InfotrygdFeed inneholder ingen hendelser fra og med {}; Behandling: {}", fomStr, behandling.getId());
        return Collections.emptyList();
    }

    private String finnDatoAvForslagTilVedtak(Behandling behandling) {
        LocalDateTime forslagTilVedtakDato = behandling.getBehandlingStegTilstandHistorikk()
            .filter(tilstand -> tilstand.getBehandlingSteg().equals(BehandlingStegType.FATTE_VEDTAK))
            .map(BehandlingStegTilstand::getOpprettetTidspunkt)
            .findFirst().orElseThrow(() -> new IllegalStateException("Vedtaket ble ikke foreslått for behandling"));

        return konverterFomDatoTilString(forslagTilVedtakDato);
    }

    private String konverterFomDatoTilString(LocalDateTime forslagTilVedtakDato) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return forslagTilVedtakDato.format(formatter);
    }

    private URI request(String fomDato, String aktørId) {
        try {
            return new URIBuilder(endpoint)
                .addParameter(FOM_DATO, fomDato)
                .addParameter(AKTØR_ID, aktørId)
                .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
