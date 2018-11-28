package no.nav.foreldrepenger.domene.mottak.dokumentmottak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndtereDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class SaksbehandlingDokumentmottakTjenesteImpl implements SaksbehandlingDokumentmottakTjeneste {

    private ObjectWriter writer;
    private ProsessTaskRepository prosessTaskRepository;

    SaksbehandlingDokumentmottakTjenesteImpl() {
        // CDI
    }

    @Inject
    public SaksbehandlingDokumentmottakTjenesteImpl(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.writer = JacksonJsonConfig.getObjectMapper().writerWithDefaultPrettyPrinter();
    }

    @Override
    public void dokumentAnkommet(InngåendeSaksdokument saksdokument) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterMottattDokumentTaskProperties.TASKTYPE);

        prosessTaskData.setFagsakId(saksdokument.getFagsakId());
        prosessTaskData.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY, saksdokument.getBehandlingTema().getOffisiellKode());
        prosessTaskData.setProperty(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY, saksdokument.getBehandlingÅrsakType());
        try {
            prosessTaskData.setPayload(writer.writeValueAsString(saksdokument));
        } catch (JsonProcessingException e) {
            throw HåndtereDokumentFeil.FACTORY.feilVedParsingAvInngåendeSaksdokument(saksdokument, e).toException();
        }

        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }
}
