package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.task;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.Behandlingsstatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SakOgBehandlingTask.TASKNAME)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class SakOgBehandlingTask implements ProsessTaskHandler {

    public static final String TASKNAME = "behandlingskontroll.oppdatersakogbehandling";

    public static final String BEHANDLINGS_TYPE_KODE_KEY = "behandlingsTypeKode";
    public static final String SAKSTEMA_KEY = "sakstemaKode";
    public static final String ANSVARLIG_ENHET_KEY = "ansvarligEnhet";
    public static final String BEHANDLING_STATUS_KEY = "behandlingStatus";
    public static final String BEHANDLING_OPPRETTET_TIDSPUNKT_KEY = "opprettBehandling";
    public static final String BEHANDLINGSTEMAKODE = "behandlingstemakode";

    private SakOgBehandlingTjeneste sakOgBehandlingTjeneste;

    SakOgBehandlingTask() {
        //for CDI proxy
    }

    @Inject
    public SakOgBehandlingTask(SakOgBehandlingTjeneste sakOgBehandlingTjeneste) {
        this.sakOgBehandlingTjeneste = sakOgBehandlingTjeneste;
    }


    @Override
    public void doTask(ProsessTaskData prosessTaskData) {

        String behandlingStatusKode = prosessTaskData.getPropertyValue(BEHANDLING_STATUS_KEY);
        if (BehandlingStatus.AVSLUTTET.getKode().equals(behandlingStatusKode)) {
            AvsluttetBehandlingStatus avsluttetBehandlingStatus = new AvsluttetBehandlingStatus();
            fyllUtBehandlingsInfo(prosessTaskData, avsluttetBehandlingStatus);
            avsluttetBehandlingStatus.setAvslutningsStatus(BehandlingStatus.AVSLUTTET.getKode());
            sakOgBehandlingTjeneste.behandlingAvsluttet(avsluttetBehandlingStatus);
        } else {
            OpprettetBehandlingStatus opprettetBehandlingStatus = new OpprettetBehandlingStatus();
            opprettetBehandlingStatus.setHendelsesTidspunkt(LocalDate.parse(prosessTaskData.getPropertyValue(BEHANDLING_OPPRETTET_TIDSPUNKT_KEY)));
            opprettetBehandlingStatus.setBehandlingsTemaKode(prosessTaskData.getPropertyValue(BEHANDLINGSTEMAKODE));
            fyllUtBehandlingsInfo(prosessTaskData, opprettetBehandlingStatus);
            sakOgBehandlingTjeneste.behandlingOpprettet(opprettetBehandlingStatus);
        }
    }

    private void fyllUtBehandlingsInfo(ProsessTaskData prosessTaskData, Behandlingsstatus behandlingsstatus) {
        behandlingsstatus.setAktørId(String.valueOf(prosessTaskData.getAktørId()));
        behandlingsstatus.setBehandlingsId(String.valueOf(prosessTaskData.getBehandlingId()));
        behandlingsstatus.setBehandlingsTypeKode(prosessTaskData.getPropertyValue(BEHANDLINGS_TYPE_KODE_KEY));
        behandlingsstatus.setSakstemaKode(prosessTaskData.getPropertyValue(SAKSTEMA_KEY));
        behandlingsstatus.setAnsvarligEnhetRef(prosessTaskData.getPropertyValue(ANSVARLIG_ENHET_KEY));
    }
}
