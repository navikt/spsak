package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(HåndterMottattDokumentTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HåndterMottattDokumentTask extends FagsakProsessTask {

    public static final String TASKTYPE = "innhentsaksopplysninger.håndterMottattDokument";

    private InnhentDokumentTjeneste innhentDokumentTjeneste;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    private KodeverkRepository kodeverkRepository;

    HåndterMottattDokumentTask() {
        // for CDI proxy
    }

    @Inject
    public HåndterMottattDokumentTask(InnhentDokumentTjeneste innhentDokumentTjeneste,
                                      MottatteDokumentTjeneste mottatteDokumentTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.innhentDokumentTjeneste = innhentDokumentTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Optional<MottattDokument> mottattDokument = mottatteDokumentTjeneste.hentMottattDokument(Long.valueOf(prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.MOTTATT_DOKUMENT_ID_KEY)));
        BehandlingÅrsakType behandlingÅrsakType = BehandlingÅrsakType.UDEFINERT;
        if (prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY) != null) {
            behandlingÅrsakType = kodeverkRepository.finn(BehandlingÅrsakType.class, prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLING_ÅRSAK_TYPE_KEY));
        }
        innhentDokumentTjeneste.utfør(mottattDokument.get(), behandlingÅrsakType);
    }
}
