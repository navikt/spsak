package no.nav.foreldrepenger.domene.mottak.hendelser.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering.HendelsemottakRepository;
import no.nav.foreldrepenger.domene.mottak.hendelser.KlargjørHendelseTask;
import no.nav.foreldrepenger.domene.mottak.hendelser.MottattHendelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.Hendelse;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class MottattHendelseTjenesteImpl implements MottattHendelseTjeneste {

    private HendelsemottakRepository hendelsemottakRepository;
    private ProsessTaskRepository prosessTaskRepository;

    MottattHendelseTjenesteImpl() {
        //CDI
    }

    @Inject
    public MottattHendelseTjenesteImpl(HendelsemottakRepository hendelsemottakRepository, ProsessTaskRepository prosessTaskRepository) {
        this.hendelsemottakRepository = hendelsemottakRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public boolean erHendelseNy(String uid) {
        return hendelsemottakRepository.hendelseErNy(uid);
    }

    @Override
    public void registrerHendelse(String uid, Hendelse hendelse) {
        hendelsemottakRepository.registrerMottattHendelse(uid);
        ProsessTaskData taskData = new ProsessTaskData(KlargjørHendelseTask.TASKNAME);
        taskData.setPayload(JsonMapper.toJson(hendelse));
        taskData.setProperty(KlargjørHendelseTask.PROPERTY_HENDELSE_TYPE, hendelse.getHendelseKode());
        taskData.setProperty(KlargjørHendelseTask.PROPERTY_UID, uid);
        taskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(taskData);
    }
}
