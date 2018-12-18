package no.nav.foreldrepenger.mottak.task;

import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.impl.feilhåndtering.SimpelFeilhåndteringsalgoritme;
import no.nav.vedtak.felles.prosesstask.spi.ForsinkelseStrategi;
import no.nav.vedtak.util.FPDateUtil;


abstract class OverførTilGsakFeilhåndteringsalgoritme extends SimpelFeilhåndteringsalgoritme {

    private ProsessTaskRepository prosessTaskRepository;
    private final KodeverkRepository kodeverkRepository;

    public OverførTilGsakFeilhåndteringsalgoritme(ForsinkelseStrategi forsinkelseStrategi, ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository) {
        super(forsinkelseStrategi);
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public boolean skalKjørePåNytt(ProsessTaskTypeInfo taskType, int antallFeilet, Exception exception) {
        if (exception instanceof IntegrasjonException) {
            return false;
        }
        return super.skalKjørePåNytt(taskType, antallFeilet, exception);
    }

    @Override
    public Feil hendelserNårIkkeKjøresPåNytt(Exception exception, ProsessTaskData prosessTaskData) {
        if (exception instanceof IntegrasjonException) {
            MottakMeldingDataWrapper wrapper = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
            MottakMeldingDataWrapper nesteSteg = wrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME, false, FPDateUtil.nå());
            ProsessTaskData nyProsessTaskData = nesteSteg.getProsessTaskData();
            prosessTaskRepository.lagre(nyProsessTaskData);

            String key = WrappedProsessTaskHandler.metricMeterNameForProsessTasksFraTil(prosessTaskData.getTaskType(), OpprettGSakOppgaveTask.TASKNAME);
            //metricRegistry.meter(key).mark();

            return ((IntegrasjonException) exception).getFeil();
        }
        return super.hendelserNårIkkeKjøresPåNytt(exception, prosessTaskData);
    }

}
