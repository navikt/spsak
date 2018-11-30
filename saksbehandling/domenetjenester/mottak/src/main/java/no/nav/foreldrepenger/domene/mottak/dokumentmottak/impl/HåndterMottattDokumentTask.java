package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(HåndterMottattDokumentTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HåndterMottattDokumentTask extends FagsakProsessTask {

    public static final String TASKTYPE = "innhentsaksopplysninger.håndterMottattDokument";

    private InnhentDokumentTjeneste innhentDokumentTjeneste;
    private KodeverkRepository kodeverkRepository;
    private ObjectMapper objectMapper;

    HåndterMottattDokumentTask() {
        // for CDI proxy
    }

    @Inject
    public HåndterMottattDokumentTask(InnhentDokumentTjeneste innhentDokumentTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.innhentDokumentTjeneste = innhentDokumentTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.objectMapper = JacksonJsonConfig.getObjectMapper();
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        InngåendeSaksdokument inngåendeSaksdokument;
        try {
            inngåendeSaksdokument = objectMapper.readValue(prosessTaskData.getPayloadAsString(), InngåendeSaksdokument.class);
            BehandlingÅrsakType behandlingÅrsakType = BehandlingÅrsakType.UDEFINERT;
            if (prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY) != null) {
                behandlingÅrsakType = kodeverkRepository.finn(BehandlingÅrsakType.class, prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY));
            }
            innhentDokumentTjeneste.utfør(inngåendeSaksdokument, behandlingÅrsakType);
        } catch (IOException e) {
            throw HåndtereDokumentFeil.FACTORY.feilVedParsingAvInngåendeSaksdokument(prosessTaskData, e).toException();
        }
    }
}
