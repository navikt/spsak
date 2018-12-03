package no.nav.sykepenger.kafka;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.json.JacksonJsonConfig;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.JournalpostMottakDto;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class SøknadConsumer extends KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SøknadConsumer.class);

    private MottakTilSaksdokumentMapper mottakTilSaksdokumentMapper;
    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste;
    private ObjectMapper objectMapper;
    private KodeverkRepository kodeverkRepository;

    SøknadConsumer() {
        // CDI
    }

    @Inject
    public SøknadConsumer(BehandlingRepositoryProvider repositoryProvider, SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste,
                          DokumentArkivTjeneste dokumentArkivTjeneste,
                          FagsakTjeneste fagsakTjeneste) {
        super("sykepengesoeknad");

        this.dokumentmottakTjeneste = dokumentmottakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.objectMapper = JacksonJsonConfig.getObjectMapper();
        this.mottakTilSaksdokumentMapper = new MottakTilSaksdokumentMapper(repositoryProvider.getKodeverkRepository(), fagsakTjeneste, dokumentArkivTjeneste);
    }

    @Override
    public void handleMessage(String key, String payload) {
        LOGGER.debug("Mottatt melding med key='{}', og payload='{}'", key, payload);

        try {
            JournalpostMottakDto dto = objectMapper.readValue(payload, JournalpostMottakDto.class);
            // TODO : Sett noe som gir mening ..
            dto.setDokumentTypeIdOffisiellKode(kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL).getOffisiellKode());
            dto.setDokumentKategoriOffisiellKode(kodeverkRepository.finn(DokumentKategori.class, DokumentKategori.SØKNAD).getOffisiellKode());
            InngåendeSaksdokument saksdokument = mottakTilSaksdokumentMapper.map(dto, PayloadType.JSON);
            dokumentmottakTjeneste.dokumentAnkommet(saksdokument);
        } catch (IOException e) {
            throw KafkaConsumerFeil.FACTORY.klarteIkkeParseInput(getTopic(), payload).toException();
        }
    }
}
